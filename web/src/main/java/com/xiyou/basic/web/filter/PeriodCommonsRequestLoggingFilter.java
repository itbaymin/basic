package com.xiyou.basic.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by baiyc
 * 2020/1/20/020 17:46
 * Description：mvc日志过滤器
 */
@Slf4j(topic = "mvc")
public class PeriodCommonsRequestLoggingFilter extends AbstractRequestLoggingFilter {

    public static final String REQUEST_TIMING = "_REQUEST_TIMING";

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        request.setAttribute(REQUEST_TIMING, System.currentTimeMillis());
        log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        long beforeRequestTime = (long) request.getAttribute(REQUEST_TIMING);
        long now = System.currentTimeMillis();
        long cost = now - beforeRequestTime;
        log.info("{}, cost: {}ms", message, cost);
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return !request.getRequestURI().contains("/actuator");
    }

}
