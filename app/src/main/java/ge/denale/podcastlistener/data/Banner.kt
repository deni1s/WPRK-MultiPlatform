package ge.denale.podcastlistener.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Banner(
    val id: String,
    val imageUrl: String,
    val type: String,
    val url: String?
):Parcelable


/*{"id":1,
   "image_url":"https:\/\/s17.picofile.com\/file\/8429645276\/banner1.jpg"}*/