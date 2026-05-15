package com.adjaba.Interface;



import com.adjaba.models.newmodels.LoginRequest;
import com.adjaba.models.newmodels.LoginResponse;
import com.adjaba.models.newmodels.GetAdvertsResponse;
import com.adjaba.models.newmodels.Root;
import com.adjaba.models.newmodels.TvPollAuthResponse;
import com.adjaba.models.newmodels.TvStartAuthResponse;
import com.adjaba.models.newmodels.VideoImageModel;
import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.models.newmodels.WeatherModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by gys on 02-01-2019.
 */

public interface ApiCalls {
    //sayed new login
    @POST("v2/authenticate_user")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("tv/start-auth")
    Call<TvStartAuthResponse> tvStartAuth();

    @GET("tv/poll-auth/{deviceCode}")
    Call<TvPollAuthResponse> tvPollAuth(@Path("deviceCode") String deviceCode);

    @GET("get_screen_by_user")
    Call<List<Root>> getScreenResponse(@Header("Authorization") String authHeader);

    @GET("get_advert_by_user")
    Call<List<GetAdvertsResponse>> getAdvertResponse(@Header("Authorization") String authHeader);

    @GET("get_screen_playlists/{screenId}")
    Call<List<WatchingModel>> getAdsByScreen(
            @Path("screenId") String screenId,
            @Header("Authorization") String token
    );

    @GET("media/{path}")
    Call<VideoImageModel> getUrl(@Header("Authorization") String authHeader, @Path("path") String path);

    @GET("/v1/forecast.json")
    Call<WeatherModel> getWeather(@Query("key") String key,
                                  @Query("q") String city);

}
