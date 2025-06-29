package com.example.prm392_group4_teamproject.CAPI;
import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit getClientWithToken(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context)) // gắn Interceptor
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://betinder.gicunhco.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://betinder.gicunhco.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}