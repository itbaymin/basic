package com.xiyou.basic.remote.feign;

import feign.QueryMapEncoder;
import feign.codec.EncodeException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by baiyc
 * 2020/1/19/019 21:00
 * Description：编码器
 */
public class SmartQueryMapEncoder implements QueryMapEncoder {

    @Override
    public Map<String, Object> encode (Object object) throws EncodeException {
        try {
            ObjectParamMetadata metadata = ObjectParamMetadata.getMetadata(object.getClass());
            Map<String, Object> fieldNameToValue = new HashMap<>();
            for (Field field : metadata.objectFields) {
                Object value = field.get(object);
                if (value != null && value != object) {
                    fieldNameToValue.put(field.getName(), value);
                }
            }
            return fieldNameToValue;
        } catch (IllegalAccessException e) {
            throw new EncodeException("Failure encoding object into query map", e);
        }
    }
}
