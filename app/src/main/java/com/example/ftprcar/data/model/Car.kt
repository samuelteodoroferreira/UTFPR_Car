package com.example.ftprcar.data.model

import com.google.gson.annotations.SerializedName

data class Car(
    val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place? = null
)
