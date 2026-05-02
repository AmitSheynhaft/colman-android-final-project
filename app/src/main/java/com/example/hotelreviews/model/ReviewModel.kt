package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hotelreviews.base.MyApplication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object ReviewModel {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val localDb = AppDatabase.getDatabase(MyApplication.Globals.appContext!!)
    private val reviewDao = localDb.reviewDao()

    fun getAllReviews(): LiveData<List<Review>> {
        return reviewDao.getAll()
    }

    fun refreshAllReviews(onComplete: () -> Unit = {}) {
        // Delta sync logic
        // For simplicity in this step, getting all and replacing
        reviewsCollection.get().addOnSuccessListener { snapshot ->
            val remoteReviews = snapshot.toObjects(Review::class.java)
            MyApplication.Globals.executorService.execute {
                reviewDao.deleteAll()
                reviewDao.insertAll(*remoteReviews.toTypedArray())
                MyApplication.Globals.mainHandler.post {
                    onComplete()
                }
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun addReview(review: Review, onComplete: () -> Unit) {
        val docRef = reviewsCollection.document()
        review.id = docRef.id
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
        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$name.jpg")
        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri: android.net.Uri ->
                onComplete(uri.toString())
            }
        }.addOnFailureListener {
            onComplete(null)
        }
    }
}
