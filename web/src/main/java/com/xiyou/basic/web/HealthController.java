package com.xiyou.basic.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by baiyc
 * 2020/1/20/020 17:31
 * Description：健康监测
 */
@RestController
public class HealthController {

    @RequestMapping("/actuator/health")
    public String health() {
        return "ok";
    }
}