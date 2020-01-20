package com.xiyou.basic.remote;

import com.xiyou.basic.remote.annotation.HeaderParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ReflectionUtils;

/**
 * Created by baiyc
 * 2020/1/19/019 20:11
 * Description：请求抽象
 */
public abstract class AbstractHttpParam {
    /**
     * 过滤出头信息字段
     */
    protected static final ReflectionUtils.FieldFilter HEAD_FIELD_FILTER =
            (field) -> field.isAnnotationPresent(HeaderParam.class);

    /**
     * @return
     */
    public HttpHeaders createHttpHeaderFromParam() {
        HttpHeaders httpHeaders = new HttpHeaders();
        ReflectionUtils.doWithFields(headerTargetObject().getClass(), (field) -> {
            HeaderParam headerParam = field.getAnnotation(HeaderParam.class);
            String headerName = StringUtils.defaultIfBlank(headerParam.value(), field.getName());
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            httpHeaders.add(headerName, String.valueOf(field.get(headerTargetObject())));
        }, HEAD_FIELD_FILTER);
        return httpHeaders;
    }

    /**
     * 用于获取Header的目标对象。加了@HeaderParam注解的对象模型
     *
     * @return
     */
    protected Object headerTargetObject() {
        return this;
    }
}
