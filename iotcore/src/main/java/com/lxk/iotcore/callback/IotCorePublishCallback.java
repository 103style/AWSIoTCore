package com.lxk.iotcore.callback;

import androidx.annotation.Nullable;


/**
 * @author https://github.com/103style
 * @date 2020/4/14 10:06
 * <p>
 * 发布回调
 */
public interface IotCorePublishCallback {

    /**
     * 发布主题的回调
     * <p>
     * 回调运行在主线程
     *
     * @param success   发布是否成功
     * @param throwable 错误信息
     */
    void publishCallback(boolean success, @Nullable Throwable throwable);
}
