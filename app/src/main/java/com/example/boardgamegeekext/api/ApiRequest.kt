package com.example.boardgamegeekext.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRequest {

    @GET("https://boardgamegeek.com/xmlapi2/user")
    fun getUser(@Query("name") name : String): Call<UserApi>

    @GET("https://boardgamegeek.com/xmlapi2/collection")
    fun getCollection(@Query("username") username : String, @Query("stats") stats : String) : Call<CollectionApi>

    @GET("https://boardgamegeek.com/xmlapi2/thing")
    fun getDetailedGameData(@Query("id") id : String) : Call<DetailedGameDataApi>
}