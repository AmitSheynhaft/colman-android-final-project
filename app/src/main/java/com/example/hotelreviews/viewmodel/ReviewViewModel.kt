package com.example.hotelreviews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.hotelreviews.model.Model
import com.example.hotelreviews.model.Review

class ReviewViewModel : ViewModel() {
    val allReviews: LiveData<List<Review>> = Model.instance.getAllReviews()
    val userReviews: LiveData<List<Review>> = Model.instance.getUserReviews()
    
    val loadingState: LiveData<Model.LoadingState> = Model.instance.reviewsLoadingState

    val isLoading: LiveData<Boolean> = loadingState.map {
        it == Model.LoadingState.LOADING
    }

    fun refreshReviews() {
        Model.instance.refreshAllReviews()
    }

    fun addReview(review: Review, onComplete: () -> Unit) {
        Model.instance.addReview(review, onComplete)
    }
}
