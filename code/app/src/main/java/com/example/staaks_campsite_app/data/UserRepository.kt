package com.example.staaks_campsite_app.data

import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _users = mutableStateListOf<User>()

    init {
        usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _users.clear()

            snapshot?.documents?.forEach { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _users.add(user)
                }
            }
        }

        listOf(User("1"), User("2"), User("3")).forEach { addUser(it) }
    }

    val users: List<User>
        get() = _users

    fun addUser(user: User) {
        usersRef.document(user.id).set(user)
    }

    fun deleteUser(user: User) {
        usersRef.document(user.id).delete()
    }
}