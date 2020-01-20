package com.xiyou.basic.remote.feign;

import feign.form.util.CharsetUtil;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * Created by baiyc
 * 2020/1/19/019 20:57
 * Description：用于外部接口form表单请求 用 Bean 做参数
 */
public class BeanFormWriteMessageConverter implements HttpMessageConverter {

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        if(mediaType != null) {
            return !MultiValueMap.class.isAssignableFrom(clazz) && mediaType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public Object read(Class clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("BeanFormMessageConverter暂不支持读取", inputMessage);
    }

    @Override
    public void write(Object object, MediaType contentType, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException, IOException {
        ObjectParamMetadata metadata = ObjectParamMetadata.getMetadata(object.getClass());
        StringBuilder builder = new StringBuilder();
        metadata.objectFields.forEach(field -> {
            try {
                if (builder.length() != 0) {
                    builder.append('&');
                }
                builder.append(URLEncoder.encode(field.getName(), CharsetUtil.UTF_8.name()));
                Object value = field.get(object);
                builder.append('=');
                if (value != null) {
                    builder.append(URLEncoder.encode(String.valueOf(value), CharsetUtil.UTF_8.name()));
                }
            } catch (UnsupportedEncodingException | IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        });

        byte[] bytes = builder.toString().getBytes();
        outputMessage.getHeaders().setContentLength(bytes.length);

        StreamUtils.copy(bytes, outputMessage.getBody());
    }
}
