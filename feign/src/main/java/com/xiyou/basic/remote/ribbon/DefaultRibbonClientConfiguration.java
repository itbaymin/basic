package com.xiyou.basic.remote.ribbon;

import com.ecwid.consul.v1.ConsulClient;
import com.netflix.client.IClientConfigAware;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.consul.discovery.*;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

import static com.netflix.client.config.CommonClientConfigKey.DeploymentContextBasedVipAddresses;
import static com.netflix.client.config.CommonClientConfigKey.EnableZoneAffinity;

/**
 * Created by baiyc
 * 2020/1/19/019 20:16
 * Description：spring.cloud.consul.ribbon.enabled: false 禁用RibbonConsulAutoConfiguration
 * 为了综合 服务发现 和 基于配置 的服务列表
 * 相比较于 ConsulRibbonClientConfiguration
 * 全局为ConsulServerList， 但如果ribbon客户端有指定的serverList则使用ribbon客户端指定的
 */
public class DefaultRibbonClientConfiguration {
    protected static final String VALUE_NOT_SET = "__not__set__";

    protected static final String DEFAULT_NAMESPACE = "ribbon";

    @Autowired
    private ConsulClient client;

    @Value("${ribbon.client.name}")
    private String serviceId = "client";

    @Autowired
    private PropertiesFactory propertiesFactory;

    public DefaultRibbonClientConfiguration() {
    }

    public DefaultRibbonClientConfiguration(String serviceId) {
        this.serviceId = serviceId;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config,
                                          ConsulDiscoveryProperties properties) {

        if (this.propertiesFactory.isSet(ServerList.class, serviceId)) {
            ServerList sl = this.propertiesFactory.get(ServerList.class, config, serviceId);
            if (IClientConfigAware.class.isAssignableFrom(sl.getClass())) {
                ((IClientConfigAware)sl).initWithNiwsConfig(config);
            }
            return sl;
        }
        ConsulServerList serverList = new ConsulServerList(this.client, properties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerListFilter<Server> ribbonServerListFilter() {
        return new HealthServiceServerListFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing() {
        return new ConsulPing();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsulServerIntrospector serverIntrospector() {
        return new ConsulServerIntrospector();
    }

    @PostConstruct
    public void preprocess() {
        setProp(this.serviceId, DeploymentContextBasedVipAddresses.key(), this.serviceId);
        setProp(this.serviceId, EnableZoneAffinity.key(), "true");
    }

    protected void setProp(String serviceId, String suffix, String value) {
        // how to set the namespace properly?
        String key = getKey(serviceId, suffix);
        DynamicStringProperty property = getProperty(key);
        if (property.get().equals(VALUE_NOT_SET)) {
            ConfigurationManager.getConfigInstance().setProperty(key, value);
        }
    }

    protected DynamicStringProperty getProperty(String key) {
        return DynamicPropertyFactory.getInstance().getStringProperty(key, VALUE_NOT_SET);
    }

    protected String getKey(String serviceId, String suffix) {
        return serviceId + "." + DEFAULT_NAMESPACE + "." + suffix;
    }
}
