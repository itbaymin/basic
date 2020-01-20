package com.xiyou.basic.remote.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by baiyc
 * 2020/1/19/019 21:03
 * Description：响应日志
 */
@Slf4j(topic = "integration")
public class LogHttpRespInterceptor implements HttpResponseInterceptor {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {

        Object timer = context.getAttribute("http.request.timer");
        long cost = System.currentTimeMillis() - NumberUtils.toLong(String.valueOf(timer));

        StatusLine statusLine = response.getStatusLine();
        int httpCode = statusLine.getStatusCode();
        String contentType = response.getEntity().getContentType().getValue();
        String body = IOUtils.toString(response.getEntity().getContent());

        if(!response.getEntity().isRepeatable()) {
            response.setEntity(new StringEntity(body, UTF_8));
        }
        log.info("{} [{}] resp_body:{}; cost:{}ms", httpCode, contentType, body, cost);
    }
}
