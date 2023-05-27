package ge.denale.podcastlistener.services

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import ge.denale.podcastlistener.R

class FrescoImageLoadingServiceImpl : ImageLoadingService {
    //    override fun load(musicPlayerOnlineImageView: MusicPlayerOnlineImageView, imageUrl: String) {
//        if (musicPlayerOnlineImageView is SimpleDraweeView) {
//            musicPlayerOnlineImageView.setImageURI(imageUrl)
//        } else {
//            throw IllegalStateException("ImageView Must be instance of SimpleDraweeView")
//        }
//    }
//
//    override fun loadGif(musicPlayerOnlineImageView: MusicPlayerOnlineImageView, imageUrl: String) {
//        if (musicPlayerOnlineImageView is SimpleDraweeView) {
//            val controllerListener: BaseControllerListener<ImageInfo?> =
//                object : BaseControllerListener<ImageInfo?>() {
//                    override fun onFinalImageSet(
//                        id: String?,
//                        imageInfo: ImageInfo?,
//                        animatable: Animatable?
//                    ) {
//                        animatable?.start()
//                    }
//                }
//            val controller: DraweeController = Fresco.newDraweeControllerBuilder()
//                .setUri(imageUrl)
//                .setAutoPlayAnimations(true)
//                .setControllerListener(controllerListener)
//                .build()
//            musicPlayerOnlineImageView.controller = controller
//        } else {
//            throw IllegalStateException("ImageView Must be instance of SimpleDraweeView")
//        }
//    }
    override fun load(imageView: ImageView, imageUrl: String, context: Context) {
        val requestOptions: RequestOptions = RequestOptions()
            .error(R.drawable.default_image)
            //.centerCrop()
            .transform(CenterCrop(), RoundedCorners(dpToPx(context, 12f)))
        // .transform(RoundedCorners(16))


        val crossFadeFactory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade(crossFadeFactory))
            .into(object : ImageViewTarget<Drawable?>(imageView) {
                override fun setResource(resource: Drawable?) {
                    imageView.setImageDrawable(resource)
                }
            })
    }

    override fun loadGif(imageView: ImageView, imageUrl: String, context: Context) {

        val requestOptions = RequestOptions()
            .error(R.drawable.default_image)
            //.centerCrop()
            .transform(CenterCrop(), RoundedCorners(dpToPx(context, 12f)))

        Glide.with(context)
            .asGif()
            .load(imageUrl)
            .apply(requestOptions)
            .into(object : ImageViewTarget<GifDrawable?>(imageView) {
                override fun setResource(resource: GifDrawable?) {
                    imageView.setImageDrawable(resource)
                }
            })
    }

    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}