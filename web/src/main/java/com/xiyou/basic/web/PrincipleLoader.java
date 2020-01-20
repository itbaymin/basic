package com.xiyou.basic.web;

/**
 * Created by baiyc
 * 2020/1/20/020 17:33
 * Description：
 */
public interface PrincipleLoader {
    /**
     * 如果实现方如果根据id获取到具体用户对象失败, 不能返回null, 需要提供一个返回相同id值的Principle实例。 否则业务层无法正常往下执行
     * @param id
     * @return
     */
    Principle load(String id);

    PrincipleLoader DEFAULT_PRINCIPLE_LOADER = (id) -> (Principle) () -> id;
}
