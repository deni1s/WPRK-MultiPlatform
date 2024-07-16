package ru.denale.podcastlistener.feature.activities.playmusic

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.material.slider.Slider
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import io.appmetrica.analytics.AppMetrica
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.common.convertMillisToString
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.databinding.ActivityPlayMusicBinding
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.services.ImageLoadingService
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

private const val PERMISSION_CODE = 26

class PlayMusic2 : AppCompatActivity() {

    val playMusicViewModel: PlayMusicViewModel2 by viewModel { parametersOf(intent.extras) }
    val imageLoadingService: ImageLoadingService by inject()

    //   private var musicPlayerServiceIntent: Intent? = null
    private var timer: Timer? = null
    private var isAdvertisementAvialable = true
    private var musicList: List<Music> = emptyList()
    private var type: String? = null
    private var currentPosition: Int = 0
    private var cameFromList: Boolean = false
    private var startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.getParcelable<Music>(EXTRA_MUSIC)?.let { music ->
                currentPosition = musicList.indexOfFirst { it.id == music.id }
                routeToSelectedPodcastFromList()
                cameFromList = true
            }
        }
    }
    private lateinit var binding: ActivityPlayMusicBinding


    private fun routeToSelectedPodcastFromList() {
        seekTo(currentPosition)
        mediaController?.play()
    }

    var sessionToken: SessionToken? = null
    var controllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null

    private fun askNotificationPermission() {
        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_CODE
            )
        } else {
            startService()
        }
    }

    private fun startService() {
        sessionToken = SessionToken(this, ComponentName(this, MusicPlayerService2::class.java))
        controllerFuture =
            MediaController.Builder(this, sessionToken!!).buildAsync()
        controllerFuture!!.addListener({
            mediaController = controllerFuture!!.get()
            if (cameFromList) {
                cameFromList = false
                if (mediaController?.mediaItemCount == 0) {
                    setMediaItems(musicList, currentPosition, 0)
                    mediaController?.play()
                } else {
                    routeToSelectedPodcastFromList()
                    displayMusicInfoItem(mediaController?.currentMediaItem)
                }
            } else {
                playMusicViewModel.musicLiveData.observe(this) { data ->
                    binding.progressPlayer.isVisible = false
                    musicList = data.list
                    type = data.session?.waveId ?: data.singlePodcastId
                    if (data.session != null) {
                        setMenuClickListener(data.session.waveId)
                    }
                    if (!isSameType(type)) {
                        mediaController?.pause()
                        val music = musicList.firstOrNull { it.id == data.session?.podcastId }
                            ?: musicList.first()
                        currentPosition = musicList.indexOf(music)
                        setMediaItems(
                            musicList,
                            currentPosition,
                            data.session?.progress?.toLong() ?: 0L
                        )
                        mediaController?.play()
                    } else {
                        currentPosition = mediaController?.currentMediaItemIndex ?: 0
                        displayMusicInfoItem(mediaController?.currentMediaItem)
                        restoreMediaState()
                        mediaController?.play()
                    }
                }
            }
            mediaController?.addListener(futureListener)
        }, MoreExecutors.directExecutor())
    }

    private fun restoreMediaState() {
        if (mediaController?.currentMediaItem != null) {
            binding.slider.valueFrom = 0.0f
            binding.slider.valueTo = mediaController?.duration?.toFloat()?.takeIf { it > 0F } ?: 0f
            mediaController?.currentPosition?.takeIf { it > binding.slider.valueFrom && it <= binding.slider.valueTo }
                ?.toFloat()?.let {
                    binding.slider.value = it
                }
        }
        //if (mediaController?.isPlaying == true) {
        setTimer()
        binding.btnPlayMusic.setImageResource(R.drawable.ic_pause)
//        } else {
//            btn_play_music.setImageResource(R.drawable.ic_play)
//        }
    }

    private fun seekTo(position: Int) {
        binding.slider.value = 0F
        mediaController?.seekTo(position, 0L)
    }

    private val futureListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                setTimer()
                binding.btnPlayMusic.setImageResource(R.drawable.ic_pause)
            } else {
                timer?.cancel()
                binding.btnPlayMusic.setImageResource(R.drawable.ic_play)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            displayMusicInfoItem(mediaItem)
            if (mediaController?.isPlaying == true) {
                binding.btnPlayMusic.setImageResource(R.drawable.ic_pause)
            } else {
                binding.btnPlayMusic.setImageResource(R.drawable.ic_play)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            if (events.contains(Player.EVENT_IS_LOADING_CHANGED) || events.contains(Player.EVENT_TRACKS_CHANGED) || events.contains(
                    Player.EVENT_PLAYBACK_STATE_CHANGED
                )
            ) {
                if (player.duration > 0) {
                    binding.slider.valueTo = player.duration.toFloat()
                    player.currentPosition.let {
                        if (binding.slider.valueTo >= it) {
                            binding.slider.value = it.toFloat()
                        } else {
                            AppMetrica.reportError(
                                "Max slider value error",
                                "Player duration: ${player.duration}, currentValue: ${player.currentPosition}"
                            )
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            showMusicError()
            mediaController?.seekToNext()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayMusicBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (savedInstanceState == null) {
            populateTopAdBanner()
        }

        playMusicViewModel.isAdvertisementAvailableData.observe(this) {
            isAdvertisementAvialable = it
        }

        playMusicViewModel.titleLiveData.observe(this) { binding.textViewPlayerTitle.text = it }

        binding.imgBackPlayMusic.setOnClickListener { finish() }

        binding.slider.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            binding.tvTimePlaying.text = convertMillisToString(value.toLong())
        })

        binding.slider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) = Unit

                override fun onStopTrackingTouch(slider: Slider) {
                    mediaController?.seekTo(slider.value.toLong())
                }
            })

        binding.btnPlayMusic.setOnClickListener {
            timer?.cancel()
            if (mediaController?.isPlaying == true) {
                mediaController?.pause()
            } else {
                mediaController?.play()
            }
        }
        binding.btnSkipPrevious.setOnClickListener {
            binding.slider.value = 0F
            mediaController?.seekToPrevious()
        }
        binding.btnSkipNext.setOnClickListener {
            binding.slider.value = 0F
            mediaController?.seekToNext()
        }
    }

    private fun setMenuClickListener(type: String) {
        binding.imgListScreen.isVisible = true
        binding.imgListScreen.setOnClickListener {
            startForResult.launch(Intent(this, MusicsActivity::class.java).apply {
                putExtra(EXTRA_MUSIC_TYPE, type)
            })
        }
    }

    private fun showMusicError() {
        Toast.makeText(this@PlayMusic2, "Ошибка вопроизведения", Toast.LENGTH_SHORT).show()
        mediaController?.seekToNext()
    }

    private fun setTimer() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    try {
                        val newValue =  binding.slider.value + 1000
                        if ( binding.slider.valueTo > newValue) {
                            binding.slider.value = newValue
                        }
                    } catch (e: Exception) {
                        AppMetrica.reportError("slider error 1", e.message)
                    }
                }
            }

        }, 1000, 1000)
    }

    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(futureListener)
        mediaController?.release()
        timer?.cancel()
        timer = null
    }

    override fun onStart() {
        super.onStart()
        askNotificationPermission()
    }

    private fun setMediaItems(musics: List<Music>, position: Int, progress: Long) {
        if (musics.isNotEmpty()) {
            val mediaItems = musics.mapIndexed { index, music ->
                music.toMediaItem(index == 0, index == musics.lastIndex)
            }
            mediaController?.setMediaItems(mediaItems, position, progress)
            mediaController?.prepare()
            displayMusicInfoItem(mediaItems[position])
        }
    }

    private fun Music.toMediaItem(isFirst: Boolean, isLast: Boolean): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(mediaUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(author)
                    .setExtras(getMusicExtra(this, isFirst, isLast))
                    .setTitle(title)
                    .setArtworkUri(Uri.parse(imageUrl))
                    .build()
            )
            .build()
    }

    private fun isSameType(type: String?): Boolean {
        return type == mediaController?.currentMediaItem?.mediaMetadata?.extras?.getString("type")
    }

    private fun getMusicExtra(music: Music, isFirst: Boolean, isLast: Boolean): Bundle {
        return Bundle().apply {
            putParcelable("mediaItem", music)
            putBoolean("isFirst", isFirst)
            putBoolean("isLast", isLast)
            putString("type", type)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService()
            } else {
                Toast.makeText(
                    this,
                    "Для корректной работы плеера пожалуйста предоставьте разрешение",
                    Toast.LENGTH_SHORT
                ).show()
                startService()
            }
        }
    }



    private fun displayMusicInfoItem(mediaItem: MediaItem?) {
        binding.progressPlayer.isVisible = false
        val music = mediaItem?.mediaMetadata?.extras?.getParcelable<Music>("mediaItem")
        val isFirst = mediaItem?.mediaMetadata?.extras?.getBoolean("isFirst") ?: true
        val isLast = mediaItem?.mediaMetadata?.extras?.getBoolean("isLast") ?: true

        imageLoadingService.load(binding.coverMusic , music?.imageUrl.orEmpty(), this)
        binding.textViewPlayerTitle.text = music?.author.orEmpty()
        binding.tvSingerMusic.text = music?.title
        binding.tvTimeMusic.isVisible = !music?.durationString.isNullOrEmpty()
        binding.tvTimeMusic.text = music?.durationString

        binding.btnSkipNext.isVisible = !isLast
        binding.btnSkipPrevious.isVisible = !isFirst

        binding.slider.valueFrom = 0.000000000000000F
        mediaController?.duration?.toFloat()?.takeIf { it > binding.slider.valueFrom }?.let {
            binding.slider.valueTo = it
        }

        binding.playerScreenWarning.isVisible = !music?.warningDescription.isNullOrEmpty()
        binding.playerScreenWarning.text = music?.warningDescription.orEmpty()
    }

    private fun populateTopAdBanner() {
        if (playMusicViewModel.isAdvertisementAllowed()) {
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            binding.playerTopAdvBanner.layoutParams = binding.playerTopAdvBanner.layoutParams.apply {
                height = dpToPx(this@PlayMusic2, size.height.toFloat())
            }
            binding.playerTopAdvBannerFailText.isVisible = false
            binding.playerTopAdvBannerProgress.isVisible = true
            binding.playerTopAdvBanner.isVisible = true
            binding.playerTopAdvBanner.also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.PLAYER_TOP_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.

                        binding.playerTopAdvBannerFailText.isVisible = false
                        binding.playerTopAdvBannerProgress.isVisible = false
                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        } else {
                            AppMetrica.reportEvent("PlayerTopBanner", "success on init")
                        }
                        binding.playerTopAdvBanner.forceLayout()
                        binding.playerTopAdvBanner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        }
                        binding.playerTopAdvBannerFailText.isVisible = true
                        binding.playerTopAdvBannerProgress.isVisible = false
                        binding.playerTopAdvBanner.visibility = View.INVISIBLE
                        AppMetrica.reportEvent("PlayerTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            binding.playerTopAdvBanner.isVisible = false
        }
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onDestroy() {
        binding.playerTopAdvBanner.destroy()
        binding.playerTopAdvBanner.setBannerAdEventListener(null)

        super.onDestroy()
        timer?.cancel()
    }
}