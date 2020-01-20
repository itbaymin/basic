package com.xiyou.basic.remote;

/**
 * Created by baiyc
 * 2020/1/19/019 20:15
 * Description：响应抽象
 */
public abstract class AbstractHttpResult {


    /**
     * @return
     */
    public final boolean isSuccess() {
        return isBizSuccess();
    }

    /**
     * 业务意义上的是否成功. 默认为true, 具体业务结果模型自行重写判定业务是否成功
     *
     * @return
     * @author dongt
     * @date 2016年7月29日 下午1:56:35
     */
    protected abstract boolean isBizSuccess();
}
