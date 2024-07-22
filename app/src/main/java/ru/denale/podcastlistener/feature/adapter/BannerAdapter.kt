package ru.denale.podcastlistener.feature.adapter

import ru.denale.podcastlistener.data.Banner
import ru.denale.podcastlistener.feature.home.BannerFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BannerAdapter(fragment: Fragment, private val banners:List<Banner>):FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = banners.size

    override fun createFragment(position: Int): Fragment = BannerFragment.newInstance(banners[position])
}