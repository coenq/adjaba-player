package com.rnd.utilities;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {

    private final static Retrofit retrofit = new Retrofit.Builder().baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build();
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
