package com.azi.etkinlikkesifkotlin1

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object RetrofitClient {
    private const val BASE_URL = "https://www.eventbriteapi.com/v3/"

    val instance: EventbriteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventbriteApi::class.java)
    }

}
