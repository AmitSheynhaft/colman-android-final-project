package com.example.hotelreviews.model

import androidx.lifecycle.LiveData
import com.example.hotelreviews.base.MyApplication
import com.google.firebase.firestore.FirebaseFirestore

object UserModel {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val localDb by lazy { AppDatabase.getDatabase(MyApplication.Globals.appContext!!) }
    private val userDao by lazy { localDb.userDao() }

    private const val LAST_UPDATED = "user_lastUpdated"

    fun getUserById(id: String): LiveData<User> {
        return userDao.getUserById(id)
    }

    fun refreshUser(id: String, onComplete: () -> Unit = {}) {
        val lastUpdated = MyApplication.Globals.appContext?.getSharedPreferences("TAG", android.content.Context.MODE_PRIVATE)
            ?.getLong(LAST_UPDATED + id, 0L) ?: 0L

        usersCollection.document(id).get().addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(User::class.java)
            if (user != null && user.lastUpdated > lastUpdated) {
                MyApplication.Globals.executorService.execute {
                    if (user.isDeleted) {
                        userDao.delete(user)
                    } else {
                        userDao.insert(user)
                    }
                    MyApplication.Globals.appContext?.getSharedPreferences("TAG", android.content.Context.MODE_PRIVATE)
                        ?.edit()?.putLong(LAST_UPDATED + id, user.lastUpdated)?.apply()
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

    fun addUser(user: User, onComplete: () -> Unit) {
        user.lastUpdated = System.currentTimeMillis()
        user.isDeleted = false
        usersCollection.document(user.id).set(user).addOnSuccessListener {
            MyApplication.Globals.executorService.execute {
                userDao.insert(user)
                MyApplication.Globals.mainHandler.post {
                    onComplete()
                }
            }
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun uploadProfileImage(bitmap: android.graphics.Bitmap, userId: String, onComplete: (String?) -> Unit) {
        MyApplication.Globals.executorService.execute {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profile_images/$userId.jpg")
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos)
            val data = baos.toByteArray()

            imageRef.putBytes(data).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri: android.net.Uri ->
                    MyApplication.Globals.mainHandler.post {
                        onComplete(uri.toString())
                    }
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("UserModel", "Image upload failed: ${exception.message}", exception)
                MyApplication.Globals.mainHandler.post {
                    onComplete(null)
                }
            }
        }
    }
}
