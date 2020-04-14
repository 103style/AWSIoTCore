[TOC]

# IOTCore使用文档

##  使用示例
###  连接

连接步骤：

* 通过 服务端接口 获取 `cognito_token` 和 `identity_id`。
* 通过 服务端接口 获取 `poolId`,`endponit`,`region`
* 创建 `IoTCoreManager`实例，开始连接。
  ```java
  private void initIoTCore(Context context, IoTBean ioTBean) {
      if (ioTCoreManager == null) {
          ioTCoreManager = new IoTCoreManager();
          
          //在连接之前配置 poolId,endponit,region
          //ioTCoreManager.config(poolId,endponit,region);
          
          //开启订阅超时
          ioTCoreManager.setOpenSubscribeTimeout(true);
          
          //配置订阅等待超时时间 默认30s
          //ioTCoreManager.setSubscribeTimeout(30);
      }
      ioTCoreManager.connect(context, ioTBean.cognito_token, ioTBean.identity_id, this);
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