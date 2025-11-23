package com.example.josh.network

import com.example.josh.data.ProductsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(@Query("limit") limit: Int = 30): ProductsResponse
}
