package com.example.hotelreviews.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String = "",
    var username: String = "",
    var fullName: String = "",
    var email: String = "",
    var profileImageUrl: String = "",
    var lastUpdated: Long? = null
) {
    companion object {
        const val COLLECTION = "users"
        const val LAST_UPDATED = "lastUpdated"
    }
}
