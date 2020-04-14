package com.lxk.iotcore.callback;

/**
 * @author https://github.com/103style
 * @date 2020/4/14 16:58
 * <p>
 * 订阅回调
 */
public interface IotCoreSubscribeCallback {

    /**
     * 订阅主题回调
     * <p>
     * 回调运行在主线程
     *
     * @param throwable 错误信息
     * @param success   订阅是否成功
     */
    void subscribeCallback(boolean success, Throwable throwable);

    /**
     * 订阅消息到达
     * <p>
     * 回调运行在主线程
     *
     * @param msg 接受到的信息
     */
    void onMessageArrived(String msg);

    /**
     * 订阅超时
     */
    void subscribeTimeout();

    /**
     * 订阅结束
     */
    void finish();
}
