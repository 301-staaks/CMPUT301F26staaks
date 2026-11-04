package com.example.staaks_campsite_app.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.example.staaks_campsite_app.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.UUID
import android.provider.Settings
import com.example.staaks_campsite_app.data.model.Admin
import com.example.staaks_campsite_app.data.model.Entrant
import com.example.staaks_campsite_app.data.model.Owner
import com.example.staaks_campsite_app.data.model.UserType
import com.example.staaks_campsite_app.data.model.toUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
                val typeString = document.getString("type")
                val userClass = when (typeString) {
                    "ENTRANT" -> Entrant::class.java
                    "OWNER" -> Owner::class.java
                    "ADMIN" -> Admin::class.java
                    else -> null
                }
                if (userClass != null) {
                    val user = document.toObject(userClass)
                    if (user != null) {
                        _users.add(user)
                    }
                }
            }
        }
    }

    val users: List<User>
        get() = _users

    fun addUser(user: User) {
        usersRef.document(user.id).set(user)
    }

    fun deleteUser(user: User) {
        usersRef.document(user.id).delete()
    }

    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val reg = usersRef.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(snapshot?.documents?.mapNotNull { doc ->
                when (doc.getString("type")) {
                    "ENTRANT" -> doc.toObject(Entrant::class.java)
                    "OWNER" -> doc.toObject(Owner::class.java)
                    "ADMIN" -> doc.toObject(Admin::class.java)
                    else -> null
                }
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun observeUser(id: String): Flow<User?> = callbackFlow {
        val reg = usersRef.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(when (snapshot?.getString("type")) {
                "ENTRANT" -> snapshot.toObject(Entrant::class.java)
                "OWNER" -> snapshot.toObject(Owner::class.java)
                "ADMIN" -> snapshot.toObject(Admin::class.java)
                else -> null
            })
        }
        awaitClose { reg.remove() }
    }
}

@SuppressLint("HardwareIds")
fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: UUID.randomUUID().toString()
}

fun createUser(
    context: Context,
    type: UserType,
    name: String,
    email: String,
    phone: String
) {
    val user: User = when (type) {
        UserType.ENTRANT -> Entrant()
        UserType.OWNER -> Owner()
        UserType.ADMIN -> Admin()
    }
    user.name = name
    user.email = email
    user.phone = phone

    Firebase.firestore.collection("users")
        .document(getDeviceId(context))
        .set(user)
}