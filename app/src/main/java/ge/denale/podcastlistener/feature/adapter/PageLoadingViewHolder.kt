package ge.denale.podcastlistener.feature.adapter

import android.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.nativeads.*

class PageLoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvLoading: TextView? = null

    fun VH(itemView: View) {
//        tvLoading = itemView.findViewById<View>(R.id.tv_loading_text) as TextView
    }
}