package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): LiveData<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)
}
