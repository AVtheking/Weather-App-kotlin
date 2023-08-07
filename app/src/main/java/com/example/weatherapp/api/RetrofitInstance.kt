package com.example.weatherapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object
    {
        fun getRetrofitInstance():Retrofit{
            return Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val api by lazy {
                RetrofitInstance.getRetrofitInstance().create(WeatherService::class.java)
        }
    }
}