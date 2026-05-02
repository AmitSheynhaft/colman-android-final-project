package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE isDeleted = 0 ORDER BY lastUpdated DESC")
    fun getAll(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId AND isDeleted = 0 ORDER BY lastUpdated DESC")
    fun getByUserId(userId: String): LiveData<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg reviews: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(review: Review)

    @Delete
    fun delete(review: Review)

    @Query("DELETE FROM reviews")
    fun deleteAll()
}
