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

    // Computed every time so it always reflects the current Firebase auth state.
    private val currentUserId: String?
        get() = AuthModel.getCurrentUser()?.uid

    // Tracks the last user ID for which an observer was registered, so we can
    // clean it up even after Firebase has already signed out (currentUserId == null).
    private var lastFetchedUserId: String? = null

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private var userObserver: androidx.lifecycle.Observer<User>? = null

    fun fetchUser() {
        val userId = currentUserId ?: return
        lastFetchedUserId = userId

        userObserver?.let { existingObserver ->
            UserModel.getUserById(userId).removeObserver(existingObserver)
        }

        // Always refresh from local DB first
        val observer = androidx.lifecycle.Observer<User> { value ->
            if (value != null) {
                _user.postValue(value)
            }
        }
        userObserver = observer
        UserModel.getUserById(userId).observeForever(observer)
        
        // Then try to refresh from network
        UserModel.refreshUser(userId)
    }

    override fun onCleared() {
        super.onCleared()
        lastFetchedUserId?.let { id ->
            userObserver?.let { UserModel.getUserById(id).removeObserver(it) }
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
            // Use current value from LiveData, fallback to empty but don't overwrite with empty if we have one
            val currentImageUrl = _user.value?.profileImageUrl ?: ""
            saveUser(userId, name, currentImageUrl, onComplete)
        }
    }

    private fun saveUser(userId: String, name: String, imageUrl: String, onComplete: () -> Unit) {
        val email = AuthModel.getCurrentUser()?.email ?: _user.value?.email ?: ""
        val user = User(userId, email, name, imageUrl, System.currentTimeMillis())
        UserModel.addUser(user) {
            // Also update the user's details in all their existing reviews
            com.example.hotelreviews.model.ReviewModel.updateUserInReviews(userId, name, imageUrl) {
                // Update local LiveData immediately for instant UI feedback
                _user.postValue(user)
                _isLoading.value = false
                onComplete()
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearUserData() {
        lastFetchedUserId?.let { id ->
            userObserver?.let { UserModel.getUserById(id).removeObserver(it) }
        }
        userObserver = null
        lastFetchedUserId = null
        _user.value = null
        _errorMessage.value = null
        _isLoading.value = false
    }
}
