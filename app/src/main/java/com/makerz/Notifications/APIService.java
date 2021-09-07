package com.makerz.Notifications;

import com.makerz.model.Message;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    // To get authorization:key, go firebase, press setting icon, press cloud messaging, the server key token is the authorization:key

    @Headers({
        "Content-Type:application/json",
        "Authorization:key=AAAAWMx9Vns:APA91bGGS7EiRhnqzFZnZ2gVJx9yLfJAfwEzw_k-jdqXE59roHC1n2knuMk_emzoHyMvEIvwZmsY2DDyTd7QEja9feiNY8nH1BQ2sm-nWvVtENTz9VNy6TGBSm8JCt2G6ItRMsLKV1N4"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
