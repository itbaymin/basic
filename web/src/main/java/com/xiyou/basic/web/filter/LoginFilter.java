package com.xiyou.basic.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyou.basic.web.LoginHelper;
import com.xiyou.basic.web.Principle;
import com.xiyou.basic.web.PrincipleLoader;
import com.xiyou.basic.web.UserContext;
import com.xiyou.basic.web.result.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by baiyc
 * 2020/1/20/020 17:45
 * Description：登陆拦截器
 */
@Slf4j
public class LoginFilter extends OncePerRequestFilter {

    private Set<String> notLoginPatterns = new HashSet<>();

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private ObjectMapper objectMapper;

    private PrincipleLoader principleLoader;

    private LoginHelper loginHelper;

    public LoginFilter(PrincipleLoader principleLoader, ObjectMapper objectMapper, LoginHelper loginHelper) {
        this.principleLoader = Optional.ofNullable(principleLoader).orElse(PrincipleLoader.DEFAULT_PRINCIPLE_LOADER);
        this.objectMapper = objectMapper;
        this.loginHelper = loginHelper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        Cookie loginCookie = loginHelper.findLoginCookie(request);

        Principle principle = null;
        if (loginCookie != null) {

            String tokenValue = loginCookie.getValue();
            String userId = loginHelper.getUserId(tokenValue);
            if(StringUtils.isNotBlank(userId)) {
                principle = principleLoader.load(userId);
            } else {
                log.warn("未找到token: {} 对应的用户ID", tokenValue);
            }
        }

        UserContext.set(principle);

        try {
            for (String pattern : notLoginPatterns) {
                if (antPathMatcher.match(pattern, request.getServletPath())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            if(principle == null) {
                unLoginResponse(response);
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.remove();
        }
    }

    /**
     * 给予未登录响应
     * @param response
     * @throws IOException
     */
    private void unLoginResponse(HttpServletResponse response) throws IOException {
        PrintWriter printWriter = response.getWriter();
        response.setContentType("application/json; charset=utf-8");
        // 这里必须要设置允许跨域，因为CrosMapping的配置应用在更后面的适配器执行阶段
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "OPTIONS,GET,POST,DELETE,PUT");
        try {
            objectMapper.writeValue(printWriter, WebResult.UN_LOGIN);
            printWriter.flush();
        } finally {
            IOUtils.closeQuietly(printWriter);
        }
    }

    public void addNotLoginPattern(String notLoginPattern) {
        notLoginPatterns.add(notLoginPattern);
    }

}
