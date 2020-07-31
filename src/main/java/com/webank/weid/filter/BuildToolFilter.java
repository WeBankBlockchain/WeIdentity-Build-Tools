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

import com.webank.weid.service.DeployService;

@WebFilter(urlPatterns = {"/*"})
public class BuildToolFilter implements Filter {
    
    private static List<String> filterUrl = new ArrayList<String>();
    static {
        filterUrl.add("modal.html");
        filterUrl.add("foot.html");
        filterUrl.add("log.html");
    }
    @Autowired
    DeployService deployService;

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
        }else if (uri.endsWith(".html")) {
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
