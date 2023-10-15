package ru.denale.podcastlistener.feature.category

import android.content.Context
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
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
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.img_back_category
import kotlinx.android.synthetic.main.activity_category.recycle_category_all_activity
import kotlinx.android.synthetic.main.activity_category.textViewEmpty
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.BuildConfig

class CategoryActivity : MusicPlayerOnlineActivity(), CategoryAdapter.OnClickCategory {
    val categoryAdapter: CategoryAdapter by inject()
    val categoryViewModel: CategoryViewModel by viewModel()
    private var disposable: Disposable? = null
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
        setContentView(R.layout.activity_category)
        populateAdBanner()
        recycle_category_all_activity.post {
            val sColumnWidth = 110
            val spanCount = Math.floor(
                (recycle_category_all_activity.width / convertDpToPixel(
                    sColumnWidth.toFloat(),
                    this
                )).toDouble()
            ).toInt()
            categoryViewModel.setRowsCount(spanCount)
            val layoutManager = GridLayoutManager(this, spanCount)

            recycle_category_all_activity.layoutManager = layoutManager
            recycle_category_all_activity.adapter = categoryAdapter
            val endlessScrollListener = object : EndlessScroll(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    categoryViewModel.loanNextNewsPart(totalItemsCount)
                }
            }
            recycle_category_all_activity!!.addOnScrollListener(endlessScrollListener)
        }

        disposable = categoryViewModel.categoryLiveData.subscribe {
            categoryAdapter.categoryArray.addAll(it)
            categoryAdapter.notifyDataSetChanged()
            textViewEmpty.isVisible = categoryAdapter.categoryArray.isEmpty()
            progress_all_category_activity.isVisible = false
        }

        disposable =
            categoryViewModel.errorLiveData.observeOn(AndroidSchedulers.mainThread()).subscribe {
                if (categoryAdapter.categoryArray.isEmpty()) {
                    textViewEmpty.isVisible = true
                    textViewEmpty.text = it
                    progress_all_category_activity.isVisible = false
                }
                //   authorsAdapter.notifyItemRangeChanged(previousSize, authorsAdapter.authorsArray.size)
            }
        categoryAdapter.onClickCategory = this

        img_back_category.setOnClickListener {
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        populateAdBanner()
    }

    private fun populateAdBanner() {
        if (categoryViewModel.isAdvertisementAllowed()) {
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            progressAdv.let { childView ->
                (childView.parent as? ViewGroup)?.removeView(childView)
            }
            if (bannerAdView == null) {
                category_all_activity_banner.layoutParams = category_all_activity_banner.layoutParams.apply {
                    height = dpToPx(this@CategoryActivity, size.height.toFloat())
                }
                category_all_activity_banner.addView(progressAdv)
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
                                category_all_activity_banner.addView(bannerView)
                            } catch (e: Exception) {
                                YandexMetrica.reportError("PlayMusic1", e.message)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (previousBanner == null) {
                            clearAdView(bannerAdView)
                            bannerAdView = null
                            textViewAdvHint.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            progressAdv.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            category_all_activity_banner.removeAllViews()
                            category_all_activity_banner.addView(textViewAdvHint)
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
            category_all_activity_banner.isVisible = false
        }
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
        category_all_activity_banner.removeAllViews()
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onClick(genre: Genre) {
        startActivity(Intent(this, MusicsActivity::class.java).apply {
            putExtra(SCREEN_TITLE_DATA, genre.name)
            putExtra(EXTRA_GENRE_ID_KEY_DATA, genre.id)
        })
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
        bannerAdView = null

        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}