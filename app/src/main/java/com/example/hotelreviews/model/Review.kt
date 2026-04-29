package com.example.hotelreviews.model

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId val id: String = "",
    val userId: String = "",
    val hotelName: String = "",
    val rating: Int = 0,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
