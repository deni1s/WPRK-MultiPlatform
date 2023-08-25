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
import org.koin.android.ext.android.inject
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_KEY_DATA
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.data.Banner
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic1
import ru.denale.podcastlistener.services.ImageLoadingService


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
                "url" -> openInternetAddress(banner.payload.orEmpty())
                "email" -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "plain/text"
                    intent.putExtra(Intent.EXTRA_EMAIL, banner.payload.orEmpty())
                    startActivity(Intent.createChooser(intent, ""))
                }
                "display" -> {}
                "wave" -> startActivity(Intent(requireContext(), PlayMusic1::class.java).apply {
                    putExtra(EXTRA_MUSIC_TYPE, banner.id)
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