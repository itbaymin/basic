package com.xiyou.basic.web.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.xiyou.basic.cache.common.utils.Constants;
import com.xiyou.basic.web.result.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * Created by baiyc
 * 2020/1/20/020 17:42
 * Description：响应处理
 */
@ControllerAdvice
@Slf4j(topic = "mvc")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return WebResult.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object returnResult, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
        Object requestTraceId = servletServerHttpRequest.getServletRequest().getAttribute(Constants.GLOBAL_TRACE_KEY);
        if(returnResult instanceof WebResult) {
            WebResult webResult = (WebResult) returnResult;
            webResult.setMsgKey(Objects.toString(requestTraceId));
            try {
                log.info("Api响应: " + objectMapper.writeValueAsString(webResult));
            } catch (JsonProcessingException e) {
                log.error("Api响应序列化失败", e);
            }
            return webResult;
        } else if (returnResult instanceof JSONPObject) {
            JSONPObject jsonpObject = (JSONPObject) returnResult;
            Object obj = jsonpObject.getValue();
            if(obj instanceof WebResult) {
                WebResult webResult = (WebResult) obj;
                webResult.setMsgKey(Objects.toString(requestTraceId));
                try {
                    log.info("Api响应: " + objectMapper.writeValueAsString(webResult));
                } catch (JsonProcessingException e) {
                    log.error("Api响应序列化失败", e);
                }
            }
            return jsonpObject;
        } else {
            return returnResult;
        }

    }

}
