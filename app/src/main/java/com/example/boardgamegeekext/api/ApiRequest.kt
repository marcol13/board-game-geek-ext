package com.example.boardgamegeekext.api

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ApiRequest {

    @GET("https://boardgamegeek.com/xmlapi2/user?name=marcol13")
    fun getUser(): Call<UserApi>

}