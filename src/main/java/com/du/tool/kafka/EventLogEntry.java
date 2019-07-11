package com.du.tool.kafka;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Data
public class EventLogEntry implements Serializable {
    private static final long serialVersionUID = 2405172041950251807L;

    /**
     * 从源头标记id
     */
    private String eventId;

    /**
     * 标记LOG来源
     */
    private String eventChannel;

    /**
     * host
     */
    private String hostName;

    /**
     * address
     */
    private String address;

    /**
     * 发送端标记时间
     */
    private Long eventTime;

    /**
     * 日志等级
     */
    private String level;

    /**
     * message信息
     */
    private String message;

    /**
     * renderedMessage信息
     * 包含线程，类，时间，节点等信息
     */
    private String categoryName;

    /**
     * 线程的一些状态和记录
     */
    private String threadName;

    /**
     * 打日志所用的Log类
     */
    private String fqnOfCategoryClass;

    /**
     * 该时间是日志通道自动时间
     */
    private Long timeStamp;

    /**
     * 堆栈信息
     * 如果没有，返回"-"中划线
     */
    private String throwableInfo;

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}")
                .add("\"eventTime\":" + eventTime)
                .add("\"timeStamp\":" + timeStamp);
        final Map<String, Object> map = new HashMap<>(10);
        map.put("eventId",eventId);
        map.put("eventChannel",eventChannel);
        map.put("hostName",hostName);
        map.put("address",address);
        map.put("level",level);
        map.put("message",message);
        map.put("categoryName",categoryName);
        map.put("threadName",threadName);
        map.put("fqnOfCategoryClass",fqnOfCategoryClass);
        map.put("throwableInfo",throwableInfo);
        return Joiner(stringJoiner,map);
    }

    /**
     * 避免给null加引号
     * @param stringJoiner
     * @param map
     * @return
     */
    private String Joiner(StringJoiner stringJoiner, Map<String, Object> map){
        for (Map.Entry<String, Object> entry : map.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null)
                stringJoiner = stringJoiner.add("\"" + key + "\":\"" + entry.getValue() + "\"");
            else stringJoiner = stringJoiner.add("\"" + key + "\":" + null);
        }
        return stringJoiner.toString();
    }
}
