package com.example.staaks_campsite_app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot


enum class UserType { ENTRANT, OWNER, ADMIN }
sealed class User {
    @DocumentId var id: String = ""
    var name: String = ""
    var email: String = ""
    var phone: String = ""
    @ServerTimestamp var createdAt: Timestamp? = null
    abstract val type: UserType
}

@IgnoreExtraProperties
class Entrant: User () {
    override val type get() = UserType.ENTRANT
}

@IgnoreExtraProperties
class Owner: User () {
    override val type get() = UserType.OWNER
}

@IgnoreExtraProperties
class Admin: User () {
    override val type get() = UserType.ADMIN
}

fun DocumentSnapshot.toUser(): User? =
    when (getString("type")?.let { UserType.valueOf(it) }) {
        UserType.ENTRANT -> toObject(Entrant::class.java)
        UserType.OWNER -> toObject(Owner::class.java)
        UserType.ADMIN -> toObject(Admin::class.java)
        null -> null
    }