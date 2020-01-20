package com.xiyou.basic.remote.config.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by baiyc
 * 2020/1/19/019 21:02
 * Description：
 */
@Data
@Component
@ConfigurationProperties(prefix = "httpclient.connection")
public class HttpConnectionProperties {

    @Value("${httpclient.connection.connectTimeout:6200}")
    int connectTimeout;
    @Value("${httpclient.connection.so.readTimeout:6000}")
    int readTimeout;
    @Value("${httpclient.connection.connectionRequestTimeout:2000}")
    int connectionRequestTimeout;
    @Value("${httpclient.connection.timeToLive:10}")
    int timeToLive;
    @Value("${httpclient.connection.maxTotal:160}")
    int maxTotal;
    @Value("${httpclient.connection.maxTotalPerRoute:20}")
    int maxTotalPerRoute;

}