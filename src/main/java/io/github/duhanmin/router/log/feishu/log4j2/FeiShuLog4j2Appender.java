package io.github.duhanmin.router.log.feishu.log4j2;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.duhanmin.router.log.entity.EventLogEntry;
import io.github.duhanmin.router.log.util.ExceptionUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;

/**
 * Created by hawkingfoo on 2017/6/29 0029.
 */
@Plugin(name = "FeiShuLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class FeiShuLog4j2Appender extends AbstractAppender {
    private static final String feishu = "{\"config\":{\"wide_screen_mode\":true},\"header\":{\"title\":{\"content\":\"服务错误日志\",\"tag\":\"plain_text\"}},\"i18n_elements\":{\"zh_cn\":[{\"fields\":[{\"is_short\":true,\"text\":{\"content\":\"**服务名：**\\n{}\",\"tag\":\"lark_md\"}},{\"is_short\":true,\"text\":{\"content\":\"**时间：**\\n{}\",\"tag\":\"lark_md\"}},{\"is_short\":false,\"text\":{\"content\":\"\",\"tag\":\"lark_md\"}},{\"is_short\":true,\"text\":{\"content\":\"**级别：**\\n{}\",\"tag\":\"lark_md\"}},{\"is_short\":true,\"text\":{\"content\":\"**机器：**\\n{}\",\"tag\":\"lark_md\"}},{\"is_short\":false,\"text\":{\"content\":\"\",\"tag\":\"lark_md\"}}],\"tag\":\"div\"},{\"tag\":\"hr\"},{\"tag\":\"div\",\"text\":{\"content\":\"**告警内容**\\n{}\",\"tag\":\"lark_md\"}}]}}";
    private String url;
    private String appName;
    private boolean syncSend;
    private int timeOutMilliseconds = 500;

    public FeiShuLog4j2Appender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
                                String appName, boolean syncSend, String url) {
        super(name, filter, layout, ignoreExceptions,null);
        this.appName = appName;
        this.syncSend = syncSend;
        this.url = url;
    }

    @Override
    protected Serializable toSerializable(LogEvent event) {
        return super.toSerializable(event);
    }

    @Override
    public void append(LogEvent event) {
        try {
            EventLogEntry message = subAppend(event);
            if (message.getLevel().toLowerCase(Locale.ROOT).equals("error")){
                final String log = StrUtil.format(feishu, appName, message.getTimeStamp(), message.getLevel(),
                        message.getHostName(), message.getThrowableInfo());
                message(log);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*  接收配置文件中的参数 */
    @PluginFactory
    public static FeiShuLog4j2Appender createAppender(@PluginAttribute("name") String name,
                                                      @PluginAttribute("appName") String appName,
                                                      @PluginAttribute("syncSend") boolean syncSend,
                                                      @PluginAttribute("url") String url,
                                                      @PluginElement("Filter") final Filter filter,
                                                      @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                      @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new FeiShuLog4j2Appender(name, filter, layout, ignoreExceptions,appName,syncSend,url);
    }

    public void message(String content){
        Map<String,Object> map = new HashMap<>();
        map.put("msg_type", "interactive");
        map.put("card", content);
        ThreadUtil.execute(() -> HttpRequest.post(url).body(JSONUtil.toJsonStr(map)).timeout(timeOutMilliseconds).execute());
    }

    /**
     * 获取与拼装消息
     * @param event
     * @return
     */
    private EventLogEntry subAppend(LogEvent event) {
        EventLogEntry eventLogEntry = new EventLogEntry();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            eventLogEntry.setHostName(inetAddress.getHostName());
            eventLogEntry.setAddress(inetAddress.getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                final String msg = toSerializable(event).toString();
                eventLogEntry.setThrowableInfo(ExceptionUtils.stacktraceToOneLineString(msg));
            }catch (Exception e){
                eventLogEntry.setThrowableInfo("-");
            }finally {
                eventLogEntry.setEventId(UUID.randomUUID().toString());
                eventLogEntry.setEventTime(System.currentTimeMillis());
                eventLogEntry.setEventChannel(this.appName);
                eventLogEntry.setLevel(event.getLevel().toString());
                eventLogEntry.setMessage(event.getMessage().getFormattedMessage());
                eventLogEntry.setThreadName(event.getThreadName());
                String date = new java.text.SimpleDateFormat("yyyy-dd-MM HH:mm:ss SSS").format(new Date(event.getTimeMillis()));
                eventLogEntry.setTimeStamp(date);
            }
        }
        return eventLogEntry;
    }

}

