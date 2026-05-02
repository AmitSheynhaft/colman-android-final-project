package com.example.hotelreviews.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hotelreviews.base.MyApplication
import com.google.firebase.auth.FirebaseAuth

class Model private constructor() {

    private val modelFirebase = ModelFirebase()
    private val localDb = AppLocalDb.db
    private val auth = FirebaseAuth.getInstance()

    companion object {
        val instance: Model by lazy { Model() }
    }

    enum class LoadingState {
        LOADING,
        LOADED
    }

    val reviewsLoadingState = MutableLiveData<LoadingState>(LoadingState.LOADED)
    val userLoadingState = MutableLiveData<LoadingState>(LoadingState.LOADED)

    // --- User Auth & Profile ---

    fun login(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                refreshUser {
                    callback(true, null)
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun register(email: String, pass: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                val userEmail = auth.currentUser?.email ?: ""
                val user = User(id = uid, email = userEmail)
                updateUser(user) {
                    callback(true, null)
                }
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUser(): LiveData<User?> {
        val uid = auth.currentUser?.uid ?: ""
        return localDb.userDao().getUserById(uid)
    }

    fun refreshUser(callback: (() -> Unit)? = null) {
        val uid = auth.currentUser?.uid ?: return
        userLoadingState.value = LoadingState.LOADING
        modelFirebase.getUser(uid) { user ->
            if (user != null) {
                MyApplication.Globals.executorService.execute {
                    localDb.userDao().insert(user)
                    MyApplication.Globals.mainHandler.post {
                        userLoadingState.value = LoadingState.LOADED
                        callback?.invoke()
                    }
                }
            } else {
                userLoadingState.value = LoadingState.LOADED
                callback?.invoke()
            }
        }
    }

    fun updateUser(user: User, callback: () -> Unit) {
        userLoadingState.value = LoadingState.LOADING
        modelFirebase.updateUser(user) {
            MyApplication.Globals.executorService.execute {
                localDb.userDao().insert(user)
                MyApplication.Globals.mainHandler.post {
                    userLoadingState.value = LoadingState.LOADED
                    callback()
                }
            }
        }
    }

    // --- Reviews ---

    fun getAllReviews(): LiveData<List<Review>> {
        refreshAllReviews()
        return localDb.reviewDao().getAll()
    }

    fun getUserReviews(): LiveData<List<Review>> {
        val uid = auth.currentUser?.uid ?: ""
        return localDb.reviewDao().getByUserId(uid)
    }

    fun refreshAllReviews() {
        reviewsLoadingState.value = LoadingState.LOADING
        
        val lastUpdated = MyApplication.Globals.appContext
            ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
            ?.getLong(Review.LAST_UPDATED, 0L) ?: 0L

        modelFirebase.getAllReviews(lastUpdated) { list ->
            MyApplication.Globals.executorService.execute {
                var time = lastUpdated
                for (review in list) {
                    localDb.reviewDao().insert(review)
                    if (review.lastUpdated ?: 0 > time) {
                        time = review.lastUpdated ?: 0
                    }
                }

                MyApplication.Globals.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.edit()
                    ?.putLong(Review.LAST_UPDATED, time)
                    ?.apply()

                MyApplication.Globals.mainHandler.post {
                    reviewsLoadingState.value = LoadingState.LOADED
                }
            }
        }
    }

    fun addReview(review: Review, callback: () -> Unit) {
        review.userId = auth.currentUser?.uid ?: ""
        reviewsLoadingState.value = LoadingState.LOADING
        modelFirebase.addReview(review) {
            refreshAllReviews()
            callback()
        }
    }
}
