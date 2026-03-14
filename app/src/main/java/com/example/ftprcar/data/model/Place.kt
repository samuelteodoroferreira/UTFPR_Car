package com.example.ftprcar.data.model

import com.google.gson.annotations.SerializedName

data class Place(
    val lat: Double,
    @SerializedName("long") val lng: Double
)
