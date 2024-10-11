

package com.webank.weid.config;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebaseProxyConfiguration {

    @Value("${proxy.servlet.url}")
    private String proxyServletUrl;

    @Value("${proxy.target.url}")
    private String proxyTargetUrl;

    @Value("${proxy.log.enable}")
    private String proxyLog;

    @Bean
    public ServletRegistrationBean<?> servletRegistrationBean(){
        ServletRegistrationBean<?> servletRegistrationBean = new ServletRegistrationBean<>(new ProxyServlet(), proxyServletUrl);
        servletRegistrationBean.addInitParameter("targetUri", "http://" + proxyTargetUrl);
        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, proxyLog);
        return servletRegistrationBean;
    }
}