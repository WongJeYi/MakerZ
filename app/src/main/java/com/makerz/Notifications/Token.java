package com.makerz.Notifications;

public class Token {
    /*An FCM Token, or commonly known as a registrationToken.
     An ID is issued by GCM connection servers to the client app that allows it to receive messages*/

    String token;

    public Token(String token) {
        this.token = token;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
