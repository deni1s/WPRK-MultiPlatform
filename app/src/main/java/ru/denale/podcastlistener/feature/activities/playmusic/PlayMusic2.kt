package ru.denale.podcastlistener.feature.activities.playmusic

import android.Manifest
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.android.synthetic.main.activity_play_music.btn_play_music
import kotlinx.android.synthetic.main.activity_play_music.btn_skip_next
import kotlinx.android.synthetic.main.activity_play_music.btn_skip_previous
import kotlinx.android.synthetic.main.activity_play_music.cover_music
import kotlinx.android.synthetic.main.activity_play_music.img_back_playMusic
import kotlinx.android.synthetic.main.activity_play_music.img_list_screen
import kotlinx.android.synthetic.main.activity_play_music.player_screen_warning
import kotlinx.android.synthetic.main.activity_play_music.player_top_adv_banner
import kotlinx.android.synthetic.main.activity_play_music.player_top_adv_banner_fail_text
import kotlinx.android.synthetic.main.activity_play_music.player_top_adv_banner_progress
import kotlinx.android.synthetic.main.activity_play_music.progress_player
import kotlinx.android.synthetic.main.activity_play_music.slider
import kotlinx.android.synthetic.main.activity_play_music.textViewPlayerTitle
import kotlinx.android.synthetic.main.activity_play_music.tv_singer_music
import kotlinx.android.synthetic.main.activity_play_music.tv_time_music
import kotlinx.android.synthetic.main.activity_play_music.tv_time_playing
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.common.convertMillisToString
import ru.denale.podcastlistener.data.Music
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
    private var sessionData: SessionData? = null
    private var isAdvertisementAvialable = true
    private var musicList: List<Music> = emptyList()
    private var type: String? = null
    private var currentPosition: Int = 0
    private var firstEnterProgress: Long = 0
    private var cameFromList: Boolean = false
    private var startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.getParcelable<Music>(EXTRA_MUSIC)?.let { music ->
                currentPosition = musicList.indexOf(music)
                routeToSelectedPodcastFromList()
                cameFromList = true
            }
        }
    }

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
            when {
                cameFromList -> {
                    cameFromList = false
                    if (mediaController?.mediaItemCount == 0) {
                        setMediaItems(musicList, currentPosition, 0)
                        mediaController?.play()
                    } else {
                        routeToSelectedPodcastFromList()
                        displayMusicInfoItem(mediaController?.currentMediaItem)
                    }
                }

                !isSameType(type) -> {
                    setMediaItems(musicList, currentPosition, firstEnterProgress)
                }

                else -> {
                    displayMusicInfoItem(mediaController?.currentMediaItem)
                    if (mediaController?.isPlaying == true) {
                        setTimer()
                        btn_play_music.setImageResource(R.drawable.ic_pause)
                    } else {
                        btn_play_music.setImageResource(R.drawable.ic_play)
                    }
                }
            }
            mediaController?.addListener(futureListener)
        }, MoreExecutors.directExecutor())
    }

    private fun seekTo(position: Int) {
        slider.value = 0F
        mediaController?.seekTo(position, 0L)
    }

    private val futureListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                setTimer()
                btn_play_music.setImageResource(R.drawable.ic_pause)
            } else {
                timer?.cancel()
                btn_play_music.setImageResource(R.drawable.ic_play)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            displayMusicInfoItem(mediaItem)
            if (mediaController?.isPlaying == true) {
                btn_play_music.setImageResource(R.drawable.ic_pause)
            } else {
                btn_play_music.setImageResource(R.drawable.ic_play)
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            Log.d("eventPlaye", events.get(0).toString())
            super.onEvents(player, events)
            if (events.contains(Player.EVENT_IS_LOADING_CHANGED) || events.contains(Player.EVENT_TRACKS_CHANGED)) {
                if (player.duration > 0) {
                    player.currentPosition.let { slider.value = it.toFloat() }
                    slider.valueTo = player.duration.toFloat()
                }
            }

            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) || events.contains(Player.EVENT_TRACKS_CHANGED)) {
                if (player.duration > 0) {
                    player.currentPosition.let { slider.value = it.toFloat() }
                    slider.valueTo = player.duration.toFloat()
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
        setContentView(R.layout.activity_play_music)
        if (savedInstanceState == null) {
            populateTopAdBanner()
        }
        playMusicViewModel.musicLiveData.observe(this) { data ->
            progress_player.isVisible = false
            musicList = data.list
            type = data.singlePodcastId ?: data.session?.waveId
            if (data.session != null) {
                setMenuClickListener(data.session.waveId)
            }
            val music = musicList.firstOrNull { it.id == data.session?.podcastId }
                ?: musicList.first()
            currentPosition = musicList.indexOf(music)
            firstEnterProgress = sessionData?.progress ?: 0L
        }

        playMusicViewModel.isAdvertisementAvailableData.observe(this) {
            isAdvertisementAvialable = it
        }

        playMusicViewModel.titleLiveData.observe(this) { textViewPlayerTitle.text = it }

        img_back_playMusic.setOnClickListener { finish() }

        slider.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            tv_time_playing.text = convertMillisToString(value.toLong())
        })

        slider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) = Unit

                override fun onStopTrackingTouch(slider: Slider) {
                    mediaController?.seekTo(slider.value.toLong())
                }
            })

        btn_play_music.setOnClickListener {
            timer?.cancel()
            if (mediaController?.isPlaying == true) {
                mediaController?.pause()
            } else {
                mediaController?.play()
            }
        }
        btn_skip_previous.setOnClickListener {
            slider.value = 0F
            mediaController?.seekToPrevious()
        }
        btn_skip_next.setOnClickListener {
            slider.value = 0F
            mediaController?.seekToNext()
        }
    }

    private fun setMenuClickListener(type: String) {
        img_list_screen.isVisible = true
        img_list_screen.setOnClickListener {
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
                        val newValue = slider.value + 1000
                        if (slider.valueTo > newValue) {
                            slider.value = newValue
                        }
                    } catch (e: Exception) {
                        YandexMetrica.reportError("slider error 1", e.message)
                    }
                }
            }

        }, 1000, 1000)
    }

    override fun onStop() {
        super.onStop()
        firstEnterProgress = 0L
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
                MediaItem.Builder()
                    .setMediaId(music.id)
                    .setUri(music.mediaUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(music.author)
                            .setExtras(getMusicExtra(music, index == 0, index == musics.lastIndex))
                            .setTitle(music.title)
                            .setArtworkUri(Uri.parse(music.imageUrl))
                            .build()
                    )
                    .build()
            }
            mediaController?.setMediaItems(mediaItems, position, progress)
            mediaController?.prepare()
            displayMusicInfoItem(mediaItems[position])
        }
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

    private fun convertSecondsToMMSS(millisSeconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millisSeconds)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(millisSeconds) - TimeUnit.HOURS.toMinutes(hours)
        val remainingSeconds =
            TimeUnit.MILLISECONDS.toSeconds(millisSeconds) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(
                minutes
            )
        return if (hours == 0L) {
            "%02d:%02d".format(minutes, remainingSeconds)
        } else "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
    }

    private fun displayMusicInfo(music: Music, isFirst: Boolean, isLast: Boolean) {
        progress_player.isVisible = false
        currentPosition = musicList.indexOf(music)
        imageLoadingService.load(cover_music, music.imageUrl.orEmpty(), this)
        textViewPlayerTitle.text = music.author.orEmpty()
        tv_singer_music.text = music.title
        tv_time_music.isVisible = !music.durationString.isNullOrEmpty()
        tv_time_music.text = music.durationString

        btn_skip_next.isVisible = !isLast
        btn_skip_previous.isVisible = !isFirst

        slider.valueFrom = 0.000000000000000F
        mediaController?.duration?.let { position ->
            if (position > 0) {
                mediaController?.duration?.toFloat()
            }
        }

        timer?.cancel()

        player_screen_warning.isVisible = !music.warningDescription.isNullOrEmpty()
        player_screen_warning.text = music.warningDescription.orEmpty()
        //  btn_play_music.setImageResource(R.drawable.ic_play)
    }

    private fun displayMusicInfoItem(mediaItem: MediaItem?) {
        progress_player.isVisible = false
        val music = mediaItem?.mediaMetadata?.extras?.getParcelable<Music>("mediaItem")
        val isFirst = mediaItem?.mediaMetadata?.extras?.getBoolean("isFirst") ?: true
        val isLast = mediaItem?.mediaMetadata?.extras?.getBoolean("isLast") ?: true

        imageLoadingService.load(cover_music, music?.imageUrl.orEmpty(), this)
        textViewPlayerTitle.text = music?.author.orEmpty()
        tv_singer_music.text = music?.title
        tv_time_music.isVisible = !music?.durationString.isNullOrEmpty()
        tv_time_music.text = music?.durationString

        btn_skip_next.isVisible = !isLast
        btn_skip_previous.isVisible = !isFirst

        slider.valueFrom = 0.000000000000000F
        mediaController?.duration?.toFloat()?.takeIf { it > slider.valueFrom }?.let {
            slider.valueTo = it
        }
        mediaController?.currentPosition?.takeIf { it > slider.valueFrom }?.toFloat()?.let {
            slider.value = it
        }

        player_screen_warning.isVisible = !music?.warningDescription.isNullOrEmpty()
        player_screen_warning.text = music?.warningDescription.orEmpty()
    }

    private fun populateTopAdBanner() {
        if (playMusicViewModel.isAdvertisementAllowed()) {
            val size = BannerAdSize.stickySize(
                this.applicationContext,
                resources.displayMetrics.widthPixels
            )
            player_top_adv_banner.layoutParams = player_top_adv_banner.layoutParams.apply {
                height = dpToPx(this@PlayMusic2, size.height.toFloat())
            }
            player_top_adv_banner_fail_text.isVisible = false
            player_top_adv_banner_progress.isVisible = true
            player_top_adv_banner.isVisible = true
            player_top_adv_banner.also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.PLAYER_TOP_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.

                        player_top_adv_banner_fail_text.isVisible = false
                        player_top_adv_banner_progress.isVisible = false
                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        } else {
                            YandexMetrica.reportEvent("PlayerTopBanner", "success on init")
                        }
                        player_top_adv_banner.forceLayout()
                        player_top_adv_banner.requestLayout()
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        player_top_adv_banner_fail_text.isVisible = true
                        player_top_adv_banner_progress.isVisible = false
                        player_top_adv_banner.visibility = View.INVISIBLE
                        YandexMetrica.reportEvent("PlayerTopBanner", "error on init")
                    }

                    override fun onAdClicked() = Unit

                    override fun onLeftApplication() = Unit

                    override fun onReturnedToApplication() = Unit

                    override fun onImpression(impressionData: ImpressionData?) = Unit
                })
                bannerView.loadAd(AdRequest.Builder().build())
            }
        } else {
            player_top_adv_banner.isVisible = false
        }
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    override fun onDestroy() {
        player_top_adv_banner?.destroy()
        player_top_adv_banner?.setBannerAdEventListener(null)

        super.onDestroy()
        timer?.cancel()
    }
}