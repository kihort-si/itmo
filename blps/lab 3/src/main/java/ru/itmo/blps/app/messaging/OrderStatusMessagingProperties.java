package ru.itmo.blps.app.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging.order-status")
public class OrderStatusMessagingProperties {
    private boolean enabled;
    private String brokerUrl = "tcp://127.0.0.1:61616";
    private String stompHost = "127.0.0.1";
    private int stompPort = 61613;
    private String stompLogin = "";
    private String stompPasscode = "";
    private String topicName = "OrderStatusEvents";
    private String stompTopicPrefix = "/topic/";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getStompHost() {
        return stompHost;
    }

    public void setStompHost(String stompHost) {
        this.stompHost = stompHost;
    }

    public int getStompPort() {
        return stompPort;
    }

    public void setStompPort(int stompPort) {
        this.stompPort = stompPort;
    }

    public String getStompLogin() {
        return stompLogin;
    }

    public void setStompLogin(String stompLogin) {
        this.stompLogin = stompLogin;
    }

    public String getStompPasscode() {
        return stompPasscode;
    }

    public void setStompPasscode(String stompPasscode) {
        this.stompPasscode = stompPasscode;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getStompTopicPrefix() {
        return stompTopicPrefix;
    }

    public void setStompTopicPrefix(String stompTopicPrefix) {
        this.stompTopicPrefix = stompTopicPrefix;
    }

    public String stompDestination() {
        return stompTopicPrefix + topicName;
    }
}
