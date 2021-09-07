package com.makerz.Notifications;

import com.google.gson.annotations.SerializedName;
import com.makerz.model.Message;

public class Sender {
    @SerializedName("data")
    private Message data;
    @SerializedName("to")
    private String to;
    @SerializedName("notification")
    private Notification notification;

    public Sender() {
    }

    public Sender(Message data, String to,Notification notification) {
        this.data = data;
        this.to = to;
        this.notification = notification;
    }

    public Message getData() {
        return data;
    }

    public void setData(Message data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
