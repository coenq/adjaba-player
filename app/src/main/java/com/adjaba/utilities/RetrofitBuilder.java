package com.adjaba.utilities;

import com.adjaba.Interface.ApiCalls;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {

    private final static Gson lenientGson = new GsonBuilder()
            .setLenient()
            .create();

    private final static Retrofit retrofit = new Retrofit.Builder().baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(lenientGson)).build();
    private final static Retrofit retrofit2 = new Retrofit.Builder().baseUrl(Config.BASE_URL_weather)
            .addConverterFactory(GsonConverterFactory.create()).build();
    private final static ApiCalls apiCalls1 = retrofit.create(ApiCalls.class);
    private final static ApiCalls apiCalls2 = retrofit2.create(ApiCalls.class);

    public ApiCalls apiCalls() {
        return apiCalls1;
    }
    public ApiCalls apiCalls2() {
        return apiCalls2;
    }

}
