package ge.denale.podcastlistener.feature.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.transition.ViewPropertyTransition
import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.*
import ge.denale.podcastlistener.data.Author
import ge.denale.podcastlistener.data.Genre
import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ge.denale.podcastlistener.feature.activities.playmusic.PlayMusic
import ge.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ge.denale.podcastlistener.feature.adapter.BannerAdapter
import ge.denale.podcastlistener.feature.adapter.CategoryAdapter
import ge.denale.podcastlistener.feature.adapter.MusicAdapter
import ge.denale.podcastlistener.feature.authors.AuthorsActivity
import ge.denale.podcastlistener.feature.category.CategoryActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class HomeFragment : MusicPlayerOnlineFragment(), MusicAdapter.SetOnClick,
    CategoryAdapter.OnClickCategory, AuthorsAdapter.OnClickAuthor {
    private var currentPage = 0
    val homeViewModel: HomeViewModel by viewModel()
    val categoryAdapter: CategoryAdapter by inject()
    val authorsAdapter: AuthorsAdapter by inject()
    var bannerAdapter: BannerAdapter? = null
    var timer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycle_category.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recycle_category.adapter = categoryAdapter
        categoryAdapter.onClickCategory = this

        recycle_authors.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recycle_authors.adapter = authorsAdapter
        authorsAdapter.onAuthorClicked = this

        homeViewModel.bannerLiveData.observe(viewLifecycleOwner) {
            bannerIndicator.isVisible = it.size > 1
            bannerAdapter = BannerAdapter(this, it)
            bannerSliderViewPager.adapter = bannerAdapter
            bannerSliderViewPager.post {
                val viewPagerHeight = (((bannerSliderViewPager.width - convertDpToPixel(
                    32f,
                    requireContext()
                )) * 173) / 328).toInt()
                val layoutParams = bannerSliderViewPager.layoutParams
                layoutParams.height = viewPagerHeight
                bannerSliderViewPager.layoutParams = layoutParams
            }

            bannerIndicator.setViewPager2(bannerSliderViewPager)

            autoSlide();
        }

        homeViewModel.authorsLiveData.observe(viewLifecycleOwner) {
            authorsAdapter.authorsArray = it as ArrayList<Any>
            main_loadingView.isVisible = false
            main_content_view.isVisible = true
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


    fun autoSlide() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    if (currentPage == (bannerAdapter!!.itemCount + 1) - 1) {
                        currentPage = 0;
                    }
                    bannerSliderViewPager.setCurrentItem(currentPage++, true);
                }
            }
        }, 1000, 3000)

    }

    override fun onClick(music: Music) {
        startActivity(Intent(requireContext(), PlayMusic::class.java).apply {
            putExtra(EXTRA_KEY_DATA, music)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
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