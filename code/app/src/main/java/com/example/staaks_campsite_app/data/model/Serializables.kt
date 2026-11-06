package com.example.staaks_campsite_app.data.model

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class admin_campsite(
    val campsiteID: String
)

@Serializable
data class owner_campsite(
    val campsiteID: String
)

@Serializable
data class entree_campsite(
    val campsiteID: String
)