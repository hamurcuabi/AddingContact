package com.emrhmrc.addcontactlist.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final int TIME_OUT = 10;
    private static final String TAG = "ApiClient";
    private static final String USER_NAME = "";
    private static final String PASSWORD = "";
    private static Retrofit retrofit = null;
    private static String Base_Url = "http://185.134.28.54/api/";

    public static Retrofit getClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
               // .addInterceptor(new BasicAuthInterceptor(USER_NAME, PASSWORD))
                .retryOnConnectionFailure(true)
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Base_Url)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit;
        }
        return retrofit;

    }
}
