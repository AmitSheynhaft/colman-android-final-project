package com.example.hotelreviews.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        _user.value = auth.currentUser
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onSuccess()
                } else {
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun register(email: String, pass: String, imageUri: Uri?, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (imageUri != null && user != null) {
                        // In a production app, you would upload the image to Firebase Storage here
                        // For now, we'll just update the profile with the local URI or placeholder
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .build()
                        
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            _isLoading.value = false
                            _user.value = auth.currentUser
                            onSuccess()
                        }
                    } else {
                        _isLoading.value = false
                        _user.value = auth.currentUser
                        onSuccess()
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = task.exception?.message ?: "Registration failed"
                }
            }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
