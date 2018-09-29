package com.diff.user.app.Retrofit;

import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */


public interface ApiInterface {
    //synchronous.
    @GET("json?")
    Call<ResponseBody> getResponse(@Query("latlng") String param1, @Query("key") String param2);

    @POST(URLHelper.EXTEND_TRIP)
    @FormUrlEncoded
    Call<ResponseBody> extendTrip(@Header("X-Requested-With") String xmlRequest, @Header("Authorization") String strToken,
                                  @Field("request_id") String request_id, @Field("latitude") String latitude, @Field("longitude") String longitude, @Field("address") String address);

    @GET(URLHelper.SAVE_LOCATION)
    Call<ResponseBody> getFavoriteLocations(@Header("X-Requested-With") String xmlRequest,
                                            @Header("Authorization") String strToken);

    @POST(URLHelper.SAVE_LOCATION)
    @FormUrlEncoded
    Call<ResponseBody> updateFavoriteLocations(@Header("X-Requested-With") String xmlRequest, @Header("Authorization") String strToken,
                                               @Field("type") String type, @Field("latitude") String latitude, @Field("longitude") String longitude, @Field("address") String address);

    @DELETE(URLHelper.SAVE_LOCATION + "/" + "{id}")
    Call<ResponseBody> deleteFavoriteLocations(@Path("id") String id, @Header("X-Requested-With") String xmlRequest, @Header("Authorization") String strToken,
                                               @Query("type") String type, @Query("latitude") String latitude, @Query("longitude") String longitude, @Query("address") String address);

    @POST(AccessDetails.access_login)
    @FormUrlEncoded
    Call<ResponseBody> accessAPI(@Header("X-Requested-With") String xmlRequest, @Field("username") String username,
                                 @Field("accesskey") String accesskey);

    @POST(URLHelper.SEND_REQUEST_API)
    @FormUrlEncoded
    Call<ResponseBody> sendCashRequestAPI(@Header("X-Requested-With") String xmlRequest, @Header("Authorization") String accesskey,
                                          @Field("RF_name") String OthersName, @Field("RF_phone_number") String OthersPhone,@Field("pass_num") String PassportNum,
                                          @Field("s_latitude") String s_latitude, @Field("s_longitude") String s_longitude,
                                          @Field("d_latitude") String d_latitude, @Field("d_longitude") String d_longitude,
                                          @Field("s_address") String s_address, @Field("d_address") String d_address,
                                          @Field("service_type") String service_type, @Field("distance") String distance,
                                          @Field("schedule_date") String schedule_date, @Field("schedule_time") String schedule_time,
                                          @Field("payment_mode") String payment_mode, @Field("use_wallet") String use_wallet,
                                          @Field("estimated_fare") String ride_for_others , @Field("estimated_fare") String estimated_fare );

    @POST(URLHelper.SEND_REQUEST_API)
    @FormUrlEncoded
    Call<ResponseBody> sendCardRequestAPI(@Header("X-Requested-With") String xmlRequest, @Header("Authorization") String accesskey,
                                          @Field("s_latitude") String s_latitude, @Field("s_longitude") String s_longitude,
                                          @Field("d_latitude") String d_latitude, @Field("d_longitude") String d_longitude,
                                          @Field("s_address") String s_address, @Field("d_address") String d_address,
                                          @Field("service_type") String service_type, @Field("distance") String distance,
                                          @Field("schedule_date") String schedule_date, @Field("schedule_time") String schedule_time,
                                          @Field("payment_mode") String payment_mode, @Field("use_wallet") String use_wallet,
                                          @Field("card_id") String card_id);

}
