package ru.denale.podcastlistener.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.*
import ru.denale.podcastlistener.data.Author
import ru.denale.podcastlistener.data.Genre
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusic1
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ru.denale.podcastlistener.feature.adapter.BannerAdapter
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import ru.denale.podcastlistener.feature.authors.AuthorsActivity
import ru.denale.podcastlistener.feature.category.CategoryActivity
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class HomeFragment : MusicPlayerOnlineFragment(), MusicAdapter.SetOnClick,
    CategoryAdapter.OnClickCategory, AuthorsAdapter.OnClickAuthor {
    val homeViewModel: HomeViewModel by viewModel()
    val categoryAdapter: CategoryAdapter by inject()
    val authorsAdapter: AuthorsAdapter by inject()
    var topBannerAdapter: BannerAdapter? = null
    var bottomBannerAdapter: BannerAdapter? = null

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
            if (it.isEmpty()) return@observe
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

    override fun onClick(music: Music) {
        startActivity(Intent(requireContext(), PlayMusic1::class.java).apply {
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