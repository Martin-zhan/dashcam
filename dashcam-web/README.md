#Dashcam UI and dashboard(TODO)

#Api define
日志类型:

|类型|ID|说明|
|---|---|---|
|other|0|其他日志|
|app|1|应用日志|
|database|2|数据库日志|
|web_service|3|外部服务日志|

日志级别

|级别|ID|说明|
|---|---|---|
|info|1|常规日志|
|debug|2|调试日志|
|error|3|错误日志|
|fatal|4|致命错误日志|

## 获取应用列表
```
GET /api/app/list
Authorization: access token
Content-Type: application/json
```
正常返回
```
{
    "id": 9300,
    "name": "消息订阅",
    "log_count": 10000, //条数
    "error_count": 200, //条数
}
```
## 日志查询
获取一条trace的所有日志: 通过parent_trace_id查找(parent_trace_id={parent_trace_id}&&trace_id={parent_trace_id})
```
GET /api/app/:id/log?trace_id=<trace_id>logType=<logType>&logLevel=<logLevel>&title=<title>
    &tags=<tags>&ip=<ip>&hostname=<hostname>&startTime=<startTime>&endTime=<endTime>
Authorization: access token
Content-Type: application/json
```
参数说明

|参数|是否必须|默认值|说明|
|---|---| ---|---|
|appId|yes|none|应用ID|
|trace_id|no|none|Root TraceID(parent_trace_id)|
|appType|no|app|应用类型()|
|logLevel|no|error|日志级别|
|title|no|none|标题|
|tags|no|none|格式:{"tag1":"value1","tag2":"value2"} URL Encode|
|ip|no|none|产生日志的机器IP|
|hostname|no|none|产生日志的机器hostname|
|startTime|no|2个小时前|开始时间|
|endTime|no|当前时间|结束时间|
正常返回
```
[{
    "title":"titletitle",
    "hostname":"xxx.xxx.com",
    "ip":"127.0.0.1",
    "log_type":"app",
    "log_level": "info",
    "tags":{"tag1":"value1","tag2":"value2",...},
    "content":"this is test log message",
    "trace_id": "abixtxtx_stxg",//trace
    "parent_trace_id": "abixtxtx_stxg_p",//trace
    "time":1480319841,
    "end_time: 1480319842, //trace
}]
```
##日志统计信息
日志的统计信息按分钟聚合, 可以按空间使用量(MB)或者总条数查询
```
GET /api/app/log/metrics?appId=<appId>&logLevel=<logLevel>&hostname=<hostname>
    &ip=<ip>&startTime=<startTime>&endTime=<endTime>
Authorization: access token
Content-Type: application/json
```
参数说明

|参数|是否必须|默认值|说明|
|---|---| ---|---|
|appId|no|none|应用ID, 不提供表示查询所有日志|
|logLevel|no|none|日志级别|
|hostname|no|none|机器名|
|ip|no|none|机器IP|
|startTime|no|一天前|开始时间|
|endTime|no|当前时间|结束时间|

正常返回
```
[{
    "time": 1479830400,
    "volume": 7788 
},
{
    "time": 1479830460,
    "volume": 7899
}]
```

## 通用metrics查询接口
TODO
