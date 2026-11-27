package com.example.staaks_campsite_app.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/** the user document field that stores the notification toggle */
const val NOTIFICATIONS_ENABLED_FIELD = "notificationsEnabled"

/**
 * decides whether a notification may show as a pop-up (US 01.10.01)
 *
 * notifications are always saved to the inbox. this only decides the pop-up,
 * so whatever code shows pop-ups should call this first.
 * verification codes for two-step verification ignore the setting.
 *
 * @param notificationsEnabled the user's notificationsEnabled value
 * @param isVerificationCode true for a two-step verification code
 * @return true if the pop-up should show
 */
fun shouldShowPopup(notificationsEnabled: Boolean, isVerificationCode: Boolean = false): Boolean {
    return notificationsEnabled || isVerificationCode
}

/**
 * saves the notification opt-out on the user document (US 01.10.01)
 *
 * the setting lives on the user document, so it survives closing the app
 * and is the same on every device that loads this user.
 */
object NotificationPreferences {

    /**
     * turns pop-up notifications on or off for a user
     *
     * only changes this one field, so it can't overwrite a profile edit
     *
     * @param userId the user's id
     * @param enabled true for on, false to opt out
     * @param onComplete called with true if it saved, false if it failed
     * (for example the user document doesn't exist)
     */
    fun setEnabled(userId: String, enabled: Boolean, onComplete: (Boolean) -> Unit = {}) {
        if (userId.isBlank()) {
            onComplete(false)
            return
        }

        Firebase.firestore.collection("users")
            .document(userId)
            .update(NOTIFICATIONS_ENABLED_FIELD, enabled)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }
}
