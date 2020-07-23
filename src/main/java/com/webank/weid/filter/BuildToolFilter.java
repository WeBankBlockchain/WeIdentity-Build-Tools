package com.webank.weid.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.weid.service.DeployService;

@WebFilter(urlPatterns = {"/*"})
public class BuildToolFilter implements Filter {
    
    @Autowired
    DeployService deployService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        String uri = req.getRequestURI();
        if (uri.endsWith(".html") && !uri.endsWith("foot.html") && !uri.endsWith("log.html")) {
            String guideResult = deployService.getGuideStatus();
            if (uri.endsWith("guide.html") && StringUtils.isNotBlank(guideResult)) {
                res.sendRedirect("index.html");
                return;
            }
            if (!uri.endsWith("guide.html") && StringUtils.isBlank(guideResult)) {
                res.sendRedirect("guide.html");
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
