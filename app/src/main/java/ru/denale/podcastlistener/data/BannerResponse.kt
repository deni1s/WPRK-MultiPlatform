package ru.denale.podcastlistener.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep

data class BannerResponse(
    @SerializedName("list")
    val list: ArrayList<Banner>
):Parcelable