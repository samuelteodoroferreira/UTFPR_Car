package com.example.ftprcar.data.api

import com.example.ftprcar.data.model.Car
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarApiService {

    @GET("car")
    suspend fun listCars(): List<Car>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): Car

    @POST("car")
    suspend fun createCar(@Body car: Car): Response<Car>

    @POST("car")
    suspend fun createCars(@Body cars: List<Car>): Response<List<Car>>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Response<Car>
}
