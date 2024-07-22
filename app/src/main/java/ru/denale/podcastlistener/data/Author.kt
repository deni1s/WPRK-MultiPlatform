package ru.denale.podcastlistener.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class Author(
    @SerializedName("id")
    val id: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("warningDescription")
    val warningDescription: String?
):Parcelable


/*{"id":1,
    "name":"DJ",
    "family":"Snake",
    "image_url":"https:\/\/images.thequint.com\/thequint%2F2019-02%2F7f3e28c0-19a2-4b7e-a1c6-e6584a332873%2Fa4c428724db4b4cf2cc45c09d2dd31f6.jpg?w=1200&auto=format%2Ccompress&ogImage=true"}*/