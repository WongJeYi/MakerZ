package com.makerz.model;
public class MakerEvent {
    private String title;
    private String event_url;
    private String thumbnail;
    private String description;
    private String deadline;
    private String type;
    private String time;
    private String endtime;
    private boolean verified;


    private String email;
    // MakerEvent is the data model for trip, contest and activity
    public MakerEvent(String title, String event_url, String thumbnail, String description, String deadline,String type, String email,String time,String endtime,boolean verified){
        this.title = title;
        this.event_url = event_url;
        this.thumbnail = thumbnail;
        this.description = description;
        this.deadline=deadline;
        this.type=type;
        this.email=email;
        this.time=time;
        this.endtime=endtime;
        this.verified=verified;
    }
    public MakerEvent(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
    public String getDeadline(){
        return deadline;
    }
    public void setEvent_url(String event_url) {
        this.event_url = event_url;
    }
    public String getEvent_url(){
        return event_url;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public String getThumbnail(){
        return thumbnail;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription(){
        return description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
