package com.example.hotelreviews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.model.ReviewModel

class ReviewViewModel : ViewModel() {
    val allReviews: LiveData<List<Review>> = ReviewModel.getAllReviews()
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun refreshReviews() {
        _isLoading.value = true
        ReviewModel.refreshAllReviews {
            _isLoading.value = false
        }
    }

    fun addReview(review: Review, onComplete: () -> Unit) {
        _isLoading.value = true
        ReviewModel.addReview(review) {
            _isLoading.value = false
            onComplete()
        }
    }
}
