package com.lxk.libiotcore;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.lxk.iotcore.IoTCoreManager;
import com.lxk.iotcore.callback.IotCoreSubscribeCallback;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author https://github.com/103style
 * @date 2020/4/14 17:07
 */
public class Exchange {

    private IoTCoreManager ioTCoreManager;

    public void setIoTCoreManager(IoTCoreManager ioTCoreManager) {
        this.ioTCoreManager = ioTCoreManager;
    }

    public void subscribe(String topic, HttpCallback callback) {
        subscribeAndPublish(topic, "", "", callback);
    }


    public void subscribeAndPublish(String subscribeTopic, String publishTopic, String msg,
                                    HttpCallback callback) {
        if (ioTCoreManager == null) {
            throw new NullPointerException("please init IoTCoreManager first");
        }
        ioTCoreManager.subscribe(subscribeTopic,
                new IotCoreSubscribeCallback() {
                    @Override
                    public void subscribeCallback(boolean success, Throwable throwable) {
                        if (!success) {
                            callback.appError(throwable);
                        } else {
                            if (!TextUtils.isEmpty(publishTopic)) {
                                publish(publishTopic, msg, callback);
                            }
                        }
                    }

                    @Override
                    public void onMessageArrived(String msg) {
                        callback.successByValue(toJsonObject(msg, callback));
                    }

                    @Override
                    public void subscribeTimeout() {
                        callback.appError(new RuntimeException("timeout"));
                    }

                    @Override
                    public void finish() {
                        callback.finish();
                    }
                });
    }


    public void publish(String topic, String msg, HttpCallback<?> callback) {
        if (ioTCoreManager == null) {
            throw new NullPointerException("please init IoTCoreManager first");
        }
        ioTCoreManager.publish(topic, msg,
                (success, throwable) -> {
                    if (!success) {
                        callback.appError(throwable);
                    }
                });
    }


    private <T> T toJsonObject(String result, HttpCallback<T> callback) {
        Type[] types = callback.getClass().getGenericInterfaces();
        Type param = types[0];
        Type res = Object.class;
        if (param instanceof ParameterizedType) {
            Type[] type = ((ParameterizedType) param).getActualTypeArguments();
            res = type[0];
        }
        return JSONObject.parseObject(result, res);
    }

    public void release() {
        if (ioTCoreManager != null) {
            ioTCoreManager.disconnect();
        }
    }
}
