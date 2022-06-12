package com.muse.wprk

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.muse.wprk.core.utilities.*
import com.muse.wprk.core.utilities.NavigationRoutes.*
import com.muse.wprk.main.PodcastHome
import com.muse.wprk.main.model.Show
import com.muse.wprk.presentation.podcasts.PodcastDetail
import com.muse.wprk.presentation.podcasts.PodcastViewModel
import com.muse.wprk.presentation.shows.ShowDetail
import com.muse.wprk_concept.presentation.MembershipHome
import com.muse.wprk_concept.presentation.ShowHome
import com.mwaibanda.virtualgroceries.Domain.Presentation.Navigation.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.log10
import kotlin.math.roundToInt


@AndroidEntryPoint
class MainActivity : ComponentActivity(), AudioManager.OnAudioFocusChangeListener {
    private lateinit var loudnessEnhancer: LoudnessEnhancer
    private lateinit var player: ExoPlayer
    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayer.Builder(this)
            .build()
            .apply {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                val media = MediaItem.Builder()
                    .setUri(Constants.DEFAULT_STREAM)
                    .setLiveConfiguration(
                        MediaItem.LiveConfiguration.Builder()
                            .build().apply {
                                setPlaybackSpeed(1.02f)
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(C.USAGE_MEDIA)
                                        .setContentType(C.CONTENT_TYPE_MUSIC)
                                        .build(),
                                    true
                                )
                                setForegroundMode(true)
                            })
                    .build()
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(media)

                setMediaSource(source)
                prepare()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(key1 = Unit) {
                enhanceLoudness(player)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel()
                }
            }
            var isPlaying by rememberSaveable { mutableStateOf(false) }
            var currentShow: Show? by rememberSaveable { mutableStateOf(null) }

            WPRKEntry(
                player,
                isPlaying,
                onPlayPauseClick = {
                    isPlaying = it
                }) { navController, navPadding ->
                val backgroundColor = Color.Black
                NavHost(
                    navController = navController,
                    startDestination = SplashScreen.route,
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(navPadding)
                ) {
                    composable(SplashScreen.route) {
                        SplashScreen(navController)
                    }
                    composable(ShowHome.route) {
                        ShowHome(
                            navController = navController,
                            gradient = backgroundColor,
                            showsViewModel = hiltViewModel(),
                            onShowClick = { currentShow = it },
                            onShowSetScheduleClick = { s, c ->
                                setAlarm(context = c, show = s)
                            }
                        ) {
                            switchToURL(it) {
                                isPlaying = true
                            }
                        }
                    }
                    composable(ShowDetail.route) { backStackEntry ->
                        currentShow?.let {
                            ShowDetail(
                                show = it,
                                gradient = backgroundColor
                            )
                        }
                    }
                    composable(PodcastHome.route) {
                        PodcastHome(
                            navController = navController,
                            backgroundColor = backgroundColor,
                            podcastViewModel = hiltViewModel()
                        ) {
                            switchToURL(it) {
                                isPlaying = true
                            }
                        }
                    }
                    composable(
                        PodcastDetail.route,
                        arguments = listOf(
                            navArgument("showID") { defaultValue = "" },
                            navArgument("imageURL") { defaultValue = "" },
                            navArgument("title") { defaultValue = "" },
                            navArgument("subTitle") { defaultValue = "" }
                        )) { backStackEntry ->

                        PodcastDetail(
                            navController = navController,
                            thumbnailURL = backStackEntry.arguments?.getString("imageURL"),
                            showID = backStackEntry.arguments?.getString("showID"),
                            title = backStackEntry.arguments?.getString("title"),
                            description = backStackEntry.arguments?.getString("subTitle"),
                            gradient = backgroundColor,
                            podcastViewModel = hiltViewModel<PodcastViewModel>()
                        ) {
                            switchToURL(it) {
                                isPlaying = true
                            }
                        }

                    }

                    composable(Membership.route) {
                        MembershipHome(backgroundColor = backgroundColor) {
                            switchToURL(it) {
                                isPlaying = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun switchToURL(
        URL: String,
        onCompletion: () -> Unit
    ) {
        player.apply {
            val dataSourceFactory = DefaultHttpDataSource.Factory()

            val sourceURL = MediaItem.Builder()
                .setUri(URL)
                .setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build().apply {
                    setPlaybackSpeed(1.02f)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.CONTENT_TYPE_MUSIC)
                            .build(),
                        true
                    )
                    setForegroundMode(true)
                })
                .build()

            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(sourceURL)


            setMediaSource(source)
            prepare()
            playWhenReady = true

        }.also { enhanceLoudness(it) }

        onCompletion()
    }

    private fun enhanceLoudness(player: ExoPlayer) {
        try {
            val audioPct = 1.2
            val gainMB = (log10(audioPct) * 10000).roundToInt()
            loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
            loudnessEnhancer.setTargetGain(gainMB)
            loudnessEnhancer.enabled = true
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAlarm(context: Context, show: Show) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.titleKey, "Reminder ⏰")
            putExtra(NotificationReceiver.messageKey, "${show.title} Is Now Live On WPRK 91.5FM")
            putExtra(NotificationReceiver.notificationId, show.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, show.id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + 10000 * 6, pendingIntent)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getShowTimeInMillis(show: Show): Long {

        val showDateTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(5)
        val minute = showDateTime.minute
        val hour = showDateTime.hour
        val day  = showDateTime.dayOfMonth
        val month = showDateTime.monthValue
        val year = showDateTime.year
        val calender = Calendar.getInstance()
        calender.set(year, month, day, hour, minute)
        Log.d("SCH", "${day}/${month}/${year}  ${hour}:${minute}")

        return calender.timeInMillis - System.currentTimeMillis()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NotificationReceiver.channelId, NotificationReceiver.name, importance)
        channel.description = NotificationReceiver.description

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    override fun onResume() {
        super.onResume()
        audioManager.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AUDIOFOCUS_LOSS_TRANSIENT -> {
                player.pause()
            }
            AUDIOFOCUS_GAIN -> {
                player.play()
            }
        }
    }
}
