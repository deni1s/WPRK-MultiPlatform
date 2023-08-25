package ru.denale.podcastlistener.services

import android.content.Context
import android.widget.ImageView

interface ImageLoadingService {
    fun load(imageView: ImageView, imageUrl:String, context: Context)

    fun loadGif(imageView: ImageView, imageUrl:String, context: Context)
}