package ru.denale.podcastlistener.feature.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.nativeads.*
import com.yandex.mobile.ads.nativeads.template.NativeBannerView
import ru.denale.podcastlistener.R

class AdvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val adView: NativeBannerView = itemView.findViewById(R.id.native_ad)

    fun bindCategory(native: NativeAd) {
        adView.setAd(native)
    }
}