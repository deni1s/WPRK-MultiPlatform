package ru.denale.podcastlistener.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("showAdverisement")
    val showAdverisement: Boolean
)