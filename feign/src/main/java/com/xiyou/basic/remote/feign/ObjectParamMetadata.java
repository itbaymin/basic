package com.xiyou.basic.remote.feign;

import com.xiyou.basic.remote.annotation.NotUrlParam;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by baiyc
 * 2020/1/19/019 20:58
 * Description：参数元数据
 */
public class ObjectParamMetadata {
    private static final Map<Class<?>, ObjectParamMetadata> classToMetadata = new HashMap<>();

    final List<Field> objectFields;

    private ObjectParamMetadata (List<Field> objectFields) {
        this.objectFields = Collections.unmodifiableList(objectFields);
    }

    static ObjectParamMetadata parseObjectType(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Field field : FieldUtils.getAllFields(type)) {

            // ADDED 参数过滤
            if(field.isAnnotationPresent(NotUrlParam.class) || Modifier
                    .isStatic(field.getModifiers())) {
                continue;
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            fields.add(field);
        }
        return new ObjectParamMetadata(fields);
    }

    static ObjectParamMetadata getMetadata(Class<?> objectType) {
        ObjectParamMetadata metadata = classToMetadata.get(objectType);
        if (metadata == null) {
            metadata = ObjectParamMetadata.parseObjectType(objectType);
            classToMetadata.put(objectType, metadata);
        }
        return metadata;
    }
}
