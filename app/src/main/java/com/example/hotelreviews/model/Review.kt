package com.example.hotelreviews.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    @DocumentId 
    var id: String = "",
    var userId: String = "",
    var hotelName: String = "",
    var city: String = "",
    var rating: Double = 0.0,
    var description: String = "",
    var imageUrl: String = "",
    var placeId: String = "",
    var apiRating: Double = 0.0,
    var apiReviewCount: Int = 0,
    var timestamp: Long = System.currentTimeMillis()
)
