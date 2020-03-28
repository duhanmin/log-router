package com.router.log.kafka.log4jappender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import com.alibaba.fastjson.JSONArray;
import com.router.log.entity.EventLogEntry;
import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.log4j.helpers.LogLog;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;

@Data
public class KafkaLogbackAppender extends AppenderBase<ILoggingEvent> {

    private String brokerList;
    private String topic;
    private boolean syncSend;
    private String appName;
    private Producer<byte[], byte[]> producer;

    @Override
    public void stop() {
        super.stop();
        producer.close();
    }

    @Override
    public void start() {
        super.start();
        if (producer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", brokerList);
            props.put("retries", 0);
            props.put("batch.size", 0);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", ByteArraySerializer.class.getName());
            props.put("value.serializer", ByteArraySerializer.class.getName());
            producer = new KafkaProducer<>(props);
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String message = subAppend(eventObject);
        Future<RecordMetadata> response = this.producer.send(
                new ProducerRecord(this.topic, message.getBytes(StandardCharsets.UTF_8)));
        if (this.syncSend) {
            try {
                response.get();
            } catch (Exception e) {
                LogLog.error("Exception while getting response", e);
            }
        }
    }

    /**
     * 获取与拼装消息
     * @param event
     * @return
     */
    private String subAppend(ILoggingEvent event) {
        EventLogEntry eventLogEntry = new EventLogEntry();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            eventLogEntry.setHostName(inetAddress.getHostName());
            eventLogEntry.setAddress(inetAddress.getHostAddress());
        } catch (Exception e) {
            LogLog.error("获取数据所在节点ip和主机名出错", e);
        }finally {
            try{
                JSONArray list = new JSONArray();
                IThrowableProxy it = event.getThrowableProxy();
                StackTraceElementProxy[] tps = it.getStackTraceElementProxyArray();
                for (StackTraceElementProxy tp:tps) {
                    list.add(tp.toString());
                }
                eventLogEntry.setThrowableInfo(list.toJSONString());
                eventLogEntry.setThreadName(it.getMessage());
            }catch (Exception e){
                eventLogEntry.setThrowableInfo("-");
            }finally {
                eventLogEntry.setEventId(UUID.randomUUID().toString());
                eventLogEntry.setEventTime(System.currentTimeMillis());
                eventLogEntry.setEventChannel(this.appName);
                eventLogEntry.setLevel(event.getLevel().levelStr);
                eventLogEntry.setMessage(event.getFormattedMessage());
                eventLogEntry.setTimeStamp(event.getTimeStamp());
            }
        }
        return eventLogEntry.toString();
    }
}
