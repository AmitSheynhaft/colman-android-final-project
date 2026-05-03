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
        val lastUpdated = MyApplication.Globals.appContext?.getSharedPreferences("TAG", android.content.Context.MODE_PRIVATE)
            ?.getLong(LAST_UPDATED, 0L) ?: 0L

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

    fun addReview(review: Review, onComplete: () -> Unit) {
        val docRef = reviewsCollection.document()
        review.id = docRef.id
        review.lastUpdated = System.currentTimeMillis()
        review.isDeleted = false
        docRef.set(review).addOnSuccessListener {
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

    fun uploadImage(bitmap: android.graphics.Bitmap, name: String, onComplete: (String?) -> Unit) {
        MyApplication.Globals.executorService.execute {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/$name.jpg")
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri: android.net.Uri ->
                    MyApplication.Globals.mainHandler.post {
                        onComplete(uri.toString())
                    }
                }.addOnFailureListener {
                    MyApplication.Globals.mainHandler.post { onComplete(null) }
                }
            }.addOnFailureListener {
                MyApplication.Globals.mainHandler.post {
                    onComplete(null)
                }
            }
        }
    }
}
