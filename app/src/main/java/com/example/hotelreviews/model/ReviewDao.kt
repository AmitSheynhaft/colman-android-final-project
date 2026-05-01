package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getByUserId(userId: String): LiveData<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg reviews: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: Review)

    @Delete
    suspend fun delete(review: Review)

    @Query("DELETE FROM reviews")
    suspend fun deleteAll()
}
