package ru.denale.podcastlistener.feature.adapter

import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.implementSpringAnimationTrait
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.services.ImageLoadingService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.yandex.mobile.ads.nativeads.NativeAd
import ru.denale.podcastlistener.data.Author

private const val PODCAST_VIEW_TYPE = 1
private const val ADV_VIEW_TYPE = 2

class MusicAdapter(val imageLoadingService: ImageLoadingService): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var setOnClick:SetOnClick?=null
    var authorsArray = mutableListOf<Any>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    inner class PodcastViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        val img_music:ImageView = itemView.findViewById(R.id.img_music)
        val title_music:TextView = itemView.findViewById(R.id.title_music)
        val singer_music:TextView = itemView.findViewById(R.id.singer_music)
        val time_music:AppCompatTextView = itemView.findViewById(R.id.time_music)

        fun bind(music: Music) {
            imageLoadingService.load(img_music,music.imageUrl.orEmpty(), itemView.context)
            title_music.text = music.title
            singer_music.text = music.author.orEmpty()
            time_music.text = music.durationString.orEmpty()
            time_music.isVisible = !music.durationString.isNullOrEmpty()

            itemView.implementSpringAnimationTrait()
            itemView.setOnClickListener {
                setOnClick?.onClick(music)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PODCAST_VIEW_TYPE -> PodcastViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.music_item, parent, false)
            )
            ADV_VIEW_TYPE -> HorizontalAdvViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.horizontal_advertisement_item, parent, false)
            )
            else -> PodcastViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.music_item, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is MusicAdapter.PodcastViewHolder -> holder.bind(authorsArray[position] as Music)
            is HorizontalAdvViewHolder -> holder.bindCategory(authorsArray[position] as NativeAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (authorsArray[position]) {
            is Music -> PODCAST_VIEW_TYPE
            is NativeAd -> ADV_VIEW_TYPE
            else -> -1
        }
    }

    override fun getItemCount(): Int = authorsArray.size


    interface SetOnClick{
        fun onClick(music: Music)
    }

//    companion object {
//        private val MUSIC_COMPARATOR = object : DiffUtil.ItemCallback<Music>() {
//            override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean =
//                oldItem.id == newItem.id
//
//            override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean =
//                oldItem == newItem
//        }
//    }
}