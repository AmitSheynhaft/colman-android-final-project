package com.example.hotelreviews.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    var id: String = "",
    var userId: String = "",
    var hotelName: String = "",
    var rating: Int = 0,
    var description: String = "",
    var imageUrl: String = "",
    var lastUpdated: Long? = null,
    var isDeleted: Boolean = false
) {
    companion object {
        const val COLLECTION = "reviews"
        const val LAST_UPDATED = "lastUpdated"
    }
}
