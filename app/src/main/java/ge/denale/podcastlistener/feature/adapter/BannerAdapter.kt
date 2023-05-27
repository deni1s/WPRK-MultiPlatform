package ge.denale.podcastlistener.feature.adapter

import ge.denale.podcastlistener.data.Banner
import ge.denale.podcastlistener.feature.home.BannerFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BannerAdapter(fragment: Fragment, val banners:List<Banner>):FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = banners.size

    override fun createFragment(position: Int): Fragment =
        BannerFragment.newInstance(banners[position])
}