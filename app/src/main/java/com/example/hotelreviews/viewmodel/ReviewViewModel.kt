package com.example.hotelreviews.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hotelreviews.model.AuthModel
import com.example.hotelreviews.model.Review
import com.example.hotelreviews.model.ReviewModel
import java.util.UUID

class ReviewViewModel : ViewModel() {
    val allReviews: LiveData<List<Review>> = ReviewModel.getAllReviews()
    
    fun getReviewsByUserId(userId: String): LiveData<List<Review>> {
        return ReviewModel.getReviewsByUserId(userId)
    }
    
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
        review.userId = AuthModel.getCurrentUser()?.uid ?: ""
        ReviewModel.addReview(review) {
            _isLoading.value = false
            onComplete()
        }
    }

    fun uploadImageAndAddReview(review: Review, bitmap: Bitmap?, onComplete: () -> Unit) {
        _isLoading.value = true
        review.userId = AuthModel.getCurrentUser()?.uid ?: ""
        
        if (bitmap != null) {
            val fileName = UUID.randomUUID().toString()
            ReviewModel.uploadImage(bitmap, fileName) { imageUrl ->
                review.imageUrl = imageUrl ?: ""
                ReviewModel.addReview(review) {
                    _isLoading.value = false
                    onComplete()
                }
            }
        } else {
            ReviewModel.addReview(review) {
                _isLoading.value = false
                onComplete()
            }
        }
    }

    fun deleteReview(review: Review, onComplete: () -> Unit) {
        _isLoading.value = true
        ReviewModel.deleteReview(review) {
            _isLoading.value = false
            onComplete()
        }
    }
    
    fun getReviewById(reviewId: String): LiveData<Review?> {
        // We can implement a direct DAO call for this
        val result = MutableLiveData<Review?>()
        allReviews.value?.find { it.id == reviewId }?.let {
            result.value = it
        }
        return result
    }
}
