package ru.denale.podcastlistener.feature.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.*
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ru.denale.podcastlistener.feature.adapter.BannerAdapter
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import ru.denale.podcastlistener.feature.authors.AuthorsActivity
import ru.denale.podcastlistener.feature.category.CategoryActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic2
import java.util.*

class HomeFragment : MusicPlayerOnlineFragment(), MusicAdapter.SetOnClick,
    CategoryAdapter.OnClickCategory, AuthorsAdapter.OnClickAuthor {
    val homeViewModel: HomeViewModel by viewModel()
    val categoryAdapter: CategoryAdapter by inject()
    val authorsAdapter: AuthorsAdapter by inject()
    var topBannerAdapter: BannerAdapter? = null
    var bottomBannerAdapter: BannerAdapter? = null
    private var isAdvAllowed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateTopAdBanner()
        recycle_category.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recycle_category.adapter = categoryAdapter
        categoryAdapter.onClickCategory = this

        recycle_authors.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recycle_authors.adapter = authorsAdapter
        authorsAdapter.onAuthorClicked = this

        homeViewModel.topBannerLiveData.observe(viewLifecycleOwner) {
            topBannerIndicator.isVisible = it.size > 1
            if (it.isEmpty()) return@observe
            topBannerAdapter = BannerAdapter(this, it)
            topBannerSliderViewPager.adapter = topBannerAdapter
            topBannerSliderViewPager.post {
                val viewPagerHeight = (((topBannerSliderViewPager.width - convertDpToPixel(
                    32f,
                    requireContext()
                )) * 173) / 328).toInt()
                val layoutParams = topBannerSliderViewPager.layoutParams
                layoutParams.height = viewPagerHeight
                topBannerSliderViewPager.layoutParams = layoutParams
            }

            topBannerIndicator.setViewPager2(topBannerSliderViewPager)
        }

        homeViewModel.bottomBannerLiveData.observe(viewLifecycleOwner) {
            bottomBannerIndicator.isVisible = it.size > 1
            main_screen_bottom_adv_banner.isVisible = it.isEmpty()
            main_screen_bottom_slider.isVisible = it.isNotEmpty()
            if (it.isEmpty()) {
                populateBottomAdBanner()
            } else {
                bottomBannerAdapter = BannerAdapter(this, it)
                bottomBannerSliderViewPager.adapter = bottomBannerAdapter
                bottomBannerSliderViewPager.post {
                    val viewPagerHeight = (((bottomBannerSliderViewPager.width - convertDpToPixel(
                        32f,
                        requireContext()
                    )) * 100) / 328).toInt()
                    val layoutParams = bottomBannerSliderViewPager.layoutParams
                    layoutParams.height = viewPagerHeight
                    bottomBannerSliderViewPager.layoutParams = layoutParams
                }

                bottomBannerIndicator.setViewPager2(bottomBannerSliderViewPager)
            }
        }

        homeViewModel.authorsLiveData.observe(viewLifecycleOwner) {
            authorsAdapter.authorsArray = it.list as ArrayList<Any>
            main_loadingView.isVisible = false
            main_content_view.isVisible = true
            home_screen_authors_warning.isVisible = !it.warning.isNullOrEmpty()
            home_screen_authors_warning.text = it.warning.orEmpty()
        }

        homeViewModel.categoryLiveData.observe(viewLifecycleOwner) {
            categoryAdapter.categoryArray = it as ArrayList<Any>
        }

        homeViewModel.progressLiveData.observe(viewLifecycleOwner) {
            setProgressIndicator(it)
        }
        btn_all_genres.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }

        frame_all_genres.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }

        btn_all_authors.setOnClickListener {
            startActivity(Intent(requireContext(), AuthorsActivity::class.java))
        }

        frame_all_authors.setOnClickListener {
            startActivity(Intent(requireContext(), AuthorsActivity::class.java))
        }
    }


    private fun populateTopAdBanner() {
        if (homeViewModel.isAdvertisementAllowed()) {
            isAdvAllowed = true
            main_screen_top_adv_banner.isVisible = true
            val size =
                BannerAdSize.stickySize(requireContext(), resources.displayMetrics.widthPixels)
            main_screen_top_adv_banner.isVisible = false
            main_screen_top_adv_banner_fail_text.isVisible = false
            main_screen_top_adv_banner_progress.isVisible = true
            main_screen_top_adv_banner.layoutParams =
                    main_screen_top_adv_banner.layoutParams.apply {
                        height = dpToPx(requireContext(), size.height.toFloat())
                    }
            main_screen_top_adv_banner.also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.MAIN_CENTER_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.
                        if (isDetached) {
                            return
                        }
                        YandexMetrica.reportEvent("MainTopBanner", "success")
                        main_screen_top_adv_banner.isVisible = true
                        main_screen_top_adv_banner_fail_text.isVisible = false
                        main_screen_top_adv_banner_progress.isVisible = false
                        main_screen_top_adv_banner.forceLayout()
                        main_screen_top_adv_banner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDetached) {
                            return
                        }
                        main_screen_top_adv_banner.isVisible = false
                        main_screen_top_adv_banner_fail_text.isVisible = true
                        main_screen_top_adv_banner_progress.isVisible = false
                        YandexMetrica.reportEvent("MainTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            main_screen_top_adv_banner.isVisible = false
        }
    }

    private fun populateBottomAdBanner() {
        if (homeViewModel.isAdvertisementAllowed()) {
            isAdvAllowed = true
            val size =
                BannerAdSize.stickySize(requireContext(), resources.displayMetrics.widthPixels)
            main_screen_bottom_adv_banner.isVisible = false
            main_screen_bottom_adv_banner_fail_text.isVisible = false
            main_screen_bottom_adv_banner_progress.isVisible = true
                main_screen_bottom_adv_banner.layoutParams =
                    main_screen_bottom_adv_banner.layoutParams.apply {
                        height = dpToPx(requireContext(), size.height.toFloat())
                    }
            main_screen_bottom_adv_banner.also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.MAIN_BOTTOM_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.
                        if (isDetached) {
                            return
                        }
                        YandexMetrica.reportEvent("BottomTopBanner", "success")
                        main_screen_bottom_adv_banner.isVisible = true
                        main_screen_bottom_adv_banner_fail_text.isVisible = false
                        main_screen_bottom_adv_banner_progress.isVisible = false
                        main_screen_bottom_adv_banner.forceLayout()
                        main_screen_bottom_adv_banner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDetached) {
                            return
                        }
                        main_screen_bottom_adv_banner.isVisible = false
                        main_screen_bottom_adv_banner_fail_text.isVisible = true
                        main_screen_bottom_adv_banner_progress.isVisible = false
                        YandexMetrica.reportEvent("BottomTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            main_screen_bottom_adv_banner.isVisible = false
        }
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onClick(music: Music) {
        startActivity(Intent(requireContext(), PlayMusic2::class.java).apply {
            putExtra(EXTRA_KEY_DATA, music)
        })
    }

    override fun onClick(category: Genre) {
        startActivity(Intent(requireContext(), MusicsActivity::class.java).apply {
            putExtra(EXTRA_GENRE_ID_KEY_DATA, category.id)
            putExtra(SCREEN_TITLE_DATA, category.name)
        })
    }

    override fun onClick(author: Author) {
        startActivity(Intent(requireContext(), MusicsActivity::class.java).apply {
            putExtra(EXTRA_AUTHOR_ID_KEY_DATA, author.id)
            putExtra(SCREEN_TITLE_DATA, author.name)
        })
    }
}