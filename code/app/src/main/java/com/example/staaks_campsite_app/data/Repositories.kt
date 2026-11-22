package com.example.staaks_campsite_app.data

/**
 * shared repository instances for the whole app
 *
 * screens use these instead of creating their own, so each firestore
 * listener is only added once
 */
object Repositories {
    val notifications by lazy { NotificationRepository() }
}
