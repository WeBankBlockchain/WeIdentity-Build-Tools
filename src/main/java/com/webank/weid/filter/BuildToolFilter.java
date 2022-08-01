

package com.webank.weid.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.webank.weid.service.GuideService;

//@WebFilter(urlPatterns = {"/*"})
public class BuildToolFilter implements Filter {
    
    private static List<String> filterUrl = new ArrayList<String>();
    static {
        filterUrl.add("modal.html");
        filterUrl.add("foot.html");
        filterUrl.add("log.html");
    }
    @Autowired
    GuideService guideService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        String uri = req.getRequestURI();
        String requestFile = uri.substring(uri.lastIndexOf("/") + 1);
        if (filterUrl.contains(requestFile)) {
            chain.doFilter(request, response);
            return;
        } else if (uri.endsWith(".html") || uri.contains("webase-browser")) {
            String guideResult = guideService.getGuideStatus().getResult();
            if (uri.endsWith("guide.html") && StringUtils.isNotBlank(guideResult)) {
                res.sendRedirect("index.html");
                return;
            }
            if (!uri.endsWith("guide.html") && StringUtils.isBlank(guideResult)) {
                String guideHtml = "guide.html";
                if (uri.contains("/webase-browser/")) {
                    guideHtml = "../" + guideHtml;
                } 
                res.sendRedirect(guideHtml);
                return;
            }
            if (uri.endsWith("webase-browser")) {
                res.sendRedirect("webase-browser/index.html");
                return;
            }
            if (uri.endsWith("webase-browser/")) {
                res.sendRedirect("index.html");
                return;
            }
        }
        chain.doFilter(request, response);
    }

}
