# Log-Router

该项目可用于java、spark和hadoop项目的运行日志转发到kafka

# 前言

工程中的日志通过该SDK收集发送到kafka，通过logstash收集到ES，通过kibana建立项目日志索引区分查看

# 操作方法

## Spark on Yarn 日志接入ES

```SHELL
--jars  libs/kafka-clients-1.0.1.3.0.0.0-1634.jar,  \
--files spark-log4j.properties,  \
--conf spark.driver.extraJavaOptions="-Dlog4j.configuration=spark-log4j.properties" \
--conf spark.executor.extraJavaOptions="-Dlog4j.configuration=spark-log4j.properties" \
```

## Java、Spring、Spark、Flink接入日志方式类似

* 详见Spark和Flink官网
* 接入日志方式类似，详见下文中logback和log4j

## logback方式配置模板

```xml
<?xml version="1.0" encoding="UTF-8" ?>

<configuration scan="true" scanPeriod="3 seconds">
    <!--设置日志输出为控制台-->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] [%logger{32}] %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="KAFKA" class="io.github.duhanmin.router.log.kafka.logback.KafkaLogbackAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
        <syncSend>false</syncSend>
        <brokerList>ip:port</brokerList>
        <appName>hotwheels-api</appName>
        <topic>HOTWHEELS-REALTIME_LOG</topic>
    </appender>

    <!--设置日志输出为文件-->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>logFile.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>logFile.%d{yyyy-MM-dd_HH-mm}.log.zip</FileNamePattern>
        </rollingPolicy>
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>%d{HH:mm:ss,SSS} [%thread] %-5level %logger{32} - %msg%n</Pattern>
        </layout>
    </appender>

    <root>
        <level value="info"/>
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="KAFKA"/>
    </root>
</configuration>
```


## log4j方式配置模板

```SHELL
log4j.rootLogger=INFO,console,KAFKA
## appender KAFKA
log4j.appender.KAFKA=com.router.log.kafka.log4jappender.KafkaLog4jAppender
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

## Maven引入Jar包

下载源
```xml
<repositories>
    <repository>
        <id>maven-repo-master</id>
        <url>https://raw.github.com/duhanmin/mvn-repo/master/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

包
```xml
<dependency>
    <groupId>com.duhanmin</groupId>
    <artifactId>log-router</artifactId>
    <version>0.1.1</version>
</dependency>
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

logstash配置
```shell
input {
    kafka {
        bootstrap_servers => ["ip1:port,ip2:port"]
        group_id => "es-transfer"
        topics => ["HOTWHEELS-REALTIME_LOG"]
        consumer_threads => 5
        decorate_events => true
        codec => "json"
        }
}
output {
    elasticsearch {
        hosts => ["ip1:port","ip2:port"]
        codec => "json"
        index => "%{eventChannel}"
   }
}
```

# 参考资料

https://duhanmin.github.io/2020/04/01/%E9%80%9A%E7%94%A8%E6%97%A5%E5%BF%97%E6%94%B6%E9%9B%86SDK/

