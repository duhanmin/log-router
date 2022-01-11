package io.github.duhanmin.router.log.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

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
    private String timeStamp;

    /**
     * 堆栈信息
     * 如果没有，返回"-"中划线
     */
    private String throwableInfo;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
