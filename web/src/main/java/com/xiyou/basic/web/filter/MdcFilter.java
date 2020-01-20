package com.xiyou.basic.web.filter;

import com.xiyou.basic.cache.common.utils.Constants;
import com.xiyou.basic.cache.common.utils.SystemContext;
import com.xiyou.basic.web.UserContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by baiyc
 * 2020/1/20/020 17:46
 * Description：MDC拦截器，用于标记日志
 */
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            String traceId = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss").concat(StringUtils.leftPad(String.valueOf(RandomUtils.nextInt(9999)), 4, "0"));
            request.setAttribute(Constants.GLOBAL_TRACE_KEY, traceId);
            MDC.put(Constants.GLOBAL_TRACE_KEY, traceId);
            MDC.put("client-ip", SystemContext.getRequestIp());
            MDC.put("userId", UserContext.getUserId());
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
