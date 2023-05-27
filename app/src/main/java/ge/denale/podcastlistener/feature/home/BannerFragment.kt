package ge.denale.podcastlistener.feature.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.EXTRA_KEY_DATA
import ge.denale.podcastlistener.data.Banner
import ge.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ge.denale.podcastlistener.services.ImageLoadingService
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import ge.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ge.denale.podcastlistener.feature.activities.playmusic.PlayMusic
import org.koin.android.ext.android.inject
import java.lang.IllegalStateException


class BannerFragment : Fragment() {
    val imageLoadingService : ImageLoadingService by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val imageView =  inflater.inflate(R.layout.fragment_banner, container, false) as ImageView
        val banner = requireArguments().getParcelable<Banner>(EXTRA_KEY_DATA)?: throw IllegalStateException("Banner Cannot Be null")
        imageLoadingService.loadGif(imageView,banner.imageUrl, requireContext())
        imageView.setOnClickListener {
            when (banner.type) {
                "url" -> openInternetAddress(banner.url.orEmpty())
                "premium" -> openInternetAddress(banner.url.orEmpty())
                else -> startActivity(Intent(requireContext(), PlayMusic::class.java).apply {
                    putExtra(EXTRA_MUSIC_TYPE, banner.type)
                })
            }
        }
        return imageView
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

    companion object {
        fun newInstance(banner: Banner):BannerFragment{
            return BannerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_KEY_DATA, banner)
                }
            }
        }
    }

}