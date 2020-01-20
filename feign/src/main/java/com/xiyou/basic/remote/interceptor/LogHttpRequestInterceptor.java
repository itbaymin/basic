package com.xiyou.basic.remote.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by baiyc
 * 2020/1/19/019 21:03
 * Description：请求日志
 */
@Slf4j(topic = "integration")
public class LogHttpRequestInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {

        String host = Objects.toString(context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST), "");
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String method = requestLine.getMethod();
        String contentType = "";
        String body = "";
        if(request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
            contentType = httpEntityEnclosingRequest.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
            HttpEntity httpEntity = httpEntityEnclosingRequest.getEntity();

            body = IOUtils.toString(httpEntity.getContent());
            if(!httpEntity.isRepeatable()) {
                httpEntityEnclosingRequest.setEntity(new StringEntity(body));
            }
        }
        context.setAttribute("http.request.timer", System.currentTimeMillis());
        log.info("{} {}{} [{}] payload:{}", method, host, uri, contentType, body);
    }
}
