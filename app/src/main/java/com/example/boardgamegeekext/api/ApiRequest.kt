package com.example.boardgamegeekext.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiRequest {

    @GET("https://boardgamegeek.com/xmlapi2/user")
    fun getUser(@Query("name") name : String): Call<UserApi>

}