package com.xiyou.basic.web;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;

/**
 * Created by baiyc
 * 2020/1/20/020 17:35
 * Description：
 */
public class ValidatorFactoryBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof LocalValidatorFactoryBean) {
            LocalValidatorFactoryBean localValidatorFactoryBean = (LocalValidatorFactoryBean) bean;
            Properties properties = new Properties();
            properties.setProperty(HibernateValidatorConfiguration.FAIL_FAST, "true");
            localValidatorFactoryBean.setValidationProperties(properties);
        }
        return bean;
    }
}
