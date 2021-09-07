package com.makerz.model;

public class ActivityMessage
{
    String message;
    String codename;
    String key;

    public ActivityMessage()
    {

    }

    public ActivityMessage(String message, String codename) {
        this.message = message;
        this.codename = codename;
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString()
    {
        return "ActivityMessage(" + "message='"+ message + '\'' +
                ", codename='" + codename + '\'' +
                ", keys='" + key + '\'' + ')';
    }
}
