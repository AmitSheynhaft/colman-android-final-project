package com.example.hotelreviews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelreviews.model.Model

class AuthViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        Model.instance.login(email, pass) { success, error ->
            _isLoading.postValue(false)
            if (success) {
                onSuccess()
            } else {
                _errorMessage.postValue(error)
            }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        Model.instance.register(email, pass) { success, error ->
            _isLoading.postValue(false)
            if (success) {
                onSuccess()
            } else {
                _errorMessage.postValue(error)
            }
        }
    }

    fun logout() {
        Model.instance.logout()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return Model.instance.isUserLoggedIn()
    }
}
