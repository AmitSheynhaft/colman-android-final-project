package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val reviewDao: ReviewDao) {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    val allReviews: LiveData<List<Review>> = reviewDao.getAll()

    fun getReviewsByUser(userId: String): LiveData<List<Review>> {
        return reviewDao.getByUserId(userId)
    }

    suspend fun refreshReviews() {
        try {
            val snapshot = reviewsCollection.get().await()
            val remoteReviews = snapshot.toObjects(Review::class.java)
            reviewDao.deleteAll()
            reviewDao.insertAll(*remoteReviews.toTypedArray())
        } catch (e: Exception) {
            // Handle error (e.g., log it)
        }
    }

    suspend fun addReview(review: Review) {
        val docRef = reviewsCollection.document()
        review.id = docRef.id
        docRef.set(review).await()
        reviewDao.insert(review)
    }

    suspend fun updateReview(review: Review) {
        reviewsCollection.document(review.id).set(review).await()
        reviewDao.insert(review)
    }

    suspend fun deleteReview(review: Review) {
        reviewsCollection.document(review.id).delete().await()
        reviewDao.delete(review)
    }
}
