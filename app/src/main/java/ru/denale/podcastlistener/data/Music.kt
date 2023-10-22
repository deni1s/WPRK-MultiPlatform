package ru.denale.podcastlistener.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Music(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("authorId")
    val authorId: String?,
    @SerializedName("authorIds")
    val authorIds: List<String>?,
    @SerializedName("genreId")
    val genreId: String?,
    @SerializedName("genreIds")
    val genreIds: List<String>?,
    @SerializedName("durationString")
    val durationString: String?,
    @SerializedName("mediaUrl")
    val mediaUrl: String,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("warningDescription")
    val warningDescription: String?
):Parcelable



/*
    {"id":200,
    "category_id":10,
    "name":"Selfish Love (with Selena Gomez)",
    "Singer":"DJ Snake","time":"02:48",
    "image_url":"https:\/\/music-daily.ir\/wp-content\/uploads\/2021\/03\/5.DJ-Snake-Selena-Gomez-Selfish-Love.jpg",
    "music_url":"https:\/\/musicfeed.ir\/files\/dir\/2021\/3\/DJ%20Snake%20Selfish%20Love%20(with%20Selena%20Gomez).mp3"}
*/