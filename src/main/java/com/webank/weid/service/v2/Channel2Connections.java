package com.webank.weid.service.v2;

import java.io.InputStream;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.fisco.bcos.channel.dto.BcosMessage;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.timeout.IdleStateHandler;

public class Channel2Connections extends ChannelConnections {
    
    private static Logger logger = LoggerFactory.getLogger(ChannelConnections.class);
    
    private long idleTimeout = (long) 10000;
    
    private long heartBeatDelay = (long) 2000;
    
    private Bootstrap bootstrap = new Bootstrap();
    
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    private boolean running = false;
    
    public void startConnect() throws SSLException {
        if (running) {
            logger.debug("running");
            return;
        }

        logger.debug("init connections connect");
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        final ChannelConnections selfService = this;
        final ThreadPoolTaskExecutor selfThreadPool = super.getThreadPool();

        SslContext sslCtx = initSslContextForConnect();
        logger.debug(" connect sslcontext init success");

        bootstrap.handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelHandler handler = new ChannelHandler();
                        handler.setConnections(selfService);
                        handler.setIsServer(false);
                        handler.setThreadPool(selfThreadPool);

                        ch.pipeline()
                                .addLast(
                                        sslCtx.newHandler(ch.alloc()),
                                        new LengthFieldBasedFrameDecoder(
                                                Integer.MAX_VALUE, 0, 4, -4, 0),
                                        new IdleStateHandler(
                                                idleTimeout,
                                                idleTimeout,
                                                idleTimeout,
                                                TimeUnit.MILLISECONDS),
                                        handler);
                    }
                });

        running = true;
        Thread loop =
                new Thread() {
                    public void run() {
                        try {
                            while (true) {
                                if (!running) {
                                    return;
                                }
                                reconnect();
                                Thread.sleep(heartBeatDelay);
                            }
                        } catch (InterruptedException e) {
                            logger.error("error", e);
                            Thread.currentThread().interrupt();
                        }
                    }
                };
        loop.start();
    }
    
    private SslContext initSslContextForConnect() throws SSLException {
        SslContext sslCtx;
        try {
            Resource caResource = getCaCert();
            InputStream caInputStream = caResource.getInputStream();
            Resource keystorecaResource = getSslCert();
            Resource keystorekeyResource = getSslKey();

            sslCtx =
                    SslContextBuilder.forClient()
                            .trustManager(caInputStream)
                            .keyManager(
                                    keystorecaResource.getInputStream(),
                                    keystorekeyResource.getInputStream())
                            .sslProvider(SslProvider.JDK)
                            .build();
        } catch (Exception e) {
            logger.debug("SSLCONTEXT ***********" + e.getMessage());
            throw new SSLException(
                    "Failed to initialize the client-side SSLContext: " + e.getMessage());
        }
        return sslCtx;
    }
    
    public void reconnect() {
        for (Entry<String, ChannelHandlerContext> ctx : networkConnections.entrySet()) {
            if (ctx.getValue() == null || !ctx.getValue().channel().isActive()) {
                String[] split = ctx.getKey().split(":");

                String host = split[0];
                Integer port = Integer.parseInt(split[1]);
                logger.debug("try connect to: {}:{}", host, port);

                bootstrap.connect(host, port);
                logger.debug("connect to: {}:{} success", host, port);
            } else {
                logger.trace("send heart beat to {}", ctx.getKey());
                BcosMessage fiscoMessage = new BcosMessage();

                fiscoMessage.setSeq(UUID.randomUUID().toString().replaceAll("-", ""));
                fiscoMessage.setResult(0);
                fiscoMessage.setType((short) 0x13);
                fiscoMessage.setData("0".getBytes());

                ByteBuf out = ctx.getValue().alloc().buffer();
                fiscoMessage.writeHeader(out);
                fiscoMessage.writeExtra(out);

                ctx.getValue().writeAndFlush(out);
            }
        }
    }
    
    public void stopWork() {
        running = false;
        workerGroup.shutdownGracefully();
        super.getThreadPool().shutdown();
    }
}
