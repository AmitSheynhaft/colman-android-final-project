package com.example.hotelreviews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.hotelreviews.model.Model
import com.example.hotelreviews.model.User

class ProfileViewModel : ViewModel() {

    val user: LiveData<User?> = Model.instance.getCurrentUser()
    val loadingState: LiveData<Model.LoadingState> = Model.instance.userLoadingState

    val isLoading: LiveData<Boolean> = loadingState.map {
        it == Model.LoadingState.LOADING
    }

    fun logout() {
        Model.instance.logout()
    }

    fun refreshProfile() {
        Model.instance.refreshUser()
    }

    fun updateProfile(username: String, fullName: String, imageUrl: String, onComplete: () -> Unit) {
        val currentUser = user.value ?: return
        currentUser.username = username
        currentUser.fullName = fullName
        currentUser.profileImageUrl = imageUrl
        Model.instance.updateUser(currentUser, onComplete)
    }
}
