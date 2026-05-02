package com.example.hotelreviews.model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hotelreviews.base.MyApplication

@Database(entities = [Review::class, User::class], version = 3)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
    abstract fun userDao(): UserDao
}

object AppLocalDb {
    val db: AppLocalDbRepository by lazy {
        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Context not available")
        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "hotel_reviews_db.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
}
