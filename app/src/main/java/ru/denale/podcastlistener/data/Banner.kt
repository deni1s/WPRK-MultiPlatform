package ru.denale.podcastlistener.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Banner(
    @SerializedName("id")
    val id: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("payload")
    val payload: String?
):Parcelable


/*{"id":1,
   "image_url":"https:\/\/s17.picofile.com\/file\/8429645276\/banner1.jpg"}*/