package com.lxk.iotcore;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.lxk.iotcore.callback.IotCoreSubscribeCallback;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author https://github.com/103style
 * @date 2020/4/14 11:12
 * <p>
 * 订阅超时处理类
 */
public class IoTSubscribeTimeoutHandler {

    private static final String TAG = "SubscribeTimeoutHandlerThread";

    private volatile static IoTSubscribeTimeoutHandler instance;

    /**
     * 超时相关
     */
    private static int timeout = 30;
    private HandlerThread handlerThread;
    private Handler handler;
    /**
     * 超时消息标识
     */
    private volatile int index;
    /**
     * 保存topic 和对应的 超时消息标识
     */
    private ConcurrentHashMap<String, Integer> hashMap;

    private IoTSubscribeTimeoutHandler() {
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        hashMap = new ConcurrentHashMap<>();
    }

    /**
     * 配置超时时间(s)
     *
     * @param timeout 超时时间(s)
     */
    public static void setTimeout(int timeout) {
        IoTSubscribeTimeoutHandler.timeout = timeout;
    }

    /**
     * 获取单例
     */
    public static IoTSubscribeTimeoutHandler getInstance() {
        if (instance == null) {
            synchronized (IoTSubscribeTimeoutHandler.class) {
                if (instance == null) {
                    instance = new IoTSubscribeTimeoutHandler();
                }
            }
        }
        return instance;
    }

    public synchronized void sendTimeoutMsg(@NonNull String topic, @NonNull IotCoreSubscribeCallback callback) {
        index++;
        Message message = Message.obtain(handler, () -> {
            IoTCoreLogger.e("IoTSubscribeTimeoutHandler timeout msg callback,  topic =" + topic);
            callback.subscribeTimeout();
            callback.finish();
        });
        message.what = index;
        hashMap.put(topic, index);
        handler.sendMessageDelayed(message, timeout * 1000);
        IoTCoreLogger.e("IoTSubscribeTimeoutHandler  sendTimeoutMsg,  topic =" + topic);
    }

    public void removeTimeoutMsg(String topic) {
        Integer what = hashMap.get(topic);
        if (what != null) {
            handler.removeMessages(what);
            IoTCoreLogger.e("IoTSubscribeTimeoutHandler  removeTimeoutMsg,  topic =" + topic);
        } else {
            IoTCoreLogger.e("IoTSubscribeTimeoutHandler  removeTimeoutMsg,  map.get(topic) = null");
        }
    }
}
