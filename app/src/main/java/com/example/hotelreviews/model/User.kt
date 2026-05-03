package com.example.hotelreviews.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @DocumentId
    var id: String = "",
    var email: String = "",
    var name: String = "",
    var profileImageUrl: String = "",
    var lastUpdated: Long = 0L,
    var isDeleted: Boolean = false
)
