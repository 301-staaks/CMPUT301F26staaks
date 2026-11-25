package com.example.staaks_campsite_app.session;

import com.example.staaks_campsite_app.data.model.UserType;

data class SessionState(val userId: String, val type: UserType, var adminView: String) {
    val isAdmin get() = type == UserType.ADMIN
}
