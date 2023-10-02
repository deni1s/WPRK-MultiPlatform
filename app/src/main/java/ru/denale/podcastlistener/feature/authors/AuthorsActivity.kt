package ru.denale.podcastlistener.feature.authors

import android.content.Context
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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.internal.ViewUtils.dpToPx
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_AUTHOR_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_authors.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.img_back_category
import kotlinx.android.synthetic.main.activity_category.recycle_category_all_activity
import kotlinx.android.synthetic.main.activity_category.textViewEmpty
import kotlinx.android.synthetic.main.activity_musics.*
import kotlinx.android.synthetic.main.activity_play_music.player_adv_banner
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.BuildConfig


class AuthorsActivity : MusicPlayerOnlineActivity(), AuthorsAdapter.OnClickAuthor {
    val authorsAdapter: AuthorsAdapter by inject()
    val autorsViewModel: AuthorsViewModel by viewModel()
    private var disposable: Disposable? = null
    private lateinit var endlessScrollListener: EndlessScroll
    private var bannerAdView: BannerAdView? = null

    private val textViewAdvHint by lazy {
        TextView(this).apply {
            text = "Хорошего вам дня!..."
            gravity = Gravity.CENTER
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
        setContentView(R.layout.activity_authors)
        populateAdBanner()
        progress_all_authors_activity.isVisible = true
        recycle_category_all_activity.post {
            val sColumnWidth = 110
            val spanCount = Math.floor(
                (recycle_category_all_activity.width / convertDpToPixel(
                    sColumnWidth.toFloat(),
                    this
                )).toDouble()
            ).toInt()
            autorsViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(this, spanCount)
            recycle_category_all_activity.layoutManager = layoutManager
            recycle_category_all_activity.adapter = authorsAdapter

            endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    autorsViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_category_all_activity!!.addOnScrollListener(endlessScrollListener)
        }

        disposable =
            autorsViewModel.authorLiveData.observeOn(AndroidSchedulers.mainThread()).subscribe {
                //     val previousSize = authorsAdapter.authorsArray.size
                authorsAdapter.authorsArray.addAll(it.list)
                authorsAdapter.notifyDataSetChanged()
                textViewEmpty.isVisible = authorsAdapter.authorsArray.isEmpty()
                progress_all_authors_activity.isVisible = false
                authors_screen_warning.isVisible = !it.warning.isNullOrEmpty()
                authors_screen_warning.text = it.warning.orEmpty()
                //   authorsAdapter.notifyItemRangeChanged(previousSize, authorsAdapter.authorsArray.size)
            }

        disposable =
            autorsViewModel.errorLiveData.observeOn(AndroidSchedulers.mainThread()).subscribe {
                if (authorsAdapter.authorsArray.isEmpty()) {
                    textViewEmpty.isVisible = true
                    textViewEmpty.text = it
                    progress_all_authors_activity.isVisible = false
                }
                //   authorsAdapter.notifyItemRangeChanged(previousSize, authorsAdapter.authorsArray.size)
            }

        authorsAdapter.onAuthorClicked = this

        img_back_category.setOnClickListener {
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        populateAdBanner()
    }

    private fun populateAdBanner() {
        if (autorsViewModel.isAdvertisementAllowed()) {
            bannerAdView = BannerAdView(this)
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            authors_all_activity_banner.layoutParams = authors_all_activity_banner.layoutParams.apply {
                height = dpToPx(this@AuthorsActivity, size.height.toFloat())
            }
            progressAdv.let { childView ->
                (childView.parent as? ViewGroup)?.removeView(childView)
            }
            authors_all_activity_banner.addView(progressAdv)
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
                                authors_all_activity_banner.addView(bannerAdView)
                            } catch (e: Exception) {
                                YandexMetrica.reportError("PlayMusic1", e.message)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        textViewAdvHint.let { childView ->
                            (childView.parent as? ViewGroup)?.removeView(childView)
                        }
                        progressAdv.let { childView ->
                            (childView.parent as? ViewGroup)?.removeView(childView)
                        }
                        authors_all_activity_banner.removeAllViews()
                        authors_all_activity_banner.addView(textViewAdvHint)
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                loadAd(AdRequest.Builder().build())
            }
        } else {
            authors_all_activity_banner.isVisible = false
        }
    }

    override fun onStop() {
        bannerAdView?.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        progressAdv.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        textViewAdvHint.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        authors_all_activity_banner.removeAllViews()
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
        bannerAdView = null
        super.onStop()
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onClick(author: Author) {
        startActivity(Intent(this, MusicsActivity::class.java).apply {
            putExtra(EXTRA_AUTHOR_ID_KEY_DATA, author.id)
            putExtra(SCREEN_TITLE_DATA, author.name)
        })
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}