package com.example.hotelreviews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.hotelreviews.model.Model
import com.example.hotelreviews.model.Review

class ReviewsViewModel : ViewModel() {
    val reviews: LiveData<List<Review>> = Model.instance.getAllReviews()
    val loadingState: LiveData<Model.LoadingState> = Model.instance.reviewsLoadingState

    val isLoading: LiveData<Boolean> = loadingState.map {
        it == Model.LoadingState.LOADING
    }

    fun refreshReviews() {
        Model.instance.refreshAllReviews()
    }
}
