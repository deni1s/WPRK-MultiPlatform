package ru.denale.podcastlistener.feature.activities.musics

import android.content.Context
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic1
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_authors.authors_all_activity_banner
import kotlinx.android.synthetic.main.activity_category.category_all_activity_banner
import kotlinx.android.synthetic.main.activity_musics.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.denale.podcastlistener.BuildConfig

class MusicsActivity : MusicPlayerOnlineActivity(), MusicAdapter.SetOnClick {
    val musicAdapter: MusicAdapter by inject()
    val musicsViewModel: MusicsViewModel by viewModel { parametersOf(intent.extras) }
    private var disposable: MutableList<Disposable> = mutableListOf()
    private var bannerAdView: BannerAdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musics)
        populateAdBanner()
        intent.getStringExtra(SCREEN_TITLE_DATA)?.let {
            textViewMusicTitle.text = it
        }

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recycle_musics_all.layoutManager = layoutManager
        recycle_musics_all.adapter = musicAdapter
        val endlessScrollListener = object : EndlessScroll(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                musicsViewModel.loanNextNewsPart(totalItemsCount)
            }
        }
        recycle_musics_all!!.addOnScrollListener(endlessScrollListener)

        musicAdapter.setOnClick = this
        disposable.add(musicsViewModel.musicLiveData.subscribe {
            musicAdapter.authorsArray.addAll(it)
            musicAdapter.notifyItemRangeChanged(musicAdapter.authorsArray.size, it.size)
            text_view_empty_music.isVisible = musicAdapter.authorsArray.isEmpty()
            progress_music_list_activity.isVisible = false
            music_list_screen_warning
        })

        disposable.add(musicsViewModel.warningLiveData.subscribe {
            music_list_screen_warning.isVisible = true
            music_list_screen_warning.text = it
        })

        disposable.add(musicsViewModel.errorLiveData.subscribe {
            if (musicAdapter.authorsArray.isEmpty()) {
                text_view_empty_music.isVisible = true
                text_view_empty_music.text = it
                progress_music_list_activity.isVisible = false
            }
        })

        img_back_musics.setOnClickListener {
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        populateAdBanner()
    }

    override fun onStop() {
        bannerAdView?.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        music_list_adv_banner.removeAllViews()
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
        bannerAdView = null
        super.onStop()
    }

    override fun onClick(music: Music) {
        val intent  = Intent(this, PlayMusic1::class.java)
        intent.putExtra(EXTRA_MUSIC, music)
        if (musicsViewModel.type != null) {
            setResult(RESULT_OK, intent)
            finish()
        } else {
            startActivity(intent)
        }
    }

    private fun populateAdBanner() {
        if (musicsViewModel.isAdvertisementAllowed()) {
            bannerAdView = BannerAdView(this)
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            music_list_adv_banner.layoutParams = music_list_adv_banner.layoutParams.apply {
                height = dpToPx(this@MusicsActivity, size.height.toFloat())
            }
            bannerAdView?.apply {
                bannerAdView = this
                setAdSize(size)
                setAdUnitId(BuildConfig.AUTHORS_AD_UNIT_ID)
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.

                        if (isDestroyed) {
                            bannerAdView?.destroy()
                            return
                        } else {
                            try {
                                bannerAdView?.let { childView ->
                                    (childView.parent as? ViewGroup)?.removeView(childView)
                                }
                                music_list_adv_banner.addView(bannerAdView)
                            } catch (e: Exception) {
                                YandexMetrica.reportError("PlayMusic1", e.message)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) = Unit

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                loadAd(AdRequest.Builder().build())
            }
        } else {
            music_list_adv_banner.isVisible = false
        }
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onDestroy() {
        disposable.forEach {
            it.dispose()
        }
        disposable.clear()
        super.onDestroy()
    }
}