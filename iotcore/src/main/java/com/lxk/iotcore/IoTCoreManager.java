package com.lxk.iotcore;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttSubscriptionStatusCallback;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.amazonaws.util.StringUtils;
import com.lxk.iotcore.callback.IotCoreConnectCallback;
import com.lxk.iotcore.callback.IotCorePublishCallback;
import com.lxk.iotcore.callback.IotCoreSubscribeCallback;
import com.lxk.iotcore.exception.IoTAuthException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * @author https://github.com/103style
 * @date 2020/4/9 10:12
 */
public class IoTCoreManager {

    private String poolId;

    private String iotEndpoint;

    private Regions REGION;

    private AWSIotMqttManager mqttManager;

    private Context mContext;

    private boolean openSubscribeTimeout;

    /**
     * identityId token 连接
     */
    private CognitoCachingCredentialsProvider credentialsProvider;

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 配置aws参数
     *
     * @param poolId     连接池id
     * @param endPoint   连接地址
     * @param regionName 区域名称
     */
    public void config(String poolId, String endPoint, String regionName) {
        this.poolId = poolId;
        this.iotEndpoint = endPoint;
        this.REGION = Regions.fromName(regionName);
    }

    /**
     * 是否开启订阅超时
     */
    public void setOpenSubscribeTimeout(boolean open) {
        openSubscribeTimeout = open;
    }

    public void setSubscribeTimeout(int subscribeTimeout) {
        IoTSubscribeTimeoutHandler.setTimeout(subscribeTimeout);
    }

    /**
     * 验证 token id 通过则开始连接
     *
     * @param token           token
     * @param identityId      id
     * @param connectCallback 连接回调
     */
    public void connect(Context context, String token, String identityId, IotCoreConnectCallback connectCallback) {
        if (TextUtils.isEmpty(poolId) || TextUtils.isEmpty(iotEndpoint) || REGION == null) {
            throw new NullPointerException("please call IoTCoreManager.config  before IoTCoreManager.connect");
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            //如果在主线程调用 则 切换到子线程  有校验token和 id 的网络请求
            new Thread(() -> connect(context, token, identityId, connectCallback)).start();
            return;
        }

        IoTCoreLogger.i("IoTCoreManager init \n ---------token = " + token + " \n ------ identityId = " + token);
        mContext = context.getApplicationContext();
        try {
            //配置token 和 identityId 对应的 CognitoCachingCredentialsProvider
            AuthenticationProvider authenticationProvider = new AuthenticationProvider(poolId, REGION, token, identityId);
            credentialsProvider = new CognitoCachingCredentialsProvider(mContext, authenticationProvider, REGION);
            HashMap<String, String> loginsMap = new HashMap<>();
            loginsMap.put(authenticationProvider.getProviderName(), authenticationProvider.getIdentityId());
            credentialsProvider.setLogins(loginsMap);
            credentialsProvider.refresh();

            connect(connectCallback);
        } catch (NotAuthorizedException exception) {
            IoTCoreLogger.e(exception.getMessage());
            if (connectCallback != null) {
                connectCallback.mqttConnectException(new IoTAuthException(exception));
            }
        }
    }


    /**
     * 断开连接
     */
    public void disconnect() {
        IoTCoreLogger.i("disconnect...");
        try {
            if (mqttManager != null) {
                mqttManager.disconnect();
            }
        } catch (Exception e) {
            IoTCoreLogger.e("Disconnect error.");
            IoTCoreLogger.e(e);
        }
    }

    /**
     * 连接
     *
     * @param iotCoreConnectCallback 连接回调
     */
    private void connect(IotCoreConnectCallback iotCoreConnectCallback) {
        try {
            IoTCoreLogger.i("---------IoTCoreManager connect credentialsProvider.getIdentityId = " + credentialsProvider.getIdentityId());
            IoTCoreLogger.i("---------IoTCoreManager connect credentialsProvider.getToken = " + credentialsProvider.getToken());
            if (mqttManager == null) {
                //创建mqtt管理类
                mqttManager = new AWSIotMqttManager(credentialsProvider.getIdentityId(), iotEndpoint);
                mqttManager.setKeepAlive(10);
            }
            mqttManager.connect(
                    credentialsProvider,
                    (status, throwable) -> {
                        IoTCoreLogger.i("Status = " + status);
                        String statusString = status == null ? "status is null!" : status.toString();
                        runOnUiThread(() -> {
                            if (iotCoreConnectCallback != null && status != null) {
                                iotCoreConnectCallback.mqttConnectStateChange(
                                        status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected,
                                        statusString);
                            }
                            if (throwable != null) {
                                IoTCoreLogger.e("Connection error.");
                                IoTCoreLogger.e(throwable);
                                if (iotCoreConnectCallback != null) {
                                    iotCoreConnectCallback.mqttConnectException(throwable);
                                }
                            }
                        });
                    });
        } catch (final Exception e) {
            IoTCoreLogger.e("Connection error.");
            IoTCoreLogger.e(e);
            runOnUiThread(() -> {
                if (iotCoreConnectCallback != null) {
                    iotCoreConnectCallback.mqttConnectException(e);
                }
            });
        }
    }

    /**
     * 解除订阅
     *
     * @param topic 主题
     */
    public void unsubscribe(String topic) {
        IoTCoreLogger.i("unsubscribe topic = " + topic);
        try {
            mqttManager.unsubscribeTopic(topic);
        } catch (Exception e) {
            IoTCoreLogger.e("unsubscribe error.");
            IoTCoreLogger.e(e);
        }
    }

    /**
     * 订阅
     * <p>
     * 断开连接重连会自动重新订阅
     *
     * @param topic 主题
     */
    public void subscribe(String topic, IotCoreSubscribeCallback callback) {
        IoTCoreLogger.i("subscribe topic = " + topic);
        try {
            mqttManager.subscribeToTopic(
                    topic,
                    AWSIotMqttQos.QOS0,
                    //订阅状态的回调
                    new AWSIotMqttSubscriptionStatusCallback() {
                        @Override
                        public void onSuccess() {
                            IoTCoreLogger.e("subscribeToTopic \"" + topic + "\" success");
                            runOnUiThread(() -> {
                                if (callback != null) {
                                    callback.subscribeCallback(true, null);
                                }
                            });
                            sendTimeoutMsg(topic, callback);
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            IoTCoreLogger.e("subscribeToTopic  \"" + topic + " \" failure");
                            IoTCoreLogger.e(exception);
                            runOnUiThread(() -> {
                                if (callback != null) {
                                    callback.subscribeCallback(false, exception);
                                    callback.finish();
                                }
                            });
                        }
                    },
                    //有订阅的消息过来
                    (topic1, data) -> {
                        String message = new String(data, StandardCharsets.UTF_8);
                        IoTCoreLogger.i("Message arrived: \n ---------topic = " + topic1 + ",\n ---------message = " + message);
                        removeTimeoutMsg(topic);
                        runOnUiThread(() -> {
                            if (callback != null && topic.equals(topic1)) {
                                callback.onMessageArrived(message);
                                callback.finish();
                            }
                        });
                    }
            );
        } catch (Exception e) {
            IoTCoreLogger.e("Subscription error.");
            IoTCoreLogger.e(e);
            runOnUiThread(() -> {
                if (callback != null) {
                    callback.subscribeCallback(false, e);
                    callback.finish();
                }
            });
        }
    }

    private void removeTimeoutMsg(String topic) {
        if (openSubscribeTimeout) {
            IoTSubscribeTimeoutHandler.getInstance().removeTimeoutMsg(topic);
        }
    }

    /**
     * 发送延时超时消息
     *
     * @param topic    订阅主题
     * @param callback 订阅回调
     */
    private void sendTimeoutMsg(String topic, IotCoreSubscribeCallback callback) {
        if (openSubscribeTimeout) {
            IoTSubscribeTimeoutHandler.getInstance().sendTimeoutMsg(topic, callback);
        }
    }

    /**
     * 发布消息
     *
     * @param topic 主题
     * @param msg   消息内容
     */
    public void publish(String topic, String msg, IotCorePublishCallback callback) {
        IoTCoreLogger.i("publish topic = " + topic + ",---- msg = " + msg);
        try {
            mqttManager.publishData(
                    msg.getBytes(StringUtils.UTF8),
                    topic,
                    AWSIotMqttQos.QOS0,
                    (status, userData) -> {
                        if (callback != null) {
                            callback.publishCallback(
                                    status == AWSIotMqttMessageDeliveryCallback.MessageDeliveryStatus.Success,
                                    null);
                        }
                    },
                    null);
        } catch (Exception e) {
            IoTCoreLogger.e("Publish error.");
            IoTCoreLogger.e(e);
            if (callback != null) {
                callback.publishCallback(false, e);
            }
        }
    }
}
