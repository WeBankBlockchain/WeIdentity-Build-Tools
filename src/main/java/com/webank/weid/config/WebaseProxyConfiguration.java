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