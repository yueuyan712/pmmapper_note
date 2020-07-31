## pm mapper代码阅读

### 定义

PM Performance Management/Performance Monitoring

PM文件应该就是性能监控文件

PM mapper的作用即将PM文件转为VES（VNF流事件），即ves格式的json字符串

PM需要订阅Data Router，接收来自DR的事件

### src/main/java

#### config

yaml：专门用来写配置文件的语言

#### datarouter

##### 1. EventReceiver.java（接收Event实例）

 定义一个接口，实现该接口的类必须实现receive方法:接收从DR订户收到的Event。

##### 2. DeliveryHandler.java（从http请求中提取信息并创建Event实例，交给EventReceiver，失败返回响应）

该类继承了ServerResouce类(model中)，看作一个分发处理器，处理http请求。

该类有以下属性：

-  METADATA_HEADER元数据头（字符串），常量X-DMAAP-DR-META

- PUB_ID_HEADER发布id头（字符串），常量X-DMAAP-DR-PUBLISH-ID

- logger（ONAPLogAdapter实例），即为DeliveryHandler初始化日志对象，方便在日志输出的时候，可以打印出日志信息所在类

- BAD_METADATA_MESSAGE元数据错误信息（字符串）

- NO_METADATA_MESSAGE无元数据信息（字符串）

- HTTP_METHOD方法（字符串），常量库里的PUT_STRING

- DELIVERY_ENDPOINT发布端点（字符串），常量"/delivery/{filename}"

- metadataBuilder（Gson实例）

  > Gson 是 Google 提供的用来在 Java 对象和 JSON 数据之间进行映射的 Java 类库。可以将一个 JSON 字符串转成一个 Java 对象，或者反过来。

- eventReceiver（EventReceiver对象）

  > 该接口上面描述过，只有一个receive方法，用来接收Event的，接口内没有具体的实现，只说明有这个方法。

该类中有以下方法：

- 构造函数DeliveryHandler，传参为EventReceiver类数据。

  > 调用父类serverresource的构造方法，此父类属性有可访问资源的http方法和端点。方法有获得这俩属性或获得handler本身。
  >
  > 将传参EventReceiver赋值给类自己的EventReceiver
  >
  > 给metadataBuilder赋值，新建GsonBuilder实例，并为其注册自定义的TypeAdapter用来转换EventMetadata类型，该自定义适配器为RequiredFieldDeserializer类。这里仅仅是注册上去并非使用。metadataBuilder可用于EventMetadata和json的互相转换及反序列化啥的，只需调用里面的方法即可，之前注册的自定义适配器还重写了反序列化方法，fromjson没有重写。

- getMetadata，传参类型为HttpServerExchange。从http请求的请求头里获取元数据头字符串，并将其转为EventMetadata类型

- handleRequest，传参类型为HttpServerExchange，该方法用于处理请求。即从头里取publishIdentity、metadata、从receiver里取callbackExchange和body，再取mdc。取完把这一堆作为传参新建Event实例给eventReceiver。

  > - 首先会给logger输入一条信息，把http请求包装以下放进去，包装成HttpServerExchangeAdapter类。（util里）里面包含请求的客户端地址、头、url、主机地址。
  > - metadata（EventMetadata类），使用getMetadata方法取metadata
  >
  > - 映射mdc，把当前线程的context制作一份副本给mdc
  >
  > - 字符串publishIdentity，从请求头里取出PUB_ID_HEADER
  >
  > - 然后从请求中大概是获得主体？看不懂，从里面获得callbackExchange和body，再加上metadata、mdc、publishIdentity一起创建Event实例，并将该实例作为传参给eventReceiver.receive。【eventReceiver的reveive方法不是没实现嘛？？？？？】
  > - 以上所有都再try里，下面catch异常。如果NoMetadataException，在logger里记录一条信息，并为http请求设置状态码为StatusCodes.BAD_REQUEST，再发送一个NO_METADATA_MESSAGE的response
  > - 如果是JsonParseException，差不多。
  >
  > 

#### model（重要，各种类型）

https://wiki.onap.org/display/DW/VES+event+enrichment+for+DCAE+mS

##### 1. ServerResouce.java  

可看作服务资源处理器

其中描述了ServerResource类，包含的属性有：访问该资源的http方法(post还是get)和端点。

可在创建时自定义这俩属性，也可仅定义端点，那方法默认使用get。

该类实现HttpHandler接口，对外暴露http方法、端点及handler本身

##### 2. EventMetaData.java

事件元数据类。其中包括一些字符串属性，如产品名、厂商名等等

```java
public class EventMetadata { // 事件元数据
    @GSONRequired
    private String productName; // 产品名
    @GSONRequired
    private String vendorName; // 厂商名
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

##### Event

事件，其中有HttpServerExchange（应该是请求）、body、事件元数据、mdc映射、发布标识符、测量Measurement文件、过滤器、ves



### util

##### RequiredFieldDeserializer.java

自定义一个 TypeAdapter,用来处理 T类型的数据

（在DeliveryHandler.java中是用来处理EventMeta数据）

从Json相关对象到Java实体，另深层检验该对象的类的fileds中每一项，有GSONRequired注解的那项，有没有值，没有值要抛出异常。如果该对象为list递归遍历，或fields中某项为对象，也要往下深层递归遍历。

TypeAdapter的使用需要注册到 Gson 的实例上。new GsonBuilder().registerTypeAdapter(类型,TypeAdapter实例).create();

##### HttpServerExchangeAdapter.java

获得request中的各种信息，url啥的。就是把request包进这个类里，在DeliveryHandler.java中将包装完的放入log里。