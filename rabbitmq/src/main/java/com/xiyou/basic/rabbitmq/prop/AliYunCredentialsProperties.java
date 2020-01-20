package com.xiyou.basic.rabbitmq.prop;

import com.alibaba.mq.amqp.utils.UserUtils;
import com.rabbitmq.client.impl.CredentialsProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by baiyc
 * 2020/1/20/020 16:39
 * Description：属性
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "aliyun.rabbitmq.credential")
public class AliYunCredentialsProperties implements CredentialsProvider {
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String resourceOwnerId;

    @Override
    public String getUsername() {
        if(StringUtils.isEmpty(securityToken)) {
            return UserUtils.getUserName(accessKeyId, resourceOwnerId);
        } else {
            return UserUtils.getUserName(accessKeyId, resourceOwnerId, securityToken);
        }
    }

    @Override
    public String getPassword() {
        try {
            return UserUtils.getPassord(accessKeySecret);
        } catch (InvalidKeyException | NoSuchAlgorithmException e1) {
            log.error("消息队列获取密码失败", e1);
        }
        return null;
    }
}
