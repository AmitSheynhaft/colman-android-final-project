package com.example.hotelreviews.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ModelFirebase {
    private val db = FirebaseFirestore.getInstance()

    fun getAllReviews(since: Long, callback: (List<Review>) -> Unit) {
        db.collection(Review.COLLECTION)
            .whereGreaterThan(Review.LAST_UPDATED, since)
            .get()
            .addOnCompleteListener { task ->
                val list = mutableListOf<Review>()
                if (task.isSuccessful) {
                    for (doc in task.result!!) {
                        reviewFromMap(doc.data)?.let { list.add(it) }
                    }
                }
                callback(list)
            }
    }

    fun addReview(review: Review, callback: () -> Unit) {
        db.collection(Review.COLLECTION)
            .document(review.id)
            .set(reviewToMap(review))
            .addOnCompleteListener { callback() }
    }

    fun getUser(uid: String, callback: (User?) -> Unit) {
        db.collection(User.COLLECTION).document(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result!!.exists()) {
                    userFromMap(task.result!!.data!!)?.let { callback(it) }
                } else {
                    callback(null)
                }
            }
    }

    fun updateUser(user: User, callback: () -> Unit) {
        db.collection(User.COLLECTION).document(user.id)
            .set(userToMap(user))
            .addOnCompleteListener { callback() }
    }

    private fun userToMap(user: User): Map<String, Any?> = mapOf(
        "id" to user.id,
        "username" to user.username,
        "fullName" to user.fullName,
        "email" to user.email,
        "profileImageUrl" to user.profileImageUrl,
        "lastUpdated" to FieldValue.serverTimestamp()
    )

    private fun userFromMap(map: Map<String, Any?>): User? {
        val ts = map["lastUpdated"] as? Timestamp
        return User(
            id = map["id"] as? String ?: "",
            username = map["username"] as? String ?: "",
            fullName = map["fullName"] as? String ?: "",
            email = map["email"] as? String ?: "",
            profileImageUrl = map["profileImageUrl"] as? String ?: "",
            lastUpdated = ts?.seconds ?: 0L
        )
    }

    private fun reviewToMap(review: Review): Map<String, Any?> = mapOf(
        "id" to review.id,
        "userId" to review.userId,
        "hotelName" to review.hotelName,
        "rating" to review.rating,
        "description" to review.description,
        "imageUrl" to review.imageUrl,
        "lastUpdated" to FieldValue.serverTimestamp(),
        "isDeleted" to review.isDeleted
    )

    private fun reviewFromMap(map: Map<String, Any?>): Review? {
        val ts = map["lastUpdated"] as? Timestamp
        return Review(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            hotelName = map["hotelName"] as? String ?: "",
            rating = (map["rating"] as? Long)?.toInt() ?: 0,
            description = map["description"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String ?: "",
            lastUpdated = ts?.seconds ?: 0L,
            isDeleted = map["isDeleted"] as? Boolean ?: false
        )
    }
}
