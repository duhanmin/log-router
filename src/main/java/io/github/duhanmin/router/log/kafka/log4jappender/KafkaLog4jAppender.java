package io.github.duhanmin.router.log.kafka.log4jappender;

import io.github.duhanmin.router.log.entity.EventLogEntry;
import io.github.duhanmin.router.log.util.ExceptionUtils;
import lombok.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Log4j方式
 */
@Data
public class KafkaLog4jAppender extends AppenderSkeleton {
    private String brokerList;
    private String topic;
    private String compressionType;
    private String securityProtocol;
    private String sslTruststoreLocation;
    private String sslTruststorePassword;
    private String sslKeystoreType;
    private String sslKeystoreLocation;
    private String sslKeystorePassword;
    private String saslKerberosServiceName;
    private String clientJaasConfPath;
    private String kerb5ConfPath;
    private Integer maxBlockMs;
    private int retries = Integer.MAX_VALUE;
    private int requiredNumAcks = 1;
    private int deliveryTimeoutMs = 120000;
    private boolean ignoreExceptions = true;
    private boolean syncSend;
    private String appName;
    private int timeOutMilliseconds = 100;
    private Producer<byte[], byte[]> producer;

    public int getMaxBlockMs()
    {
        return this.maxBlockMs.intValue();
    }

    public void setMaxBlockMs(int maxBlockMs)
    {
        this.maxBlockMs = Integer.valueOf(maxBlockMs);
    }

    @Override
    public void activateOptions() {
        Properties props = new Properties();
        if (this.brokerList != null) {
            props.put("bootstrap.servers", this.brokerList);
        }
        if (props.isEmpty()) {
            throw new ConfigException("The bootstrap servers property should be specified");
        }
        if (this.topic == null) {
            throw new ConfigException("Topic must be specified by the Kafka log4j appender");
        }
        if (this.compressionType != null) {
            props.put("compression.type", this.compressionType);
        }
        props.put("acks", Integer.toString(this.requiredNumAcks));
        props.put("retries", Integer.valueOf(this.retries));
        props.put("delivery.timeout.ms", Integer.valueOf(this.deliveryTimeoutMs));
        if (this.securityProtocol != null) {
            props.put("security.protocol", this.securityProtocol);
        }
        if ((this.securityProtocol != null) && (this.securityProtocol.contains("SSL")) && (this.sslTruststoreLocation != null) && (this.sslTruststorePassword != null)) {
            props.put("ssl.truststore.location", this.sslTruststoreLocation);
            props.put("ssl.truststore.password", this.sslTruststorePassword);
            if ((this.sslKeystoreType != null) && (this.sslKeystoreLocation != null) && (this.sslKeystorePassword != null))
            {
                props.put("ssl.keystore.type", this.sslKeystoreType);
                props.put("ssl.keystore.location", this.sslKeystoreLocation);
                props.put("ssl.keystore.password", this.sslKeystorePassword);
            }
        }
        if ((this.securityProtocol != null) && (this.securityProtocol.contains("SASL")) && (this.saslKerberosServiceName != null) && (this.clientJaasConfPath != null)) {
            props.put("sasl.kerberos.service.name", this.saslKerberosServiceName);
            System.setProperty("java.security.auth.login.config", this.clientJaasConfPath);
            if (this.kerb5ConfPath != null) {
                System.setProperty("java.security.krb5.conf", this.kerb5ConfPath);
            }
        }
        if (this.maxBlockMs != null) {
            props.put("max.block.ms", this.maxBlockMs);
        }
        props.put("key.serializer", ByteArraySerializer.class.getName());
        props.put("value.serializer", ByteArraySerializer.class.getName());
        this.producer = getKafkaProducer(props);
        LogLog.debug("Kafka producer connected to " + this.brokerList);
        LogLog.debug("Logging for topic: " + this.topic);
    }

    protected Producer<byte[], byte[]> getKafkaProducer(Properties props)
    {
        return new KafkaProducer(props);
    }

    protected void append(LoggingEvent event) {
        String message = subAppend(event);
        LogLog.debug("[" + new Date(event.getTimeStamp()) + "]" + message);
        Future<RecordMetadata> response = this.producer.send(
                new ProducerRecord(this.topic, message.getBytes(StandardCharsets.UTF_8)));
        if (this.syncSend) {
            try {
                response.get(timeOutMilliseconds, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                if (!this.ignoreExceptions) {
                    throw new RuntimeException(ex);
                }
                LogLog.debug("Exception while getting response", ex);
            }
        }
    }

    /**
     * 获取与拼装消息
     * @param event
     * @return
     */
    private String subAppend(LoggingEvent event) {
        String eventStr = this.layout == null ? event.getRenderedMessage() : this.layout.format(event);
        EventLogEntry eventLogEntry = new EventLogEntry();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            eventLogEntry.setHostName(inetAddress.getHostName());
            eventLogEntry.setAddress(inetAddress.getHostAddress());
        } catch (Exception e) {
            LogLog.error("获取数据所在节点ip和主机名出错", e);
        }finally {
            try{
                String stacktraceToOneLineString = ExceptionUtils.stacktraceToOneLineString(event.getThrowableInformation().getThrowable(),2000);
                eventLogEntry.setThrowableInfo(stacktraceToOneLineString);
            }catch (Exception e){
                eventLogEntry.setThrowableInfo("-");
            }finally {
                eventLogEntry.setEventId(UUID.randomUUID().toString());
                eventLogEntry.setEventTime(System.currentTimeMillis());
                eventLogEntry.setEventChannel(this.appName);
                eventLogEntry.setCategoryName(eventStr);
                eventLogEntry.setFqnOfCategoryClass(event.fqnOfCategoryClass);
                eventLogEntry.setLevel(event.getLevel().toString());
                eventLogEntry.setMessage(event.getMessage().toString());
                eventLogEntry.setThreadName(event.getThreadName());
                eventLogEntry.setTimeStamp(event.timeStamp);
            }
        }
        return eventLogEntry.toString();
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.producer.close();
        }
    }

    public boolean requiresLayout() {
        return true;
    }
}