# Log-Router

该项目可用于java、spark和hadoop项目的运行日志转发到kafka

# 操作方法

## Spark on Yarn 日志接入ES

```SHELL
--jars  libs/kafka-clients-1.0.1.3.0.0.0-1634.jar,  \
--files spark-log4j.properties,  \
--conf spark.driver.extraJavaOptions="-Dlog4j.configuration=spark-log4j.properties" \
--conf spark.executor.extraJavaOptions="-Dlog4j.configuration=spark-log4j.properties" \
```


## Spark  Standalone 日志接入ES

待测试

## Java 日志接入ES

直接修改log4j文件即可

## log4j模板

```SHELL
log4j.rootLogger=INFO,console,KAFKA
## appender KAFKA
log4j.appender.KAFKA=com.du.tool.kafka.KafkaLog4jAppender
log4j.appender.KAFKA.topic=topic
log4j.appender.KAFKA.appName=name
log4j.appender.KAFKA.brokerList=ip:port
log4j.appender.KAFKA.compressionType=none
log4j.appender.KAFKA.syncSend=true
log4j.appender.KAFKA.layout=org.apache.log4j.PatternLayout
log4j.appender.KAFKA.Threshold=WARN
log4j.appender.KAFKA.layout.ConversionPattern=%d (%t) [%p - %l] %m%n

## appender console
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d (%t) [%p - %l] %m%n
```

# 附录
日志样例

```JSON
{
    "fqnOfCategoryClass": "org.apache.commons.logging.impl.SLF4JLocationAwareLog",
    "eventId": "07d643ef-d86a-4c37-9612-28b2b81936e3",
    "address": "192.168.2.155",
    "level": "ERROR",
    "eventChannel": "topic",
    "message": "错误来了",
    "categoryName": "2019-06-03 16:02:47,327 (Executor task launch worker for task 3) [ERROR - test.rocketmq.rocketmqT$1.process(rocketmqT.java:56)] 错误来了",
    "threadName": "Executor task launch worker for task 3",
    "timeStamp": "1559548967327",
    "throwableInfo": "java.lang.ArithmeticException: / by zero   at test.rocketmq.rocketmqT$1.process(rocketmqT.java:54)   at test.rocketmq.rocketmqT$1.process(rocketmqT.java:49)   at org.apache.spark.sql.execution.streaming.ForeachSink$$anonfun$addBatch$1.apply(ForeachSink.scala:53)   at org.apache.spark.sql.execution.streaming.ForeachSink$$anonfun$addBatch$1.apply(ForeachSink.scala:49)   at org.apache.spark.rdd.RDD$$anonfun$foreachPartition$1$$anonfun$apply$29.apply(RDD.scala:935)   at org.apache.spark.rdd.RDD$$anonfun$foreachPartition$1$$anonfun$apply$29.apply(RDD.scala:935)   at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2074)   at org.apache.spark.SparkContext$$anonfun$runJob$5.apply(SparkContext.scala:2074)   at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:87)   at org.apache.spark.scheduler.Task.run(Task.scala:109)   at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:345)   at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)   at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)   at java.lang.Thread.run(Thread.java:745)  ",
    "eventTime": 1559548967341,
    "host_name": "DESKTOP-V0FGT1M"
}
```

# 赞助
<img src="https://github.com/duhanmin/mathematics-statistics-python/blob/master/images/90f9a871536d5910cad6c10f0297fc7.jpg" width="250">
