package ru.denale.podcastlistener.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthorResponse(
    @SerializedName("list")
    val list: List<Author>,
    @SerializedName("warning")
    val warning: String?
):Parcelable