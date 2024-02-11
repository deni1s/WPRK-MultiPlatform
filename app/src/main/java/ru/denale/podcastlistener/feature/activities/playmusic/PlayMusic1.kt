package ru.denale.podcastlistener.feature.activities.playmusic

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import com.yandex.metrica.YandexMetrica
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.android.synthetic.main.activity_play_music.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.denale.podcastlistener.BuildConfig
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.EXTRA_MUSIC
import ru.denale.podcastlistener.common.EXTRA_MUSIC_TYPE
import ru.denale.podcastlistener.common.SCREEN_PODCAST_ID_DATA
import ru.denale.podcastlistener.common.convertMillisToString
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.feature.activities.musics.MusicsActivity
import ru.denale.podcastlistener.services.ImageLoadingService
import java.util.*
import java.util.concurrent.TimeUnit


private const val ADVERTISMENET_CODE = 23
private const val PERMISSION_CODE = 26
private const val SERVICE_ACTION_NAME = "ru.podcastlistener.MediaPlayerIntent"

class PlayMusic1 : AppCompatActivity() {
    val playMusicViewModel: PlayMusicViewModel by viewModel { parametersOf(intent.extras) }
    val imageLoadingService: ImageLoadingService by inject()
    private var currentMusic: Music? = null

    //   private var musicPlayerServiceIntent: Intent? = null
    private var isDragging: Boolean = false
    private var timer: Timer? = null
    private var topAdvTimer: Timer? = null
    private var bottomAdvTimer: Timer? = null
    private var isAdvertisementAvialable = true
    private var playerBinder: MusicPlayerService.PlayerEventBinder? = null
    private var wasServiceInitialized = false
    private var musicList: List<Music> = emptyList()
    private var type: InitializationType? = null
    private var podcastId: String? = null
    private var progress: Int? = null
    private var command: String = INITIALIZATION_COMMAND
    private var topBannerAdView: BannerAdView? = null
    private var startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.getParcelable<Music>(EXTRA_MUSIC)?.let {
                sendCommand(MediaActivityEvent.onMusicRequeired(it))
            }
        }
    }

    private val topTextViewAdvHint by lazy {
        TextView(this).apply {
            text = "Я могу больше, чем я думаю! Мой потенциал велик! Я с легкостью превращаю невозможное в возможное!"
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 0)
            setTextAppearance(
                this.context,
                R.style.TextAppearance_MyTheme_Headline6
            )
        }
    }
    private val topProgressAdv by lazy {
        ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val colorInt: Int = context.getColor(R.color.yellow)
                progressTintList = ColorStateList.valueOf(colorInt)
                indeterminateTintList = ColorStateList.valueOf(colorInt)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service !is MusicPlayerService.PlayerEventBinder) return
            wasServiceInitialized = true
            playerBinder = service
            if (musicList.isNotEmpty()) {
                sendCommand(
                    MediaActivityEvent.onNewInitializationRequired(
                        musicList,
                        isAdvertisementAvialable,
                        // костыль на всякий случай
                        type ?: if (musicList.size == 1) {
                            InitializationType.PodcastId(musicList.first().id)
                        } else null,
                        podcastId,
                        progress
                    )
                )
            } else {
                sendCommand(MediaActivityEvent.onNewDataRequired)
            }
//            service.provideMusicList(musicList)
//            service.provideAdvertisementInfo(isAdvertisementAvialable)
            service.state.observe(this@PlayMusic1) { state ->
                when (state) {
                    is MediaServiceEvent.onMusicFailed -> showMusicError()
                    is MediaServiceEvent.onMusicStarted -> onMusicStarted(state.duration)
                    is MediaServiceEvent.onMusicFinished -> {
                        timer?.cancel()
                        btn_play_music.setImageResource(R.drawable.ic_play)
                    }

                    is MediaServiceEvent.onMusicPaused -> {
                        timer?.cancel()
                        btn_play_music.setImageResource(R.drawable.ic_play)
                    }

                    is MediaServiceEvent.onMusicContinued -> {
                        btn_play_music.setImageResource(R.drawable.ic_pause)
                        runOnUiThread {
                            try {
                                slider.valueFrom = 0.0f
                                slider.valueTo = state.duration.toFloat()
                                slider.value = state.position.toFloat()
                            } catch (e: Exception) {
                                YandexMetrica.reportError("slider error 4", e.message)
                            }
                            setTimer()
                        }
                    }

                    is MediaServiceEvent.onMusicLoading -> {
                        btn_play_music.isClickable = false
                        PlayProgress.show()
                        try {
                            slider.valueFrom = 0.0f
                            slider.value = 0f
                        } catch (e: Exception) {
                            YandexMetrica.reportError("slider error 4", e.message)
                        }
                        displayMusicInfo(
                            state.music,
                            state.isFirst,
                            state.isLast
                        )
                    }

                    is MediaServiceEvent.onMusicDisplay -> {
                        displayMusicInfo(
                            state.music,
                            state.isFirst,
                            state.isLast
                        )
                    }

                    is MediaServiceEvent.onDataUpdate -> {
                        runOnUiThread {
                            displayMusicInfo(
                                state.music,
                                state.isFirst,
                                state.isLast
                            )
                            try {
                                slider.valueFrom = 0.0f
                                slider.valueTo = state.duration.toFloat()
                                slider.value = state.position.toFloat()
                            } catch (e: Exception) {
                                YandexMetrica.reportError("slider error 4", e.message)
                            }
                            if (state.isMusicInProgress) {
                                btn_play_music.setImageResource(R.drawable.ic_pause)
                            } else {
                                btn_play_music.setImageResource(R.drawable.ic_play)
                            }
                            if (state.isMusicInProgress) {
                                setTimer()
                            } else {
                                timer?.cancel()
                            }
                            type = state.type
                            (state.type as? InitializationType.Type)?.let { setMenuClickListener(it.id) }
                        }
                    }

                    null -> {}
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerBinder = null
        }
    }

    private fun askNotificationPermission(command: String) {
        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            this.command = command
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(POST_NOTIFICATIONS),
                PERMISSION_CODE
            )
        } else {
            startService(command)
            // Permission is already granted
            // Your code for using the camera or performing other actions
        }
    }

    private fun startService(command: String) {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = command
        ContextCompat.startForegroundService(this, intent)
        //startForegroundService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
        populateTopAdBanner()
        intent.data?.getQueryParameter(SCREEN_PODCAST_ID_DATA)?.let {
            stopService(getCurrentRunningServiceIntent("ru.denale.podcastlistener"))
            playMusicViewModel.loadPodcastId(it)
        } ?: intent.action?.let {
            command = it
            if (isMyServiceRunning(MusicPlayerService::class.java)) {
                initializeService(intent.action.orEmpty())
                wasServiceInitialized = true
            } else {
                playMusicViewModel.getTypeData(it)
            }
        } ?: if (savedInstanceState == null) {
            if (isMyServiceRunning(MusicPlayerService::class.java)) {
                initializeService(INITIALIZATION_WITH_PREVIOUS_COMMAND)
                wasServiceInitialized = true
            }
        }
        playMusicViewModel.musicLiveData.observe(this) { list ->
            initializeService(INITIALIZATION_COMMAND)
            progress_player.isVisible = false
            musicList = list
            sendCommand(
                MediaActivityEvent.onNewInitializationRequired(
                    musicList,
                    isAdvertisementAvialable,
                    // костыль на всякий случай
                    type ?: if (musicList.size == 1) {
                        InitializationType.PodcastId(musicList.first().id)
                    } else null,
                    podcastId,
                    progress
                )
            )
        }

        playMusicViewModel.isAdvertisementAvailableData.observe(this) {
            isAdvertisementAvialable = it
        }

        playMusicViewModel.titleLiveData.observe(this) {
            textViewPlayerTitle.text = it
        }

        playMusicViewModel.sessionLiveData.observe(this) {
            type = InitializationType.Type(it.waveId)
            setMenuClickListener(it.waveId)
            podcastId = it.podcastId
            progress = it.progress
        }
        img_back_playMusic.setOnClickListener {
            finish()
        }

        slider.addOnChangeListener(Slider.OnChangeListener
        { slider, value, fromUser ->
            tv_time_playing.text = convertMillisToString(value.toLong())
        })

        slider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    isDragging = true
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    isDragging = false
                    sendCommand(MediaActivityEvent.onMusicSeeked(slider.value.toInt()))
                }
            })

        btn_play_music.setOnClickListener {
            timer?.cancel()
            sendCommand(MediaActivityEvent.onPlayRequired)
        }
        btn_skip_previous.setOnClickListener {
            btn_play_music.setImageResource(R.drawable.ic_play)
            playPreviousMusic()
        }
        btn_skip_next.setOnClickListener {
            btn_play_music.setImageResource(R.drawable.ic_play)
            playNextMusic()
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

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Поделиться через"))
    }

    private fun showMusicError() {
        Toast.makeText(this@PlayMusic1, "Ошибка вопроизведения", Toast.LENGTH_SHORT).show()
        playNextMusic()
    }

    private fun onMusicStarted(duration: Long) {
        //   playMusicViewModel.markTrackSeen(music.id)

        runOnUiThread {
            btn_play_music.isClickable = true
            tv_time_music.isVisible = true
            tv_time_music.text = convertSecondsToMMSS(duration)

            //    slider.value = 0F
            try {
                val valueFrom = 0.0f
                slider.valueFrom = valueFrom
                val valueTo = duration.toFloat()
                if (valueTo > valueFrom) {
                    slider.valueTo = valueTo
                } else {
                    slider.valueTo = valueFrom
                }
                PlayProgress.hide()
            } catch (e: Exception) {
                YandexMetrica.reportError("slider error 10", e.message)
            }
            btn_play_music.setImageResource(R.drawable.ic_pause)
            setTimer()
        }
    }

    private fun setTimer() {
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

    private fun setTopAdvTimer() {
        topAdvTimer = Timer()
        topAdvTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    populateTopAdBanner()
                }
            }

        }, 4000, 4000)
    }

    private fun clearTopAdv(bannerAdView: BannerAdView?) {
        bannerAdView?.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        topProgressAdv.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        topTextViewAdvHint.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        player_top_adv_banner.removeAllViews()
        bannerAdView?.destroy()
        bannerAdView?.setBannerAdEventListener(null)
    }

    override fun onStop() {
        topProgressAdv.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        topTextViewAdvHint.let { childView ->
            (childView.parent as? ViewGroup)?.removeView(childView)
        }
        super.onStop()
        timer?.cancel()
        timer = null
        stopAdvTimer()
    }

    private fun stopAdvTimer() {
        topAdvTimer?.cancel()
        topAdvTimer = null
        bottomAdvTimer?.cancel()
        bottomAdvTimer = null
    }

    override fun onRestart() {
        super.onRestart()
        populateTopAdBanner()
    }

    override fun onStart() {
        super.onStart()
        if (wasServiceInitialized) {
            sendCommand(MediaActivityEvent.onNewDataRequired)
        }
    }

    private fun getCurrentRunningServiceIntent(packageName: String): Intent? {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val runningServices = activityManager?.getRunningServices(Integer.MAX_VALUE)

        if (runningServices != null) {
            for (serviceInfo in runningServices) {
                val servicePackageName = serviceInfo.service.className
                if (servicePackageName == packageName) {
                    val serviceClass = Class.forName(serviceInfo.service.className)
                    return Intent(this, serviceClass)
                }
            }
        }
        return null
    }

    private fun sendCommand(event: MediaActivityEvent) {
        playerBinder?.processEvent(event)
    }

    private fun initializeService(command: String) {
        if (!wasServiceInitialized) {
            askNotificationPermission(command)
            bindService(
                Intent(this, MusicPlayerService::class.java),
                connection,
                Context.BIND_AUTO_CREATE or Context.BIND_ADJUST_WITH_ACTIVITY
            )
            // playerBinder?.processEvent(MediaActivityEvent.onNewInitializationRequired(musicList, isAdvertisementAvialable))
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
                startService(command)
            } else {
                Toast.makeText(
                    this,
                    "Для корректной работы плеера пожалуйста предоставьте разрешение",
                    Toast.LENGTH_SHORT
                ).show()
                startService(command)
            }
        }
    }

    private fun playPreviousMusic() {
        timer?.cancel()
        try {
            slider.value = 0f
        } catch (e: Exception) {
            YandexMetrica.reportError("slider error 2", e.message)
        }
        sendCommand(MediaActivityEvent.onPreviousRequeired)
    }

    private fun playNextMusic() {
        timer?.cancel()
        try {
            slider.value = 0f
        } catch (e: Exception) {
            YandexMetrica.reportError("slider error 3", e.message)
        }
        sendCommand(MediaActivityEvent.onNextRequeired)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
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
        currentMusic = music
        imageLoadingService.load(cover_music, music.imageUrl.orEmpty(), this)
        textViewPlayerTitle.text = music.author.orEmpty()
        tv_singer_music.text = music.title
        tv_time_music.isVisible = !music.durationString.isNullOrEmpty()
        tv_time_music.text = music.durationString

        btn_skip_next.isVisible = !isLast
        btn_skip_previous.isVisible = !isFirst

        timer?.cancel()

        player_screen_warning.isVisible = !music.warningDescription.isNullOrEmpty()
        player_screen_warning.text = music.warningDescription.orEmpty()
        //  btn_play_music.setImageResource(R.drawable.ic_play)
    }

    private fun populateTopAdBanner() {
        if (playMusicViewModel.isAdvertisementAllowed()) {
            val size = BannerAdSize.stickySize(this.applicationContext, resources.displayMetrics.widthPixels)
            player_top_adv_banner.layoutParams = player_top_adv_banner.layoutParams.apply {
                height = dpToPx(this@PlayMusic1, size.height.toFloat())
            }
            topProgressAdv.let { childView ->
                (childView.parent as? ViewGroup)?.removeView(childView)
            }
            if (topBannerAdView == null) {
                player_top_adv_banner.addView(topProgressAdv)
            }
            var previousBanner = topBannerAdView
            topBannerAdView = BannerAdView(this).also { bannerView ->
                bannerView.setAdSize(size)
                bannerView.setAdUnitId(BuildConfig.PLAYER_TOP_AD_UNIT_ID)
                bannerView.setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {
                        // If this callback occurs after the activity is destroyed, you
                        // must call destroy and return or you may get a memory leak.
                        // Note `isDestroyed` is a method on Activity.

                        if (isDestroyed) {
                            bannerView.destroy()
                            return
                        } else {
                            try {
                                clearTopAdv(previousBanner)
                                previousBanner = null
                                player_top_adv_banner.addView(bannerView)
                                if (topAdvTimer == null) {
                                    YandexMetrica.reportEvent("PlayerTopBanner", "success on init")
                                    setTopAdvTimer()
                                }
                            } catch (e: Exception) {
                                YandexMetrica.reportError("PlayMusic1", e.message)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        if (previousBanner == null) {
                            clearTopAdv(topBannerAdView)
                            topBannerAdView = null
                            topTextViewAdvHint.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            topProgressAdv.let { childView ->
                                (childView.parent as? ViewGroup)?.removeView(childView)
                            }
                            player_top_adv_banner.removeAllViews()
                            player_top_adv_banner.addView(topTextViewAdvHint)
                            if (topAdvTimer == null) {
                                YandexMetrica.reportEvent("PlayerTopBanner", "error on init")
                            }
                        } else {
                            topBannerAdView = previousBanner
                        }
                        if (topAdvTimer == null) {
                            setTopAdvTimer()
                        }
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
        topBannerAdView?.destroy()
        topBannerAdView?.setBannerAdEventListener(null)
        topBannerAdView = null

        super.onDestroy()
        if (wasServiceInitialized) {
            unbindService(connection)
        }
        timer?.cancel()
    }
}