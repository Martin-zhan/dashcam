# Dashcam
基于Kafka, HBase, Elasticsearch中央日志系统。

1. 支持Java, NodeJs
2. 快速检索
3. Trace和自动trace调用链路


![](https://github.com/acupple/dashcam/blob/master/Arch.jpg)

# Trace设计
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
