package com.example.josh.data

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String,
    val task_type: String,
    val text: String? = null,
    val image_url: String? = null,
    val image_path: String? = null,
    val audio_path: String? = null,
    val duration_sec: Int,
    val timestamp: String
)

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    
    // Made optional because sometimes null / absent
    val price: Double? = null,
    val rating: Double? = null,
    
    // API always returns these keys, but safe defaults prevent decode failure
    val images: List<String> = emptyList(),
    val thumbnail: String? = null
)

@Serializable
data class ProductsResponse(
    val products: List<Product> = emptyList(),
    val total: Int? = null,
    val skip: Int? = null,
    val limit: Int? = null
)

