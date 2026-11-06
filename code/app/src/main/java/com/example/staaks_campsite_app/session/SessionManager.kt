package com.example.staaks_campsite_app.session

import com.example.staaks_campsite_app.data.model.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    private val _state = MutableStateFlow<SessionState?>(null)
    val state: StateFlow<SessionState?> = _state
    fun onLogin(uid: String, type: UserType) { _state.value = SessionState(uid, type) }
}