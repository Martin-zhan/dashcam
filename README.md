# Dashcam
基于Kafka, HBase, Elasticsearch中央日志系统。

1. 支持Java, NodeJs
2. 快速检索
3. Trace和自动trace调用链路

LogAgent 使用说明:
### 添加Maven依赖
```
<dependency>
    <groupId>org.mokey.acupple</groupId>
    <artifactId>dashcam-agent</artifactId>
    <version>1.0.1.RELEASE</version>
</dependency>
```
### 在Resources目录下面添加dashcam-common.properties
```
#必要，应用唯一标识，不可随意乱写。
dashcam.common.appName=9096123
#Kafka broker地址               
dashcam.common.config.kafka-brokers=localhost:9092
dashcam.common.config.level=INFO
dashcam.common.config.enable=true
dashcam.common.config.trace-enable=true
#单位KB，每条日志的大小限制
dashcam.common.config.max-message-size=32
#日志打包发送的默认大小
dashcam.common.config.chunk-size = 50
```
### 实例代码
1. 普通日志:  类似传统日志（如log4j）的记录方式，不同的是，除message以外还可以记录title、tag等附加信息，并可以在DashcamUI中按照这些信息检索
```
public static void main(String[] args){
    ILog logger = LogManager.getLogger(LocalLogTest.class);     //实例化一个logger
    logger.info("test message");          //仅记录message
    logger.debug("debug", "not record");    //记录title和message
    try{
        int i = 10/0;
    }catch (Exception e){
        logger.error(e);
    }
 
    //附带多个tag信息，方便检索和后期数据分析（直接传Map的方式）
    Map<String, String> tags = new HashMap<String, String>();
    tags.put("app1", "master");
    tags.put("app2", "host");
    logger.info("title","This is a log with tag for test", tags);
     
    //使用TagBuilder辅助类，以流式编程的方式创建Tags
    logger.info("title", "this is a test message",
        TagBuilder.create().append("tag1", "value1")
            .append("tag2", 100)//key-value的形式，其中value支持object类型，不强制用String
            .append("tag4=value4,tag5=value5")//可以按照key=value,key=value的形式，一次append多个tag
            .build());
    System.exit(0);
}
```
2. Trace日志： 用于程序的调用关系追踪。每个需要追踪的段落定义为一个Span，通过在代码段、方法或服务的起始、结束位置，设置Span的start、stop，即可搜集到这个Span的相关信息。Span之间可以嵌套，从而生成一个调用树。
```
private static ITrace trace = TraceManager.getTracer(TraceTest.class); //创建一个trace实例
 
@Test
public void test(){
    ISpan span = trace.startSpan("spanName", "traceTest", SpanType.WEB_SERVICE); //创建一个span实例，并开始这个span
    trace.log(LogType.APP, LogLevel.INFO, "this is traceTest message"); //写trace日志
 
    try{
        Thread.sleep(1000);
        service1(); //模拟方法调用
    }catch (Exception e){
        trace.log(LogType.APP, LogLevel.ERROR, "trace test exception", e);
    }finally {
        span.stop(); //关闭span
        trace.clear(); //清理trace树中所有未主动stop的span，保证trace信息完整，一般在整个trace过程中最末尾处调用一次即可
    }
}
 
private void service1(){
    ISpan span = trace.startSpan("service1_span", "service1",SpanType.WEB_SERVICE);
    try{
        trace.log(LogType.APP, LogLevel.INFO, "service1 message");
        service2(); //模拟方法调用
        Thread.sleep(1000);
    }catch (Exception e){
        trace.log(LogType.APP, LogLevel.ERROR, "service1 failed", e);
    }
    finally {
        span.stop();
    }
}
 
private void service2(){
    final ISpan span = trace.startSpan("service2_span", "service2", SpanType.WEB_SERVICE);
    try{
        trace.log(LogType.APP, LogLevel.INFO, "service2 message");
        Thread thread = new Thread(){
            @Override
            public void run() {
                service3(span.getTraceId(), span.getSpanId()); //模拟远程方法调用，需要传递traceId和spanId给远程方法
            }
        };
        thread.start();
        thread.join();
        Thread.sleep(1000);
    }catch (Exception e){
        trace.log(LogType.APP, LogLevel.ERROR, "service2 failed", e);
    }finally {
        span.stop();
    }
}
 
//远程调用，一般用于trace追踪一个远程服务，需要传递traceId和父Span的Id
private void service3(long traceId, long parentId){
    ITrace trace = TraceManager.getTracer("rmoteTrace");
    ISpan span = trace.continueSpan("remote_span", "rmote_service", traceId, parentId, SpanType.WEB_SERVICE);   //注意这里是continue，不是start
    trace.log(LogType.APP, LogLevel.INFO, "remote service message");
    Map<String, String> tags = new HashMap<>();
    tags.put("tag1", "value1");
    try {
        Thread.sleep(1000);
    }catch (Exception e){}
    trace.log(LogType.APP, LogLevel.INFO, "remote service message",tags );
    span.stop();
    trace.clear();
}
```


