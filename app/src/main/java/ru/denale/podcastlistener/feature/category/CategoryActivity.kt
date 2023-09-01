package ru.denale.podcastlistener.feature.category

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_GENRE_ID_KEY_DATA
import ru.denale.podcastlistener.common.MusicPlayerOnlineActivity
import ru.denale.podcastlistener.common.convertDpToPixel
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA
import ru.denale.podcastlistener.feature.adapter.EndlessScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_authors.*
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_category.img_back_category
import kotlinx.android.synthetic.main.activity_category.recycle_category_all_activity
import kotlinx.android.synthetic.main.activity_category.textViewEmpty
import kotlinx.android.synthetic.main.fragment_category.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.denale.podcastlistener.BuildConfig

class CategoryActivity : MusicPlayerOnlineActivity(), CategoryAdapter.OnClickCategory {
    val categoryAdapter: CategoryAdapter by inject()
    val categoryViewModel: CategoryViewModel by viewModel()
    private var disposable: Disposable? = null

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

    private fun populateAdBanner() {
        if (categoryViewModel.isAdvertisementAllowed()) {
            val size = BannerAdSize.stickySize(this, resources.displayMetrics.widthPixels)
            category_all_activity_banner.apply {
                setAdSize(size)
                setAdUnitId(BuildConfig.CATEGORY_AD_UNIT_ID)
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.
                        if (isDestroyed) {
                            this@apply?.destroy()
                            return
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
            category_all_activity_banner.isVisible = false
        }
    }

    override fun onClick(genre: Genre) {
        startActivity(Intent(this, MusicsActivity::class.java).apply {
            putExtra(SCREEN_TITLE_DATA, genre.name)
            putExtra(EXTRA_GENRE_ID_KEY_DATA, genre.id)
        })
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}