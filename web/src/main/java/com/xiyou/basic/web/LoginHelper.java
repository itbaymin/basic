package com.xiyou.basic.web;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by baiyc
 * 2020/1/20/020 17:41
 * Description：登陆工具
 */
@Slf4j(topic = "mvc")
@Data
@Component
public class LoginHelper {

    private static final String USER_KEY_TPL = "%s:login:%s";

    public static final String TOKEN_COOKIE_NAME = "XIYOU_TOKEN";

    @Value("${spring.application.name:app}")
    private String appName;

    /** 过期秒数 */
    @Value("${xiyou.login.timeout:7200}")
    private Integer timeout;
    /** cookie存放的域 */
    @Value("${xiyou.login.domain:xiyou.com}")
    private String domain;
    /** cookie路径 */
    @Value("${xiyou.login.path:/}")
    private String path;
    @Value("${xiyou.login.cookieName}")
    private String cookieName;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 注销
     * @param servletRequest
     */
    public void logout(HttpServletRequest servletRequest) {
        Cookie loginCookie = findLoginCookie(servletRequest);
        if(loginCookie != null) {
            String token = loginCookie.getValue();
            String userId = getUserId(token);
            log.info("退出登录, {} => {}", token, userId);
            stringRedisTemplate.delete(getUserKey(loginCookie.getValue()));
        }
    }

    public String writeLogin(HttpServletResponse response, Principle principle) {
        return writeLogin(response, principle, "", null);
    }

    public String writeLogin(HttpServletResponse response, Principle principle, Integer secondsTimeout) {
        return writeLogin(response, principle, "", secondsTimeout);
    }

    /**
     * 生成token => userId 映射. 并写入cookie
     * @param response
     * @param principle
     * @param _token 如果不指定token, 则默认生成随机传
     * @return
     */
    public String writeLogin(HttpServletResponse response, Principle principle, String _token, Integer secondsTimeout) {

        String token = StringUtils.defaultIfBlank(_token, UUID.randomUUID().toString().replaceAll("-", ""));

        stringRedisTemplate.opsForValue().set(getUserKey(token), principle.id(), Math.max(1, ObjectUtils.defaultIfNull(secondsTimeout, timeout)), TimeUnit.SECONDS);

        Cookie loginCookie = new Cookie(TOKEN_COOKIE_NAME, token);
        loginCookie.setDomain(domain);
        loginCookie.setPath(path);
        loginCookie.setMaxAge(ObjectUtils.defaultIfNull(secondsTimeout, timeout).intValue());
        response.addCookie(loginCookie);

        return token;
    }

    public Cookie findLoginCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return null;
        }
        for(Cookie cookie : cookies) {
            if(LoginHelper.TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    public String getUserKey(String token) {
        return String.format(USER_KEY_TPL, appName, token);
    }

    public String getUserId(String token) {
        String userKey = getUserKey(token);
        String userId = stringRedisTemplate.opsForValue().get(userKey);
        return userId;
    }
}
