/*
 *       Copyright© (2018-2020) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.service.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.ChannelHandler;
import org.fisco.bcos.channel.handler.ConnectionInfo;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.tuples.generated.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;

public class Channel2Connections extends ChannelConnections {
    
    private static Logger logger = LoggerFactory.getLogger(ChannelConnections.class);
    /** SSL connection default configuration */
    private static final String CA_CERT = "classpath:ca.crt";

    private static final String SSL_CERT = "classpath:node.crt";
    private static final String SSL_KEY = "classpath:node.key";
    private long idleTimeout = (long) 10000;
    private long connectTimeout = (long) 10000;
    private long sslHandShakeTimeout = (long) 10000;
    
    private Bootstrap bootstrap = new Bootstrap();
    
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    private boolean running = false;
    
    public void startConnect() throws Exception {
        if (running) {
            logger.debug("running");
            return;
        }

        logger.debug(" start connect. ");
        // init netty
        //EventLoopGroup workerGroup = new NioEventLoopGroup();

        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // set connect timeout
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout);

        final ChannelConnections selfService = this;
        final ThreadPoolTaskExecutor selfThreadPool = super.getThreadPool();

        SslContext sslContext =
                (EncryptType.encryptType == EncryptType.ECDSA_TYPE)
                        ? initSslContext()
                        : initSMSslContext();
        logger.debug(" connect sslcontext init success");

        bootstrap.handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        /*
                         * Each connection is fetched from the socketChannel, using the new handler connection information
                         */
                        ChannelHandler handler = new ChannelHandler();
                        handler.setConnections(selfService);
                        handler.setThreadPool(selfThreadPool);

                        SslHandler sslHandler = sslContext.newHandler(ch.alloc());
                        /** set ssl handshake timeout */
                        sslHandler.setHandshakeTimeoutMillis(sslHandShakeTimeout);

                        ch.pipeline()
                                .addLast(
                                        sslHandler,
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

        List<Tuple3<String, Integer, ChannelFuture>> tuple3List = new ArrayList<>();
        // try to connect to all nodes
        for (ConnectionInfo connectionInfo : super.getConnections()) {
            String IP = connectionInfo.getHost();
            Integer port = connectionInfo.getPort();

            ChannelFuture channelFuture = bootstrap.connect(IP, port);
            tuple3List.add(new Tuple3<>(IP, port, channelFuture));
        }

        boolean atLeastOneConnectSuccess = false;
        List<String> errorMessageList = new ArrayList<>();
        // Wait for all connection operations to complete
        for (Tuple3<String, Integer, ChannelFuture> tuple3 : tuple3List) {
            ChannelFuture connectFuture = tuple3.getValue3().awaitUninterruptibly();
            if (!connectFuture.isSuccess()) {
                logger.error(
                        " connect to {}:{}, error: {}",
                        tuple3.getValue1(),
                        tuple3.getValue2(),
                        connectFuture.cause().getMessage());

                String connectFailedMessage =
                        Objects.isNull(connectFuture.cause())
                                ? "connect to "
                                        + tuple3.getValue1()
                                        + ":"
                                        + tuple3.getValue2()
                                        + " failed"
                                : connectFuture.cause().getMessage();
                errorMessageList.add(connectFailedMessage);
            } else {
                // tcp connect success and waiting for SSL handshake
                logger.trace(" connect to {}:{} success", tuple3.getValue1(), tuple3.getValue2());

                SslHandler sslhandler = connectFuture.channel().pipeline().get(SslHandler.class);
                Future<Channel> sshHandshakeFuture =
                        sslhandler.handshakeFuture().awaitUninterruptibly();
                if (sshHandshakeFuture.isSuccess()) {
                    atLeastOneConnectSuccess = true;
                    logger.trace(
                            " ssl handshake success {}:{}", tuple3.getValue1(), tuple3.getValue2());
                } else {

                    String sslHandshakeFailedMessage =
                            " ssl handshake failed:/"
                                    + tuple3.getValue1()
                                    + ":"
                                    + tuple3.getValue2();

                    errorMessageList.add(sslHandshakeFailedMessage);
                }
            }
        }

        // All connections failed
        if (!atLeastOneConnectSuccess) {
            logger.error(" all connections have failed, " + errorMessageList.toString());
            throw new RuntimeException(
                    " Failed to connect to nodes: " + errorMessageList.toString());
        }

        running = true;
        logger.debug(" start connect end. ");
    }
    
    private SslContext initSslContext() throws SSLException {
        SslContext sslCtx;
        try {

            if (!isEnableOpenSSL()) {
                System.setProperty("jdk.tls.namedGroups", "secp256k1");
                logger.info("set jdk.tls.namedGroups option");
            }

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            // check ssl cert file
            Resource caResource = getCaCert();
            Resource keystorecaResource = getSslCert();
            Resource keystorekeyResource = getSslKey();

            // check if ca.crt exist
            if (Objects.isNull(caResource) || !caResource.exists()) {
                Resource resource = resolver.getResource(CA_CERT);
                if (Objects.nonNull(resource) && resource.exists()) {
                    caResource = resource;
                } else {
                    throw new RuntimeException(
                            (Objects.nonNull(caResource) ? "ca.crt" : caResource.getFilename())
                                    + " not exist ");
                }
            }

            // check if sdk.crt exist, if not , check the default value node.crt
            if (Objects.isNull(keystorecaResource) || !keystorecaResource.exists()) {
                Resource resource = resolver.getResource(SSL_CERT);
                if (Objects.nonNull(resource) && resource.exists()) {
                    keystorecaResource = resource;
                } else {
                    throw new RuntimeException(
                            (Objects.nonNull(keystorecaResource)
                                            ? "sdk.crt"
                                            : keystorecaResource.getFilename())
                                    + " not exist ");
                }
            }

            // check if sdk.key exist, if not, check the default value sdk.key
            if (Objects.isNull(keystorekeyResource) || !keystorekeyResource.exists()) {
                Resource resource = resolver.getResource(SSL_KEY);
                if (Objects.nonNull(resource) && resource.exists()) {
                    keystorekeyResource = resource;
                } else {
                    throw new RuntimeException(
                            (Objects.nonNull(keystorekeyResource)
                                            ? "sdk.key"
                                            : keystorekeyResource.getFilename())
                                    + " not exist ");
                }
            }

            logger.info(
                    " ca certificate: {}, sdk certificate: {}, sdk key: {}, enableOpenSsl: {}",
                    caResource.getFilename(),
                    keystorecaResource.getFilename(),
                    keystorekeyResource.getFilename(),
                    isEnableOpenSSL());

            sslCtx =
                    SslContextBuilder.forClient()
                            .trustManager(caResource.getInputStream())
                            .keyManager(
                                    keystorecaResource.getInputStream(),
                                    keystorekeyResource.getInputStream())
                            .sslProvider(isEnableOpenSSL() ? SslProvider.OPENSSL : SslProvider.JDK)
                            .build();
        } catch (Exception e) {
            logger.error(" Failed to initialize the SSLContext, e: {} ", e.getCause());
            throw new SSLException(" Failed to initialize the SSLContext: " + e.getMessage());
        }
        return sslCtx;
    }
    
    public void stopWork() {
        running = false;
        workerGroup.shutdownGracefully();
        super.getThreadPool().shutdown();
    }
}
