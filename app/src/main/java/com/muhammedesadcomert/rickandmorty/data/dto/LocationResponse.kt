package com.muhammedesadcomert.rickandmorty.data.dto

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("created")
    val created: String?,
    @SerializedName("dimension")
    val dimension: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("residents")
    val residents: List<String?>?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("url")
    val url: String?
)
