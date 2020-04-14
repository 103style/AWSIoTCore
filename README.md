# AWSIOTCore

SDK: [aws-sdk-android](https://github.com/aws-amplify/aws-sdk-android/)

Samples：[aws-sdk-android-samples](https://github.com/awslabs/aws-sdk-android-samples)

基于 亚马逊 [经过开发人员验证的身份 (身份池)](https://docs.aws.amazon.com/zh_cn/cognito/latest/developerguide/developer-authenticated-identities.html) 的使用


##  使用示例

目前iotcore里面添加的依赖版本：
```
iotcore/build.gradle
dependencies {
    def aws_version = "2.16.+"
    //目前为了兼容Android7以下 替换为修正Android 7 以下报错的jar
    //目前 mqttv3 以合入 还未发新版本   请跟踪下面的连接 新版本的替换
    // https://github.com/aws-amplify/aws-sdk-android/issues/1259
    api("com.amazonaws:aws-android-sdk-iot:$aws_version") {
        exclude module: 'org.eclipse.paho.client.mqttv3'
    }
    implementation "org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.3"
    api "com.amazonaws:aws-android-sdk-mobile-client:$aws_version"
}
```

### 添加依赖
```
//project.gradle
allprojects {
    repositories {
        //....
       
        maven { url "https://jitpack.io" }
       
    }
}
```

```
//app/build.gradle
api 'com.github.103style:AWSIoTCore:1.0.2'
```

###  连接

连接步骤：

* 通过 服务端接口 获取 `cognito_token` 和 `identity_id`。
* 通过 服务端接口 获取 `poolId`,`endponit`,`region`
* 创建 `IoTCoreManager`实例，开始连接。
  ```java
  private void initIoTCore(Context context, String cognitoToken, String identityId) {
      if (ioTCoreManager == null) {
          ioTCoreManager = new IoTCoreManager();
          
          //在连接之前配置 poolId,endponit,region
          //ioTCoreManager.config(poolId,endponit,region);
          
          //开启订阅超时
          ioTCoreManager.setOpenSubscribeTimeout(true);
          
          //配置订阅等待超时时间 默认30s
          //ioTCoreManager.setSubscribeTimeout(30);
      }
      ioTCoreManager.connect(context, cognitoToken, identityId, this);
  }

  @Override
  public void mqttConnectException(Throwable throwable) {
      //连接过程出现异常的回调
      Logg.e(TAG, "mqttConnectException---" + throwable.getMessage());
  }

  @Override
  public void mqttConnectStateChange(boolean success, String s) {
      //mqtt连接状态的该表  success标识mqtt连接是否成功
      mqttConnectSuccess = success;
  }
  ```


### 订阅发布
[MainActivity.java](https://github.com/103style/AWSIoTCore/blob/master/app/src/main/java/com/lxk/libiotcore/MainActivity.java)
```
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
```

---


## IoTCore参数配置
### 订阅超时时间
* 类似http请求的超时时间的 订阅超时配置。**默认30s，需要在订阅之前调用**。
```
ioTCoreManager = new IoTCoreManager();
ioTCoreManager.setSubscribeTimeout(30);
```

### 开启订阅超时
```
ioTCoreManager = new IoTCoreManager();
ioTCoreManager.setOpenSubscribeTimeout(true);
```
