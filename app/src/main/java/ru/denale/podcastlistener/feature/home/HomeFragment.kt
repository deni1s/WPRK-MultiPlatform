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
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import io.appmetrica.analytics.AppMetrica
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
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.databinding.FragmentHomeBinding
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
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateTopAdBanner()
        binding.recycleCategory.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recycleCategory.adapter = categoryAdapter
        categoryAdapter.onClickCategory = this

        binding.recycleAuthors.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recycleAuthors.adapter = authorsAdapter
        authorsAdapter.onAuthorClicked = this

        homeViewModel.topBannerLiveData.observe(viewLifecycleOwner) {
            binding.topBannerIndicator.isVisible = it.size > 1
            if (it.isEmpty()) return@observe
            topBannerAdapter = BannerAdapter(this, it)
            binding.topBannerSliderViewPager.adapter = topBannerAdapter
            binding.topBannerSliderViewPager.post {
                val viewPagerHeight = (((binding.topBannerSliderViewPager.width - convertDpToPixel(
                    32f,
                    requireContext()
                )) * 173) / 328).toInt()
                val layoutParams = binding.topBannerSliderViewPager.layoutParams
                layoutParams.height = viewPagerHeight
                binding.topBannerSliderViewPager.layoutParams = layoutParams
            }

            binding.topBannerIndicator.setViewPager2(binding.topBannerSliderViewPager)
        }

        homeViewModel.bottomBannerLiveData.observe(viewLifecycleOwner) {
            binding.bottomBannerIndicator.isVisible = it.size > 1
            binding.mainScreenBottomAdvBanner.isVisible = it.isEmpty()
            binding.mainScreenBottomSlider.isVisible = it.isNotEmpty()
            if (it.isEmpty()) {
                populateBottomAdBanner()
            } else {
                bottomBannerAdapter = BannerAdapter(this, it)
                binding.bottomBannerSliderViewPager.adapter = bottomBannerAdapter
                binding.bottomBannerSliderViewPager.post {
                    val viewPagerHeight = (((binding.bottomBannerSliderViewPager.width - convertDpToPixel(
                        32f,
                        requireContext()
                    )) * 100) / 328).toInt()
                    val layoutParams = binding.bottomBannerSliderViewPager.layoutParams
                    layoutParams.height = viewPagerHeight
                    binding.bottomBannerSliderViewPager.layoutParams = layoutParams
                }

                binding.bottomBannerIndicator.setViewPager2(binding.bottomBannerSliderViewPager)
            }
        }

        homeViewModel.authorsLiveData.observe(viewLifecycleOwner) {
            authorsAdapter.authorsArray = it.list
            binding.mainLoadingView.isVisible = false
            binding.mainContentView.isVisible = true
            binding.homeScreenAuthorsWarning.isVisible = !it.warning.isNullOrEmpty()
            binding.homeScreenAuthorsWarning.text = it.warning.orEmpty()
        }

        homeViewModel.categoryLiveData.observe(viewLifecycleOwner) {
            categoryAdapter.categoryArray = it.list
        }

        homeViewModel.progressLiveData.observe(viewLifecycleOwner) {
            setProgressIndicator(it)
        }
        binding.btnAllGenres.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }

        binding.frameAllGenres.setOnClickListener {
            startActivity(Intent(requireContext(), CategoryActivity::class.java))
        }

        binding.btnAllAuthors.setOnClickListener {
            startActivity(Intent(requireContext(), AuthorsActivity::class.java))
        }

        binding.frameAllAuthors.setOnClickListener {
            startActivity(Intent(requireContext(), AuthorsActivity::class.java))
        }
    }


    private fun populateTopAdBanner() {
        if (homeViewModel.isAdvertisementAllowed()) {
            isAdvAllowed = true
            binding.mainScreenTopAdvBanner.isVisible = true
            val size =
                BannerAdSize.stickySize(requireContext(), resources.displayMetrics.widthPixels)
            binding.mainScreenTopAdvBanner.isVisible = false
            binding.mainScreenTopAdvBannerFailText.isVisible = false
            binding.mainScreenTopAdvBannerProgress.isVisible = true
            binding.mainScreenTopAdvBanner.layoutParams =
                binding.mainScreenTopAdvBanner.layoutParams.apply {
                        height = dpToPx(requireContext(), size.height.toFloat())
                    }
            binding.mainScreenTopAdvBanner.also { bannerView ->
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
                        AppMetrica.reportEvent("MainTopBanner", "success")
                        binding.mainScreenTopAdvBanner.isVisible = true
                        binding.mainScreenTopAdvBannerFailText.isVisible = false
                        binding.mainScreenTopAdvBannerProgress.isVisible = false
                        binding.mainScreenTopAdvBanner.forceLayout()
                        binding.mainScreenTopAdvBanner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDetached) {
                            return
                        }
                        binding.mainScreenTopAdvBanner.isVisible = false
                        binding.mainScreenTopAdvBannerFailText.isVisible = true
                        binding.mainScreenTopAdvBannerProgress.isVisible = false
                        AppMetrica.reportEvent("MainTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            binding.mainScreenTopAdvBanner.isVisible = false
        }
    }

    private fun populateBottomAdBanner() {
        if (homeViewModel.isAdvertisementAllowed()) {
            isAdvAllowed = true
            val size =
                BannerAdSize.stickySize(requireContext(), resources.displayMetrics.widthPixels)
            binding.mainScreenBottomAdvBanner.isVisible = false
            binding.mainScreenBottomAdvBannerFailText.isVisible = false
            binding.mainScreenBottomAdvBannerProgress.isVisible = true
            binding.mainScreenBottomAdvBanner.layoutParams =
                binding.mainScreenBottomAdvBanner.layoutParams.apply {
                        height = dpToPx(requireContext(), size.height.toFloat())
                    }
            binding.mainScreenBottomAdvBanner.also { bannerView ->
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
                        AppMetrica.reportEvent("BottomTopBanner", "success")
                        binding.mainScreenBottomAdvBanner.isVisible = true
                        binding.mainScreenBottomAdvBannerFailText.isVisible = false
                        binding.mainScreenBottomAdvBannerProgress.isVisible = false
                        binding.mainScreenBottomAdvBanner.forceLayout()
                        binding.mainScreenBottomAdvBanner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDetached) {
                            return
                        }
                        binding.mainScreenBottomAdvBanner.isVisible = false
                        binding.mainScreenBottomAdvBannerFailText.isVisible = true
                        binding.mainScreenBottomAdvBannerProgress.isVisible = false
                        AppMetrica.reportEvent("BottomTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            binding.mainScreenBottomAdvBanner.isVisible = false
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