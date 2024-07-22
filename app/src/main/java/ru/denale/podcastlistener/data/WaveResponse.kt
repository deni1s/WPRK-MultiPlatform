package ru.denale.podcastlistener.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WaveResponse(
    @SerializedName("podcasts")
    val podcasts: List<Music>,
    @SerializedName("title")
    val title: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("warning")
    val warning: String?
)