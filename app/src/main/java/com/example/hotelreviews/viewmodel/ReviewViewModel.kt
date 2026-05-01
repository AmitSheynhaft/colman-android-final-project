package com.example.hotelreviews.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.hotelreviews.model.AppDatabase
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.model.ReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReviewRepository
    val allReviews: LiveData<List<Review>>
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val reviewDao = AppDatabase.getDatabase(application).reviewDao()
        repository = ReviewRepository(reviewDao)
        allReviews = repository.allReviews
    }

    fun refreshReviews(onComplete: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        repository.refreshReviews()
        _isLoading.value = false
        onComplete?.invoke()
    }

    fun addReview(review: Review, onComplete: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        repository.addReview(review)
        _isLoading.value = false
        onComplete?.invoke()
    }

    fun updateReview(review: Review, onComplete: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        repository.updateReview(review)
        _isLoading.value = false
        onComplete?.invoke()
    }

    fun deleteReview(review: Review, onComplete: (() -> Unit)? = null) = viewModelScope.launch {
        _isLoading.value = true
        repository.deleteReview(review)
        _isLoading.value = false
        onComplete?.invoke()
    }

    fun getReviewsByUser(userId: String): LiveData<List<Review>> {
        return repository.getReviewsByUser(userId)
    }
}
