package com.example.josh.network

import com.example.josh.data.Product

class ProductsRepository(private val api: ProductsApi) {

    /**
     * Old function - fetches only the first product (limit=1)
     */
    suspend fun fetchFirstProduct(): Product? {
        val resp = api.getProducts(limit = 1)
        return resp.products.firstOrNull()
    }

    /**
     * New Function: Fetch RANDOM product every time.
     */
    suspend fun fetchRandomProduct(): Product? {
        return try {
            // Fetch first 30 products (API max limit for this endpoint)
            val resp = api.getProducts(limit = 30)

            // Pick random one
            resp.products.randomOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generic fetch if needed elsewhere.
     */
    suspend fun fetchProducts(limit: Int = 30) = api.getProducts(limit)
}
