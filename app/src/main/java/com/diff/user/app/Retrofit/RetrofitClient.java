package com.diff.user.app.Retrofit;

import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static Retrofit retrofit_address = null;
    private static Retrofit send_retrofit = null;

    public static Retrofit getClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLHelper.map_address_url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();*/

        if (retrofit_address == null) retrofit_address = new Retrofit.Builder()
                .baseUrl(URLHelper.map_address_url)
                .client(httpClient.build())
                .build();
        return retrofit_address;
    }

    public static Retrofit getLiveTrackingClient() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        if (retrofit == null) retrofit = new Retrofit.Builder()
                .baseUrl(AccessDetails.serviceurl)
                .client(httpClient.build())
                .build();
        return retrofit;
    }

    public static Retrofit sendRequestAPI() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        if (send_retrofit == null) send_retrofit = new Retrofit.Builder()
                .baseUrl(AccessDetails.serviceurl)
                .client(httpClient.build())
                .build();
        return send_retrofit;
    }

    private static OkHttpClient.Builder httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY));
    }
}
