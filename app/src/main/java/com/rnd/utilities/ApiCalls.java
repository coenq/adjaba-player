package com.rnd.utilities;


import com.rnd.newmodels.LoginRequest;
import com.rnd.newmodels.LoginResponse;
import com.rnd.newmodels.Root;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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

    @GET("get_screen_by_user")
    Call<List<Root>> getScreenResponse(@Header("Authorization") String authHeader);
    @FormUrlEncoded
    @POST("create_screenview")
    Call<Void> sendCameraData(@Header("Authorization") String authHeader, @Field("screenId") String screenId,
                                       @Field("screenViewId") String screenViewId,
                                       @Field("amountSettled") boolean amountSettled,
                                       @Field("currency") String currency,
                                       @Field("dayHour") long dayHour,
                                       @Field("playSec") double playSec,
                                       @Field("female20") int female20,
                                       @Field("female32") int female32,
                                       @Field("female40") int female40,
                                       @Field("female50") int female50,
                                       @Field("female50plus") int female50plus,
                                       @Field("format") String format,
                                       @Field("impressionCost") double impressionCost,
                                       @Field("locationType") String locationType,
                                       @Field("male20") int male20,
                                       @Field("male32") int male32,
                                       @Field("male40") int male40,
                                       @Field("male50") int male50,
                                       @Field("male50plus") int male50plus,
                                       @Field("objectDetected") List<String> objectDetected, // هنا تبعتها كـ String: "car,person"
                                       @Field("orientation") String orientation,
                                       @Field("playTimeStamp") String playTimeStamp,
                                       @Field("screenDevice") String screenDevice,
                                       @Field("screenPlayer") String screenPlayer,
                                       @Field("tags") List<String> tags, // "ad1,promo"
                                       @Field("textDetected") List<String> textDetected, // "SALE,50% OFF"
                                       @Field("viewCount") int viewCount);
//
//    @GET("get_advert_by_user")
//    Call<List<GetAdvertsResponse>> getAdvertResponse(@Header("Authorization") String authHeader);
//
//    @GET("get_screen_playlists/{screenId}")
//    Call<List<WatchingModel>> getAdsByScreen(
//            @Path("screenId") String screenId,
//            @Header("Authorization") String token
//    );
//
//    @GET("media/{path}")
//    Call<VideoImageModel> getUrl(@Header("Authorization") String authHeader, @Path("path") String path);
//
//    @GET("/v1/forecast.json")
//    Call<WeatherModel> getWeather(@Query("key") String key,
//                                  @Query("q") String city);
//

/*

    @Headers({
            "Content-Type: text/cmd",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @GET()
    Call<List<ScreenPlaylists>> getVideos(@Url String url);

    @Headers({
            "Content-Type: text/cmd",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @GET()
    Call<List<GetScreensResponse>> getScreens(@Url String url);

    @Headers({
            "Content-Type: text/cmd",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @GET()
    Call<GoogleLoginResponse> getUser(@Url String url);


    @GET("data/2.5/weather?")
    Call<WeatherResponse> getCurrentWeatherData(@Query("lat") String lat, @Query("lon") String lon, @Query("APPID") String app_id);


    @Headers({
            "Content-Type: text/cmd",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @GET()
    Call<GetVideosResponce> getVideos1(@Url String url);


    @Headers({
            "Content-Type: application/json",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @POST("create_impression")
    Call<AddScreenViewResponce> sendScreenView(@Body AddScreenViewRequest request);


    @Headers({
            "Content-Type: application/json",
            "client-id: JamesBond",
            "client-secret: 777898"
    })
    @POST("create_impression")
    Call<AddScreenViewResponce> sendImpressions(@Body AddImpressionRequest request);


    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadFile(@Url String fileUrl);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);

*/

}
