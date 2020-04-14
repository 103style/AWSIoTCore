package com.lxk.libiotcore;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.lxk.iotcore.IoTCoreManager;
import com.lxk.iotcore.callback.IotCoreConnectCallback;
import com.lxk.iotcore.callback.IotCoreSubscribeCallback;

/**
 * @author https://github.com/103style
 * @date 2020/4/9 10:03
 */
public class MainActivity extends AppCompatActivity implements IotCoreConnectCallback {

    private static final String TAG = "MainActivity";
    IoTCoreManager ioTCoreManager;

    boolean connectSuccess;


    private String poolId = "REPLACE_ME";
    private String iotEndpoint = "REPLACE_ME";
    private String region = "REPLACE_ME";
    private String token = "REPLACE_ME";
    private String identityId = "REPLACE_ME";


    private String subscribeTopic = "REPLACE_ME";
    private String publishTopic = "REPLACE_ME";
    private String publishMsg = "REPLACE_ME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ioTCoreManager = new IoTCoreManager();
        findViewById(R.id.connect)
                .setOnClickListener(v -> {
                    ioTCoreManager.config(poolId, iotEndpoint, region);
                    //开启订阅超时
                    ioTCoreManager.setOpenSubscribeTimeout(true);
                    //设置订阅超时时间  默认30s
                    ioTCoreManager.setSubscribeTimeout(30);
                    ioTCoreManager.connect(MainActivity.this,
                            token,
                            identityId,
                            MainActivity.this);
                });

        findViewById(R.id.subscribe).setOnClickListener(v -> {
            subscribeTest();
        });
        findViewById(R.id.publish).setOnClickListener(v -> {
            publishTest();
        });
        findViewById(R.id.unsubscribe).setOnClickListener(v -> {
            unsubscribeTest();
        });
    }


    @Override
    public void mqttConnectException(Throwable throwable) {
        Log.e(TAG, "mqttConnectException: " + throwable.getMessage());
    }

    @Override
    public void mqttConnectStateChange(boolean connectSuccess, String status) {
        Log.e(TAG, "mqttConnectStateChange: status = " + status + ",  connectSuccess = " + connectSuccess);
        this.connectSuccess = connectSuccess;
    }


    private void publishTest() {
        if (!connectSuccess) {
            Log.e(TAG, "publishTest: not connected");
        }
        ioTCoreManager.publish(publishTopic, publishMsg,
                (success, throwable) -> {
                    Log.e(TAG, "publishTest: success = " + success + ", \n throwable = " + throwable);
                });
    }

    private void subscribeTest() {
        if (!connectSuccess) {
            Log.e(TAG, "subscribeTest: not connected");
        }
        ioTCoreManager.subscribe(subscribeTopic,
                new IotCoreSubscribeCallback() {
                    @Override
                    public void subscribeCallback(boolean success, Throwable throwable) {
                        Log.e(TAG, "subscribeTest: subscribeCallback success = " + success + ", \n throwable = " + throwable);
                    }

                    @Override
                    public void onMessageArrived(String msg) {
                        Log.e(TAG, "subscribeTest: onMessageArrived msg = " + msg);
                    }

                    @Override
                    public void subscribeTimeout() {
                        Log.e(TAG, "subscribeTest: subscribeTimeout  topic = " + subscribeTopic);
                    }

                    @Override
                    public void finish() {
                        Log.e(TAG, "subscribeTest: subscribeTimeout  topic = " + subscribeTopic);
                    }
                });
    }


    private void unsubscribeTest() {
        if (!connectSuccess) {
            Log.e(TAG, "subscribeTest: not connected");
        }
        ioTCoreManager.unsubscribe(subscribeTopic);
    }

}
