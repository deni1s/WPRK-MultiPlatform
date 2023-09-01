package ru.denale.podcastlistener.feature.adapter

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.nativeads.*
import com.yandex.mobile.ads.nativeads.template.NativeBannerView
import com.yandex.mobile.ads.nativeads.template.SizeConstraint
import com.yandex.mobile.ads.nativeads.template.appearance.ImageAppearance
import com.yandex.mobile.ads.nativeads.template.appearance.NativeTemplateAppearance
import ru.denale.podcastlistener.R


class AdvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val adView: BannerAdView = itemView.findViewById(R.id.native_ad)

    fun bindCategory(native: NativeAd) {
        showAd(native)
    }

    private fun showAd(nativeAd: NativeAd) {
        //val nativeAdViewBinder = NativeAdViewBinder.Builder(adView.).build()
        nativeAd.setNativeAdEventListener(NativeAdEventLogger())
//        BannerAdSize.inlineSize(adView.context, adView.context.resources.getDimensionPixelSize(R.dimen.width), maxAdHeight)
//        adView.setAd(nativeAd)
//        try {
//            nativeAd.bindNativeAd(nativeAdViewBinder)
//            nativeAd.setNativeAdEventListener(NativeAdEventLogger())
//        } catch (exception: NativeAdException) {
//           Log.e("advert error", exception.message.toString())
//        }
    }

    private fun configureAppearance() : NativeTemplateAppearance {
        return NativeTemplateAppearance.Builder()
            .withImageAppearance(
                ImageAppearance.Builder()
                    .setWidthConstraint(
                        SizeConstraint(
                            SizeConstraint.SizeConstraintType.FIXED,
                            convertDpToPixel(120f, adView.context)
                        )
                    ).build()
            )
            .build()
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    private inner class NativeAdEventLogger : NativeAdEventListener {

        override fun onAdClicked() {
            // Called when a click is recorded for an ad.
        }

        override fun onLeftApplication() {
            // Called when user is about to leave application (e.g., to go to the browser), as a result of clicking on the ad.
        }

        override fun onReturnedToApplication() {
            // Called when user returned to application after click.
        }

        override fun onImpression(data: ImpressionData?) {
            // Called when an impression is recorded for an ad.
        }
    }
}