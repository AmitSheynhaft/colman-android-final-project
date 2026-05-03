package com.example.hotelreviews.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelreviews.R
import com.example.hotelreviews.base.MyApplication
import com.example.hotelreviews.model.AuthModel
import com.example.hotelreviews.model.User
import com.example.hotelreviews.model.UserModel

class UserViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val currentUserId: String? = AuthModel.getCurrentUser()?.uid

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun fetchUser() {
        currentUserId?.let { id ->
            UserModel.getUserById(id).observeForever { localUser ->
                if (localUser != null) {
                    _user.postValue(localUser)
                }
            }
            UserModel.refreshUser(id) {
                // The observer on getUserById will pick up changes
            }
        }
    }

    fun getCurrentUser(): LiveData<User>? {
        return currentUserId?.let { UserModel.getUserById(it) }
    }

    fun refreshCurrentUser() {
        currentUserId?.let { UserModel.refreshUser(it) }
    }

    fun updateProfile(name: String, bitmap: Bitmap?, onComplete: () -> Unit) {
        val userId = currentUserId ?: return
        _isLoading.value = true

        if (bitmap != null) {
            UserModel.uploadProfileImage(bitmap, userId) { imageUrl ->
                if (imageUrl != null) {
                    saveUser(userId, name, imageUrl, onComplete)
                } else {
                    _isLoading.value = false
                    _errorMessage.value = MyApplication.Globals.appContext?.getString(R.string.error_upload_failed)
                }
            }
        } else {
            // If no new image, we might need the current image URL or just update name.
            // For simplicity in this step, we'll assume we need to fetch existing user first or just update name.
            // Let's fetch current user from local DB to get existing image URL.
            val currentUser = UserModel.getUserById(userId).value
            saveUser(userId, name, currentUser?.profileImageUrl ?: "", onComplete)
        }
    }

    private fun saveUser(userId: String, name: String, imageUrl: String, onComplete: () -> Unit) {
        val user = User(userId, AuthModel.getCurrentUser()?.email ?: "", name, imageUrl, System.currentTimeMillis())
        UserModel.addUser(user) {
            _isLoading.value = false
            onComplete()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
