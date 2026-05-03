package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hotelreviews.base.MyApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object ReviewModel {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val localDb by lazy { AppDatabase.getDatabase(MyApplication.Globals.appContext!!) }
    private val reviewDao by lazy { localDb.reviewDao() }

    private const val LAST_UPDATED = "lastUpdated"

    fun getAllReviews(): LiveData<List<Review>> {
        return reviewDao.getAllNonDeleted()
    }

    fun getReviewsByUserId(userId: String): LiveData<List<Review>> {
        return reviewDao.getByUserIdNonDeleted(userId)
    }

    fun refreshAllReviews(onComplete: () -> Unit = {}) {
        MyApplication.Globals.executorService.execute {
            val count = reviewDao.getCount()
            val lastUpdated = if (count == 0) 0L else {
                MyApplication.Globals.appContext?.getSharedPreferences("TAG", android.content.Context.MODE_PRIVATE)
                    ?.getLong(LAST_UPDATED, 0L) ?: 0L
            }

            reviewsCollection.whereGreaterThan("lastUpdated", lastUpdated)
                .get().addOnSuccessListener { snapshot ->
                    val remoteReviews = snapshot.toObjects(Review::class.java)
                    if (remoteReviews.isNotEmpty()) {
                        MyApplication.Globals.executorService.execute {
                            var latest = lastUpdated
                            for (review in remoteReviews) {
                                if (review.isDeleted) {
                                    reviewDao.delete(review)
                                } else {
                                    reviewDao.insert(review)
                                }
                                if (review.lastUpdated > latest) {
                                    latest = review.lastUpdated
                                }
                            }
                            MyApplication.Globals.appContext?.getSharedPreferences("TAG", android.content.Context.MODE_PRIVATE)
                                ?.edit()?.putLong(LAST_UPDATED, latest)?.apply()
                            
                            MyApplication.Globals.mainHandler.post {
                                onComplete()
                            }
                        }
                    } else {
                        onComplete()
                    }
                }.addOnFailureListener {
                    onComplete()
                }
        }
    }

    fun addReview(review: Review, onComplete: () -> Unit) {
        if (review.id.isEmpty()) {
            val docRef = reviewsCollection.document()
            review.id = docRef.id
        }
        review.lastUpdated = System.currentTimeMillis()
        review.isDeleted = false
        reviewsCollection.document(review.id).set(review).addOnSuccessListener {
            MyApplication.Globals.executorService.execute {
                reviewDao.insert(review)
                MyApplication.Globals.mainHandler.post {
                    onComplete()
                }
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun deleteReview(review: Review, onComplete: () -> Unit) {
        review.isDeleted = true
        review.lastUpdated = System.currentTimeMillis()
        reviewsCollection.document(review.id).set(review).addOnSuccessListener {
            MyApplication.Globals.executorService.execute {
                reviewDao.insert(review)
                MyApplication.Globals.mainHandler.post {
                    onComplete()
                }
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun updateUserInReviews(userId: String, newName: String, newImageUrl: String, onComplete: () -> Unit) {
        // 1. Update Firestore
        reviewsCollection.whereEqualTo("userId", userId).get().addOnSuccessListener { snapshot ->
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "userName", newName, "userProfileImageUrl", newImageUrl, "lastUpdated", System.currentTimeMillis())
            }
            batch.commit().addOnSuccessListener {
                // 2. Update Room
                MyApplication.Globals.executorService.execute {
                    reviewDao.updateUserInfo(userId, newName, newImageUrl)
                    MyApplication.Globals.mainHandler.post {
                        onComplete()
                    }
                }
            }.addOnFailureListener { onComplete() }
        }.addOnFailureListener { onComplete() }
    }

    fun uploadImage(bitmap: android.graphics.Bitmap, name: String, onComplete: (String?) -> Unit) {
        MyApplication.Globals.executorService.execute {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/$name.jpg")

            // Optimized for reviews: 800px is enough for mobile viewing
            val width = bitmap.width
            val height = bitmap.height
            val ratio = width.toFloat() / height.toFloat()
            val maxDim = 800
            var finalWidth = width
            var finalHeight = height

            if (width > maxDim || height > maxDim) {
                if (width > height) {
                    finalWidth = maxDim
                    finalHeight = (maxDim / ratio).toInt()
                } else {
                    finalHeight = maxDim
                    finalWidth = (maxDim * ratio).toInt()
                }
            }

            val scaledBitmap = if (finalWidth != width) {
                android.graphics.Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
            } else {
                bitmap
            }

            val baos = java.io.ByteArrayOutputStream()
            // Quality 60 is a good balance for speed and clarity
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data).continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                val result = if (task.isSuccessful) task.result.toString() else null
                MyApplication.Globals.mainHandler.post {
                    onComplete(result)
                }
            }
        }
    }
}
