# canalx

* canalx是一个类似于Otter的简易版本的Canal数据转接器,提供稳定、高可靠的Binlog Dump传输方案，并且支持自定义扩展。


## quick starter

### 相关配置 

* 添加canal instance `目前支持单机版`
```yaml
canal:
  configs[0]:
    name: canal connector name,用于添加任务时动态绑定canal connector
    hostname: canal instance host
    port: canal instance port
    destination: canal instance destination
    username: canal username
    password: canal password
```

* 添加Kafka数据通道配置 `目前仅支持kafka数据通道，`
```yaml
spring:
  kafka:
    producer:
      acks: all
      transaction-id-prefix: canal-tx-
      bootstrap-servers: hadoop:9092,hadoop:9093,hadoop:9094
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      batch-size: 1046567
      buffer-memory: 3145728
      compression-type: snappy
      retries: 3
```

### 入口

#### 添加任务

**请求url:** {hostname:port}/canal/addTask
**请求类型:** `POST`
**请求Header:** `Content-Type:application/json`
**请求体**
```json
{
	"taskName":"wechat_assistants",
	"canalConnectorName":"test", 
	"listener": {
		"listenRules":"ninth_studio:wechat_assistants",
		"batchSize": 5000,
		"channelType": "kafka",
		"listenEventType": ["DELETE","UPDATE", "INSERT"]
	}
}
```
**参数描述**
```text
`listenRules`:监听表，默认为*:*,代表监听全部schema和table,建议单schema方式监听: 'for_os:*',并且提前创建Kafka Topic,语法如下:
  kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 --create  --partitions 3 --replication-factor 2 --topic ninth_studio.wechat_assistants
  
`taskName`: 任务名称，用于取消任务时使用
`canalConnectorName`: canal连接器名称，在上述`添加canal instance`中配置
`batchSize`: canal一批处理数据量，默认5000
`channelType`: 数据管道类型，默认为kafka
`listenEventType`: 监听事件类型,默认为["DELETE","UPDATE", "INSERT"]
```

**curl**
```text
curl -X POST \
  http://hadoop:10000/canal/addTask \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 132' \
  -H 'Content-Type: application/json' \
  -H 'Host: hadoop:10000' \
  -H 'Postman-Token: e74b53d5-1b0d-4a29-95e0-2f575251c7ba,20e897ee-9670-4592-b9f4-8ee54683d9bc' \
  -H 'User-Agent: PostmanRuntime/7.15.2' \
  -H 'cache-control: no-cache' \
  -d '{
	"taskName":"wechat_assistants",
	"canalConnectorName":"test",
	"listener":{
		"listenRules":"ninth_studio:wechat_assistants"
	}
}'
```

#### 取消任务

**请求url:** {hostname:port}/canal/cancelTask/{taskName}
**请求类型:** `GET`
**请求Header:** `无`
**请求参数:** 路径参数,taskName:任务名称

**请求url**
```text
hadoop:10000/canal/cancelTask/test-canal
```

## Version

### v1.0

[x] 支持单机版Canal server
[x] 支持Kafka数据通道
[x] 支持创建和取消采集binlog task
[x] task配置存储与内存中
[x] 多schema、多table数据采集上报
[ ] 多任务压力测试

### v1.1 feature

[ ] 支持分布式Canal
[ ] 多数据通道支持
[ ] TODO