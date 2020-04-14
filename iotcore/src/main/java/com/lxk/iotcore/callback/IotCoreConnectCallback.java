package com.lxk.iotcore.callback;

/**
 * @author https://github.com/103style
 * @date 2020/4/13 17:13
 * <p>
 * 连接回调
 */
public interface IotCoreConnectCallback {
    /**
     * mqtt连接报错
     * <p>
     * 回调运行在主线程
     *
     * @param throwable 错误信息
     */
    void mqttConnectException(Throwable throwable);

    /**
     * mqtt连接状态改变
     * <p>
     * 回调运行在主线程
     *
     * @param connected 是否连接成功
     * @param status    当前状态
     */
    void mqttConnectStateChange(boolean connected, String status);


}
