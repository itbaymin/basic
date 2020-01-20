package com.xiyou.basic.remote;

import feign.Headers;
import feign.QueryMap;
import feign.RequestLine;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Map;

/**
 * Created by baiyc
 * 2020/1/19/019 21:00
 * Description：
 */
@Headers("Content-Type: application/x-www-form-urlencoded")
public interface GeneralFormDataClient {

    @RequestLine("POST")
    String postForm(URI uri, MultiValueMap<String, String> formData);

    @RequestLine("POST")
    String postFormWithQuery(URI uri, MultiValueMap<String, String> formData, @QueryMap Map<String, String> query);

    @RequestLine("GET")
    String getForm(URI uri, MultiValueMap<String, String> formData);
}
