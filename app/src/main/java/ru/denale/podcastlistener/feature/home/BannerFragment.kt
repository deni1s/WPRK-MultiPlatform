package ru.denale.podcastlistener.feature.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import org.json.JSONObject
import org.koin.android.ext.android.inject
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.data.Banner
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic1
import ru.denale.podcastlistener.services.ImageLoadingService

class BannerFragment : Fragment() {
    val imageLoadingService: ImageLoadingService by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val banner =
            requireArguments().getParcelable<Banner>(EXTRA_KEY_DATA) ?: throw IllegalStateException(
                "Banner Cannot Be null"
            )
        return populateImageView(inflater, container, banner)
    }

    private fun populateImageView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        banner: Banner
    ): View {
        val imageView = inflater.inflate(R.layout.fragment_banner, container, false) as ImageView
        imageLoadingService.loadGif(imageView, banner.imageUrl, requireContext())
        imageView.setOnClickListener {
            when (banner.type) {
                "url" -> openInternetAddress(banner.payload.orEmpty())
                "email" -> sendEmail(banner.payload.orEmpty())
                "share" -> actionShare(banner.payload.orEmpty())
                "display" -> {}
                "wave" -> startActivity(Intent(requireContext(), PlayMusic1::class.java).apply {
                    putExtra(EXTRA_MUSIC_TYPE, banner.id)
                })
            }
        }
        return imageView
    }

    private fun actionShare(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "plain/text"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        try {
            startActivity(Intent.createChooser(intent, ""))
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun openInternetAddress(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse(url)
            }
            context?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.application_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(jsonString: String) {
        val jsonString = JSONObject(jsonString)
        val emailTo = jsonString.getString("sender_address")
        val subject = jsonString.getString("subject")
        val text = jsonString.getString("main_text")
        try {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("mailto:$emailTo?subject=$subject&body=$text")
            })
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.putExtra(Intent.EXTRA_EMAIL, emailTo)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, text)
            try {
                startActivity(Intent.createChooser(intent, "Выберите почтовый клиент"))
            } catch (e: ActivityNotFoundException) {
            }
        }
    }

    companion object {
        fun newInstance(banner: Banner): BannerFragment {
            return BannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_KEY_DATA, banner)
                }
            }
        }
    }
}