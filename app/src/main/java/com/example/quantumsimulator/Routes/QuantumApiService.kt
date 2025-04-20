package com.example.quantumsimulator.Routes

import com.example.quantumsimulator.DataModels.QuantumRequest
import com.example.quantumsimulator.DataModels.QuantumResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface QuantumApiService {
    @POST("simulate/")
    suspend fun simulate(@Body request: QuantumRequest): Response<QuantumResponse>
}