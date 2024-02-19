package ru.denale.podcastlistener.feature.activities.musics

import android.content.Context
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_musics.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic2

class MusicsActivity : MusicPlayerOnlineActivity(), MusicAdapter.SetOnClick {
    val musicAdapter: MusicAdapter by inject()
    val musicsViewModel: MusicsViewModel by viewModel { parametersOf(intent.extras) }
    private var disposable: MutableList<Disposable> = mutableListOf()
    private var bannerAdView: BannerAdView? = null
    private val textViewAdvHint by lazy {
        TextView(this).apply {
            text = "События моей жизни – это ступени к большему успеху и счастью."
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 0)
            setTextAppearance(
                this.context,
                R.style.TextAppearance_MyTheme_Headline6
            )
        }
    }
    private val progressAdv by lazy {
        ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val colorInt: Int = context.getColor(R.color.yellow)
                progressTintList = ColorStateList.valueOf(colorInt)
                indeterminateTintList = ColorStateList.valueOf(colorInt)
            }
        }
    }

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

    private fun clearAdView(bannerAdView: BannerAdView?) {
        bannerAdView?.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        progressAdv.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        textViewAdvHint.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        music_list_adv_banner.removeAllViews()
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
    }

    override fun onStop() {
        progressAdv.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        textViewAdvHint.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        super.onStop()
    }

    override fun onClick(music: Music) {
        val intent  = Intent(this, PlayMusic2::class.java)
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
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            progressAdv.let { childView ->
                (childView.parent as? ViewGroup)?.removeView(childView)
            }
            if (bannerAdView == null) {
                music_list_adv_banner.layoutParams = music_list_adv_banner.layoutParams.apply {
                    height = dpToPx(this@MusicsActivity, size.height.toFloat())
                }
                music_list_adv_banner.addView(progressAdv)
            }
            var previousBanner = bannerAdView
            bannerAdView = BannerAdView(this).also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.AUTHORS_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.

                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        } else {
                            try {
                                clearAdView(previousBanner)
                                previousBanner = null
                                music_list_adv_banner.addView(bannerView)
                            } catch (e: Exception) {
                                YandexMetrica.reportError("PlayMusic1", e.message)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        }
                        if (previousBanner == null) {
                            clearAdView(bannerAdView)
                            bannerAdView = null
                            textViewAdvHint.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            progressAdv.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            music_list_adv_banner.removeAllViews()
                            music_list_adv_banner.addView(textViewAdvHint)
                        } else {
                            bannerAdView = previousBanner
                        }
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
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
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
        bannerAdView = null

        disposable.forEach {
            it.dispose()
        }
        disposable.clear()
        super.onDestroy()
    }
}