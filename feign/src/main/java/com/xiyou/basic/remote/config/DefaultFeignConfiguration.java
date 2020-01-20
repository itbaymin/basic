package com.xiyou.basic.remote.config;

import com.xiyou.basic.remote.GeneralFormDataClient;
import com.xiyou.basic.remote.config.prop.HttpConnectionProperties;
import com.xiyou.basic.remote.feign.BeanFormWriteMessageConverter;
import com.xiyou.basic.remote.feign.SmartQueryMapEncoder;
import com.xiyou.basic.remote.interceptor.LogHttpRequestInterceptor;
import com.xiyou.basic.remote.interceptor.LogHttpRespInterceptor;
import com.xiyou.basic.remote.ribbon.DefaultRibbonClientConfiguration;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.util.CharsetUtil;
import feign.httpclient.ApacheHttpClient;
import feign.optionals.OptionalDecoder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by baiyc
 * 2020/1/19/019 21:01
 * Description：
 */
@EnableConfigurationProperties(HttpConnectionProperties.class)
@ComponentScan(basePackages = "com.xiyou.basic.remote")
@AutoConfigureAfter(RibbonAutoConfiguration.class)
// spring.cloud.consul.ribbon.enabled 需要设置为false  禁用 RibbonConsulAutoConfiguration
@RibbonClients(defaultConfiguration = DefaultRibbonClientConfiguration.class)
@Configuration
public class DefaultFeignConfiguration {

    @Autowired
    private HttpConnectionProperties httpConnectionProperties;

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
                new PoolingHttpClientConnectionManager(httpConnectionProperties.getTimeToLive(), TimeUnit.MINUTES);
        poolingHttpClientConnectionManager.setMaxTotal(httpConnectionProperties.getMaxTotal());
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpConnectionProperties.getMaxTotalPerRoute());

        return poolingHttpClientConnectionManager;
    }

    @Bean(name = "apacheHttpClient", destroyMethod = "close")
    public CloseableHttpClient apacheHttpClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(httpConnectionProperties.getConnectionRequestTimeout())
                .setConnectTimeout(httpConnectionProperties.getConnectTimeout())
                .setSocketTimeout(httpConnectionProperties.getReadTimeout()).build();

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .evictExpiredConnections()
                .disableAutomaticRetries()
                .disableContentCompression()
                .disableCookieManagement()
                .useSystemProperties()
                .setDefaultRequestConfig(requestConfig)
                .addInterceptorFirst(new LogHttpRequestInterceptor())
                .addInterceptorFirst(new LogHttpRespInterceptor())
                .setConnectionManager(poolingHttpClientConnectionManager());

        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        return closeableHttpClient;

    }

    /**
     * 关闭Feign日志记录，关闭Feign重试
     */
    @ConditionalOnProperty(value = "xiyou.feign.enabled", havingValue = "true", matchIfMissing = true)
    @Configuration
    static class FeignConfiguration {

        @Autowired
        private ObjectFactory<HttpMessageConverters> messageConverters;

        private QueryMapEncoder queryMapEncoder = new SmartQueryMapEncoder();

        @Bean
        public QueryMapEncoder smartQueryMapEncoder() {
            return queryMapEncoder;
        }

        @Bean
        public Encoder springEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringEncoder(messageConverters);
        }

        @Bean
        public GeneralFormDataClient formDataClient(Decoder decoder, Encoder encoder, HttpClient httpClient) {
            return Feign.builder()
                    .queryMapEncoder(queryMapEncoder)
                    .encoder(encoder)
                    .decoder(decoder)
                    .retryer(Retryer.NEVER_RETRY)
                    .client(new ApacheHttpClient(httpClient))
                    .target(Target.EmptyTarget.create(GeneralFormDataClient.class, "form-urlencoded-client"));
        }

        @Bean
        @Scope("prototype")
        public Feign.Builder feignBuilder() {
            return Feign.builder().queryMapEncoder(queryMapEncoder).retryer(Retryer.NEVER_RETRY);
        }

        /**
         * 支持读取响应Content-Type: text/html
         * @return
         */
        @Bean
        public HttpMessageConverter<String> stringHttpAdaptHtmlMessageConverter() {
            return new StringHttpMessageConverter(CharsetUtil.UTF_8) {
                @Override
                public boolean canRead(Class<?> clazz, MediaType mediaType) {
                    return super.supports(clazz) && mediaType.isCompatibleWith(MediaType.TEXT_HTML);
                }
            };
        }

        /**
         * 支持Bean参数Form表单请求
         * @return
         */
        @Bean
        public BeanFormWriteMessageConverter beanFormMessageConverter() {
            return new BeanFormWriteMessageConverter();
        }

        @Bean
        public Decoder feignDecoder() {

            OptionalDecoder optionalDecoder = new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));

            return (response, type) -> {

                Response _response = response;

                Collection<String> accpets = response.request().headers().get(HttpHeaders.ACCEPT);

                if(CollectionUtils.isNotEmpty(accpets)) {
                    String responseContentType = accpets.iterator().next();
                    if(!StringUtils.equals(responseContentType, "/") && !StringUtils.equals(responseContentType, "*")) {
                        Map<String, Collection<String>> contentTypeHeader = new HashMap<>();
                        contentTypeHeader.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(responseContentType));
                        _response = response.toBuilder().headers(contentTypeHeader).build();
                    }
                }

                return optionalDecoder.decode(_response, type);
            };
        }

        @Bean
        public Logger.Level feignLoggerLevel() {
            return Logger.Level.NONE;
        }
    }
}
