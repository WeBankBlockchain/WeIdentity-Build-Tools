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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringEscapeUtils;


public class XssHttpServletRequestWraper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWraper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        String str = StringEscapeUtils.escapeHtml(super.getHeader(name));
        return str;
    }

    @Override
    public String getQueryString() {
        String str = StringEscapeUtils.escapeHtml(super.getQueryString());
        return  str;
    }

    @Override
    public String getParameter(String name) {
        String str = StringEscapeUtils.escapeHtml(super.getParameter(name));
        return str;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if(values != null) {
            int length = values.length;
            String[] escapseValues = new String[length];
            for(int i = 0; i < length; i++){
                escapseValues[i] = StringEscapeUtils.escapeHtml(values[i]);
            }
            return escapseValues;
        }
        return super.getParameterValues(name);
    }
}