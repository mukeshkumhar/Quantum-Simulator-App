package com.example.quantumsimulator.ApiManager

import com.example.quantumsimulator.Routes.QuantumApiService
import com.example.quantumsimulator.URLs.RetrofitInstance
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val instance: QuantumApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitInstance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(QuantumApiService::class.java)
    }
}