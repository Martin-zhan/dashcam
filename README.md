# Dashcam
基于Kafka, HBase, Elasticsearch中央日志系统。

1. 支持Java, NodeJs
2. 快速检索
3. Trace和自动trace调用链路
4. Metrics dashboard功能


![](https://github.com/acupple/dashcam/blob/master/Arch.jpg)

# Trace设计

```bash
logevent: traceId
span: spanId, traceId, parentSpanId, startTime, stopTime

tracer = log.startTrace();
span = tracer.startSpan();
og.info("message1");
og.info("message2");
spanInner = tracer.startSpan();
log.info("message");
spanInner.stop();
span.stop();
tracer.stop()
```
# Metrics 接口定义
```java
void metrics(String name, Map<String, String> attrs, long value);
```

# 计划
* 第一期: 实现普通日志和Trace功能, 实时收集日志(系统日志不落地)
* 第二期: 提供离线日志收集功能(可以采用flume等开源项目收集日志)
* 第三期: 增加通用metrics实现(基于ES)
* 第四期: 增加storm接口, 可选storm实现数据收集
