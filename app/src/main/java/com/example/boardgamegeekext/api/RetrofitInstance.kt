package com.example.boardgamegeekext.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitInstance {

    var logging: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    var httpClient : OkHttpClient.Builder = OkHttpClient.Builder().addInterceptor(logging)

    val api: ApiRequest by lazy{
        Retrofit.Builder()
            .baseUrl("https://boardgamegeek.com/xmlapi2/")
            .client(httpClient.build())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(ApiRequest::class.java)
    }
}