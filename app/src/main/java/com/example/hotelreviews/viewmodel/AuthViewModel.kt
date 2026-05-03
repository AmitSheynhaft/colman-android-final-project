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
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val _user = MutableLiveData<FirebaseUser?>(AuthModel.getCurrentUser())
    val user: LiveData<FirebaseUser?> = _user

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = MyApplication.Globals.appContext?.getString(R.string.error_fill_all_fields)
            return
        }
        _isLoading.value = true
        AuthModel.login(email, pass) { success, error ->
            _isLoading.value = false
            if (success) {
                _user.value = AuthModel.getCurrentUser()
                onSuccess()
            } else {
                _errorMessage.value = error ?: "Login failed"
            }
        }
    }

    fun register(email: String, pass: String, name: String, bitmap: Bitmap?, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _errorMessage.value = MyApplication.Globals.appContext?.getString(R.string.error_fill_all_fields)
            return
        }
        _isLoading.value = true
        AuthModel.register(email, pass) { success, error ->
            if (success) {
                val uid = AuthModel.getCurrentUser()?.uid ?: ""
                if (bitmap != null) {
                    UserModel.uploadProfileImage(bitmap, uid) { imageUrl ->
                        val user = User(id = uid, email = email, name = name, profileImageUrl = imageUrl ?: "")
                        UserModel.addUser(user) {
                            _isLoading.value = false
                            _user.value = AuthModel.getCurrentUser()
                            onSuccess()
                        }
                    }
                } else {
                    val user = User(id = uid, email = email, name = name, profileImageUrl = "")
                    UserModel.addUser(user) {
                        _isLoading.value = false
                        _user.value = AuthModel.getCurrentUser()
                        onSuccess()
                    }
                }
            } else {
                _isLoading.value = false
                _errorMessage.value = error ?: "Registration failed"
            }
        }
    }

    fun logout() {
        AuthModel.logout()
        _user.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
