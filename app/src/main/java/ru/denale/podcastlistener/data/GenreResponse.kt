package ru.denale.podcastlistener.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GenreResponse(
    @SerializedName("list")
    val list: ArrayList<Genre>
) : Parcelable