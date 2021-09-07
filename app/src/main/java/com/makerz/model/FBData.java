package com.makerz.model;

public class FBData{

    // list of facebook image, message, messageId, cretedTime
    private String FBImage = new String();
    private String FBMessage = new String();
    private String FBId = new String();
    private String FBCreatedTime = new String();
    public FBData(String FBImage,String FBMessage,String FBId,String FBCreatedTime){
        this.FBCreatedTime=FBCreatedTime;
        this.FBMessage=FBMessage;
        this.FBId=FBId;
        this.FBImage=FBImage;

    }

    public String getFBImage() {
        return FBImage;
    }

    public void setFBImage(String FBImage) {
        this.FBImage = FBImage;
    }

    public String getFBMessage() {
        return FBMessage;
    }

    public void setFBMessage(String FBMessage) {
        this.FBMessage = FBMessage;
    }

    public String getFBId() {
        return FBId;
    }

    public void setFBId(String FBId) {
        this.FBId = FBId;
    }

    public String getFBCreatedTime() {
        return FBCreatedTime;
    }

    public void setFBCreatedTime(String FBCreatedTime) {
        this.FBCreatedTime = FBCreatedTime;
    }


}