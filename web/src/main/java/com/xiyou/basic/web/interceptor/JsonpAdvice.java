package com.xiyou.basic.web.interceptor;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by baiyc
 * 2020/1/20/020 17:43
 * Description：jsonp处理
 */
@ControllerAdvice
public class JsonpAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse servletResponse = ((ServletServerHttpResponse)response).getServletResponse();
        String callback = servletRequest.getParameter("json_callback");//jsonp回调函数名
        if(StringUtils.isBlank(callback)) {
            return body;
        } else {
            servletResponse.addHeader("Access-Control-Allow-Origin", "*");
            servletResponse.addHeader("Access-Control-Allow-Methods", "*");
            servletResponse.addHeader("P3P", "CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
            return new JSONPObject(callback, body);
        }
    }
}
