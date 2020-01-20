package com.xiyou.basic.web.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiyou.basic.web.LoginHelper;
import com.xiyou.basic.web.PrincipleLoader;
import com.xiyou.basic.web.ValidatorFactoryBeanPostProcessor;
import com.xiyou.basic.web.annotations.NotLogin;
import com.xiyou.basic.web.filter.LoginFilter;
import com.xiyou.basic.web.filter.MdcFilter;
import com.xiyou.basic.web.filter.PeriodCommonsRequestLoggingFilter;
import com.xiyou.basic.web.filter.SystemContextFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by baiyc
 * 2020/1/20/020 17:48
 * Description：mvc配置
 */
@Slf4j
@ComponentScan(basePackages = "com.xiyou.basic.web")
@Configuration
public class WebConfiguration implements WebMvcConfigurer {


    @Bean
    public ValidatorFactoryBeanPostProcessor validatorFactoryBeanPostProcessor() {
        return new ValidatorFactoryBeanPostProcessor();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*").allowedOrigins("*").allowedHeaders("*").allowCredentials(true);
    }

    @ConditionalOnProperty(name = "xiyou.login.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public FilterRegistrationBean loginFilter(
            @Autowired(required = false) PrincipleLoader principleLoader,
            LoginHelper loginHelper,
            ObjectMapper objectMapper,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {

        LoginFilter loginFilter = new LoginFilter(principleLoader, objectMapper, loginHelper);
        Map<RequestMappingInfo, HandlerMethod> maps = requestMappingHandlerMapping.getHandlerMethods();

        maps.entrySet().forEach(entry -> {
            HandlerMethod handlerMethod = entry.getValue();
            boolean typeNotLoginFlag = handlerMethod.getBeanType().isAnnotationPresent(NotLogin.class);
            if (typeNotLoginFlag) {
                RequestMapping requestMapping = entry.getValue().getBeanType().getAnnotation(RequestMapping.class);
                if (ArrayUtils.isEmpty(requestMapping.value())) {
                    entry.getKey().getPatternsCondition().getPatterns().forEach(action -> loginFilter.addNotLoginPattern(action.replaceAll("\\{[^}]*\\}", "*")));
                } else {
                    loginFilter.addNotLoginPattern("/" + requestMapping.value()[0] + "/**");
                }
            } else {
                boolean methodNotLoginFlag = handlerMethod.getMethod().isAnnotationPresent(NotLogin.class);
                if (methodNotLoginFlag) {
                    entry.getKey().getPatternsCondition().getPatterns().forEach(action -> loginFilter.addNotLoginPattern(action.replaceAll("\\{[^}]*\\}", "*")));
                }
            }
        });

        loginFilter.addNotLoginPattern("/favicon.ico");
        loginFilter.addNotLoginPattern("/error");
        loginFilter.addNotLoginPattern("/actuator/**");
        loginFilter.addNotLoginPattern("/instances");
        loginFilter.addNotLoginPattern("/webjars/**");

        // swagger相关路径
//        loginFilter.addNotLoginPattern("/swagger*/**");
//        loginFilter.addNotLoginPattern("/v2/api-docs");
//        loginFilter.addNotLoginPattern("/swagger-ui.html");


        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(loginFilter);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean systemFilter() {

        SystemContextFilter systemContextFilter = new SystemContextFilter();
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(systemContextFilter);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));

        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean mdcFilter() {

        MdcFilter traceFilter = new MdcFilter();

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(traceFilter);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));

        return filterRegistrationBean;
    }


    @Bean
    public FilterRegistrationBean logFilter() {

        PeriodCommonsRequestLoggingFilter periodCommonsRequestLoggingFilter = new PeriodCommonsRequestLoggingFilter();
        periodCommonsRequestLoggingFilter.setIncludePayload(true);
        periodCommonsRequestLoggingFilter.setIncludeQueryString(true);
        periodCommonsRequestLoggingFilter.setMaxPayloadLength(4096);

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(periodCommonsRequestLoggingFilter);
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));

        return filterRegistrationBean;
    }
}
