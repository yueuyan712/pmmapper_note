## pm mapper代码阅读

### src/main/java

#### config

yaml：专门用来写配置文件的语言

#### datarouter

- EventReceiver 定义一个接口，实现该接口的类必须实receive方法，可接收从DR订户收倒的事件。
- DeliveryHandler继承了ServerResouce类(model中)。

#### model

https://wiki.onap.org/display/DW/VES+event+enrichment+for+DCAE+mS

##### ServerResouce.java  

其中描述了ServerResource类，包含的属性有访问该资源的方法和端点。可在创建时自定义这俩属性，也可不定义，那就用库里默认的。该类implements了库里的HttpHandler接口。

##### EventMetaData.java

事件元数据类。其中包括一些字符串属性，如产品名、厂商名等等

```java
public class EventMetadata { // 事件元数据
    @GSONRequired
    private String productName;
    @GSONRequired
    private String vendorName;
    @GSONRequired
    private String startEpochMicrosec; // 开始时期微秒
    @GSONRequired
    private String lastEpochMicrosec; // 结束实其微秒，是一个时间戳
    @GSONRequired
    private String sourceName;
    @GSONRequired
    private String timeZoneOffset; // 时域偏移，格林威治时间和本地时间之间的时差？猜测
    @GSONRequired
    private String location; // 地理位置
    @GSONRequired
    private String compression; // 压缩
    @GSONRequired
    private String fileFormatType; // 文件格式类型
    @GSONRequired
    private String fileFormatVersion; // 文件格式版本
}
```

### util

##### RequiredFieldDeserializer

自定义一个 TypeAdapter,用来处理 T类型的数据

在DeliveryHandler.java中是用来处理EventMeta数据

从Json相关对象到Java实体，另深层检验该对象的类的fileds中每一项，有GSONRequired注解的那项，有没有值，没有值要抛出异常。如果该对象为list递归遍历，或fields中某项为对象，也要往下深层递归遍历。

TypeAdapter的使用需要注册到 Gson 的实例上。new GsonBuilder().registerTypeAdapter(类型,TypeAdapter实例).create();