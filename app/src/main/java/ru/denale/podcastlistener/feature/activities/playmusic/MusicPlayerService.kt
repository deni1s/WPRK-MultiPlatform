package ru.denale.podcastlistener.feature.activities.playmusic

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.data.Music
import ru.denale.podcastlistener.data.repo.MusicRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject


private const val NOTIFICATION_ID = 543
private const val CHANNEL_ID = "podcast player"
private const val STOP_COMMAND = "stop"
const val INITIALIZATION_COMMAND = "initialization"
const val INITIALIZATION_WITH_PODCAST_COMMAND = "initializeWithPodcast"
const val INITIALIZATION_WITH_TYPE_COMMAND = "initializeWithType"
const val INITIALIZATION_WITH_PREVIOUS_COMMAND = "initializeWithPrevious"

sealed class InitializationType {
    data class Type(val id: String) : InitializationType()
    data class PodcastId(val podcastId: String) : InitializationType()
}

class MusicPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val musicRepository: MusicRepository by inject()
    private var downloadManager: DownloadManager? = null
    private var musicPosition: Int = 0

    private var wasDragged = false
    private var isNotified = false
    private var isFirstPlayerInitializationReady = false
    private lateinit var notificationManager: NotificationManagerCompat

    private val eventState = MutableLiveData<MediaServiceEvent>()

    private val binder = PlayerEventBinder(eventState)
    private var isBound = false
    private val compositeDisposable = CompositeDisposable()

    private var musicList: List<Music> = emptyList()
    private var initializationType: InitializationType? = null

    override fun onCreate() {
        super.onCreate()

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.createNotificationChannel(createNotificationChannel())
    }

    private fun createNotificationChannel(): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .setName("Podcast App")
            .setVibrationPattern(longArrayOf(0L))
            .setVibrationEnabled(false)
            .build()
    }

    private fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification) // Start the service in the foreground
    }

    private fun createNotification(music: Music): Notification {
        // Build the notification using a NotificationCompat.Builder

        val notificationLayout = if (musicList.size == 1) {
            getSingleMusicNotification(musicList[musicPosition])
        } else {
            getMultipleMusicNotification(musicList[musicPosition])
        }

        val builder = if (initializationType != null) {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(music.title)
                .setContentText(music.author)
                .setSmallIcon(R.drawable.ic_baseline_monetization_on_24)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                //   .setContentIntent(getClickWholeLayoutIntent())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(null)
                .setVibrate(longArrayOf(0L))
                .setSilent(true)
        } else {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(music.title)
                .setContentText(music.author)
                .setSmallIcon(R.drawable.ic_baseline_monetization_on_24)
                //  .setContentIntent(getClickWholeLayoutIntent())
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(null)
                .setVibrate(longArrayOf(0L))
                .setSilent(true)
        }
        return builder.build()
    }

    private fun getSingleMusicNotification(music: Music): RemoteViews {
        return RemoteViews(packageName, R.layout.service_single_notification_layout).apply {
            setTextViewText(R.id.podcact_author, music.author)
            setTextViewText(R.id.podcact_text, music.title)

            if (mediaPlayer?.isPlaying == true) {
                setImageViewResource(R.id.play_pause, R.drawable.ic_pause)
            } else {
                setImageViewResource(R.id.play_pause, R.drawable.ic_play)
            }
            setOnClickPendingIntent(R.id.play_pause, getPlayPauseIntent())
            setOnClickPendingIntent(R.id.whole_notification_layout, getClickWholeLayoutIntent())
        }
    }

    private fun getMultipleMusicNotification(music: Music): RemoteViews {
        return RemoteViews(packageName, R.layout.service_notification_layout).apply {
            setTextViewText(R.id.podcact_author, music.author)
            setTextViewText(R.id.podcact_text, music.title)

            setOnClickPendingIntent(R.id.whole_notification_layout, getClickWholeLayoutIntent())

            if (musicPosition != 0) {
                setViewVisibility(R.id.play_prevous, View.VISIBLE)
            } else {
                setViewVisibility(R.id.play_prevous, View.GONE)
            }
            if (musicPosition != musicList.lastIndex) {
                setViewVisibility(R.id.play_next, View.VISIBLE)
            } else {
                setViewVisibility(R.id.play_next, View.GONE)
            }

            if (mediaPlayer?.isPlaying == true) {
                setImageViewResource(R.id.play_pause, R.drawable.ic_pause)
            } else {
                setImageViewResource(R.id.play_pause, R.drawable.ic_play)
            }
            setOnClickPendingIntent(R.id.play_prevous, getClickOnPreviousButtonIntent())
            setOnClickPendingIntent(R.id.play_pause, getPlayPauseIntent())
            setOnClickPendingIntent(R.id.play_next, getClickOnNextButtonIntent())
        }
    }

    private fun getClickOnPreviousButtonIntent(): PendingIntent {
        val previousIntent = Intent(applicationContext, MusicPlayerService::class.java).apply {
            action = MediaActivityEvent.onPreviousRequeired.toString()
        }
        return PendingIntent.getService(
            applicationContext,
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDeleteIntent(): PendingIntent {
        val nextIntent = Intent(applicationContext, MusicPlayerService::class.java).apply {
            action = STOP_COMMAND
        }
        return PendingIntent.getService(
            applicationContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getClickOnNextButtonIntent(): PendingIntent {
        val nextIntent = Intent(applicationContext, MusicPlayerService::class.java).apply {
            action = MediaActivityEvent.onNextRequeired.toString()
        }
        return PendingIntent.getService(
            applicationContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getClickWholeLayoutIntent(): PendingIntent {
        val intent = Intent(this, PlayMusic1::class.java)
        val initialType = when (initializationType) {
            is InitializationType.Type -> INITIALIZATION_WITH_TYPE_COMMAND + (initializationType as InitializationType.Type).id
            is InitializationType.PodcastId -> INITIALIZATION_WITH_PODCAST_COMMAND + (initializationType as InitializationType.PodcastId).podcastId
            null -> INITIALIZATION_COMMAND
        }
        val extras = Bundle().apply {
            putString("initialType", initialType)
        }
        intent.action = initialType
        //  intent.putExtras(extras)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE, extras)
    }

    private fun getPlayPauseIntent(): PendingIntent {
        val playIntent = Intent(applicationContext, MusicPlayerService::class.java).apply {
            action = MediaActivityEvent.onPlayRequired.toString()
        }

        return PendingIntent.getService(
            applicationContext,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDefaultNotification(): Notification {
        // Build the notification using a NotificationCompat.Builder
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Медиаплеер")
            .setSmallIcon(R.drawable.default_image)
            //  .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(null)
            .setVibrate(longArrayOf(0L))
            .setSilent(true)
        return builder.build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            MediaActivityEvent.onNextRequeired.toString() -> {
                playNextMusic(true)
                updateNotification(createNotification(musicList[musicPosition]))
            }

            MediaActivityEvent.onPreviousRequeired.toString() -> {
                playPreviousMusic(true)
                updateNotification(createNotification(musicList[musicPosition]))
            }

            MediaActivityEvent.onPlayRequired.toString() -> {
                processAction(MediaActivityEvent.onPlayRequired)
                updateNotification(createNotification(musicList[musicPosition]))
                if (mediaPlayer?.isPlaying != true && !isBound) {
                    stopForeground(false)
                }
            }

            INITIALIZATION_COMMAND -> updateNotification(createDefaultNotification())
            INITIALIZATION_WITH_PREVIOUS_COMMAND -> {
                musicList.getOrNull(musicPosition)?.let {
                    sendMessageToActivity(
                        MediaServiceEvent.onDataUpdate(
                            duration = mediaPlayer?.duration ?: 1,
                            position = mediaPlayer?.currentPosition ?: 0,
                            music = it,
                            isLast = musicList.lastIndex == musicPosition,
                            isFirst = musicPosition == 0,
                            isMusicInProgress = mediaPlayer?.isPlaying == true,
                            type = initializationType
                        )
                    )
                    updateNotification(createNotification(it))
                }
            }

            STOP_COMMAND -> {
                if (!isBound) {
                    stopService()
                }
            }
        }

        if (intent.action?.contains(INITIALIZATION_WITH_PODCAST_COMMAND) == true) {
            val newType = InitializationType.PodcastId(
                intent.action?.removePrefix(INITIALIZATION_WITH_PODCAST_COMMAND).orEmpty()
            )
            if (initializationType != null && newType != initializationType) {
                clearServiceData()
            } else {
                initializationType = newType
                updateNotification(createDefaultNotification())
            }
        }
        if (intent.action?.contains(INITIALIZATION_WITH_TYPE_COMMAND) == true) {
            val newType = InitializationType.Type(
                intent.action?.removePrefix(INITIALIZATION_WITH_TYPE_COMMAND).orEmpty()
            )
            if (initializationType != null && newType != initializationType) {
                clearServiceData()
            } else {
                initializationType = newType

                musicList.getOrNull(musicPosition)?.let {
                    sendMessageToActivity(
                        MediaServiceEvent.onDataUpdate(
                            duration = mediaPlayer?.duration ?: 1,
                            position = mediaPlayer?.currentPosition ?: 0,
                            music = it,
                            isLast = musicList.lastIndex == musicPosition,
                            isFirst = musicPosition == 0,
                            isMusicInProgress = mediaPlayer?.isPlaying == true,
                            type = initializationType
                        )
                    )
                    updateNotification(createNotification(it))
                } ?: updateNotification(createDefaultNotification())
            }
        }

        return START_NOT_STICKY
    }

    private fun playNextMusic(shouldPlay: Boolean) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        // timer?.cancel()
        mediaPlayer = null

        if (musicList.lastIndex == musicPosition) {
            sendMessageToActivity(MediaServiceEvent.onMusicFinished)
            return
        }
        musicPosition++
        musicList[musicPosition].run {
            checkSetListening(authorIds ?: authorId?.let { listOf(it) }, genreIds ?: genreId?.let { listOf(it) })
            if (shouldPlay) {
                sendMessageToActivity(
                    MediaServiceEvent.onMusicLoading(
                        music = this,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0
                    )
                )
            } else {
                sendMessageToActivity(
                    MediaServiceEvent.onMusicDisplay(
                        music = this,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0
                    )
                )
            }
            if (shouldPlay) {
                playMusic(this)
            }
        }
    }

    private fun checkSetListening(genreIds: List<String>?, authorIds: List<String>?) {
        if (!wasDragged) {
            mediaPlayer?.let { player ->
                if (player.currentPosition >= player.duration * 0.7 && !isNotified) {
                    isNotified = true
                    setTrackListened(genreIds, authorIds)
                }
            }
        }
    }

    private fun playPreviousMusic(shouldPlay: Boolean) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        //  timer?.cancel()
        mediaPlayer = null

        if (musicPosition == 0) return
        musicPosition--
        musicList[musicPosition].run {
            checkSetListening(authorIds ?: authorId?.let { listOf(it) }, genreIds ?: genreId?.let { listOf(it) })
            if (shouldPlay) {
                sendMessageToActivity(
                    MediaServiceEvent.onMusicLoading(
                        music = this,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0
                    )
                )
            } else {
                sendMessageToActivity(
                    MediaServiceEvent.onMusicDisplay(
                        music = this,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0
                    )
                )
            }
            if (shouldPlay) {
                playMusic(this)
            }
        }
    }

    private fun seekMusic(progress: Int) {
        if (mediaPlayer != null) {
            wasDragged = true
            mediaPlayer?.seekTo(progress)
        }
    }

    private fun playMusic(music: Music) {
        updateNotification(createNotification(music))
        isNotified = false
        wasDragged = false
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
        mediaPlayer?.setDataSource(this, Uri.parse(music.mediaUrl))
        mediaPlayer?.prepareAsync()

        mediaPlayer?.setOnPreparedListener { player ->
            markTrackSeen(music.id)

            player.start()
            updateNotification(createNotification(music))
            sendMessageToActivity(MediaServiceEvent.onMusicStarted(player.duration.toLong()))
        }
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.setOnPreparedListener(null)
            if (!isNotified) {
                isNotified = true
                setTrackListened(
                    music.genreId?.let { listOf(it) } ?: music.genreIds,
                    music.authorId?.let { listOf(it) } ?: music.authorIds
                )
            }

            playNextMusic(true)
        }

        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            sendMessageToActivity(MediaServiceEvent.onMusicFailed)
            playNextMusic(true)
            true
        }
    }

    private fun setTrackListened(genreIds: List<String>?, authorIds: List<String>?) {
        compositeDisposable.add(
            musicRepository.setTrackListened(genreIds, authorIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({
                    Log.d("listenre", "setTrackListened: success")
                }, {
                    Log.d("listenre", "setTrackListened: failure")
                })
        )
    }

    private fun markTrackSeen(podcastId: String) {
        compositeDisposable.add(
            musicRepository.markTrackSeen(podcastId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete().subscribe({}, {})
        )
    }

    private fun sendMessageToActivity(event: MediaServiceEvent) {
        eventState.value = event
    }

    private fun pauseMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            sendMessageToActivity(MediaServiceEvent.onMusicPaused)
        }
    }

    private fun stopService() {
        // Stop the foreground service and remove the notification
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        clearServiceData()
        super.onDestroy()
    }

    private fun clearServiceData() {
        if (initializationType != null && initializationType is InitializationType.Type) {
            musicList.getOrNull(musicPosition)?.let {
                musicRepository.saveLastSessionData(
                    (initializationType as InitializationType.Type).id,
                    it.id,
                    mediaPlayer?.currentPosition
                )
            }
        }
        initializationType = null
        wasDragged = false
        isNotified = false
        compositeDisposable.clear()
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun processAction(event: MediaActivityEvent) {
        when (event) {
            is MediaActivityEvent.onPauseRequeired -> {
                if (musicList.isNotEmpty()) {
                    pauseMusic()
                }
            }

            is MediaActivityEvent.onPlayRequired -> {
                if (mediaPlayer?.isPlaying == true) {
                    pauseMusic()
                } else if (mediaPlayer?.currentPosition != null && mediaPlayer!!.currentPosition > 0) {
                    sendMessageToActivity(
                        MediaServiceEvent.onMusicContinued(
                            duration = mediaPlayer?.duration ?: 1,
                            position = mediaPlayer?.currentPosition ?: 0
                        )
                    )
                    mediaPlayer?.start()
                    musicList[musicPosition].let { updateNotification(createNotification(it)) }
                } else if (musicList.isNotEmpty()) {
                    musicList[musicPosition].let {
                        sendMessageToActivity(
                            MediaServiceEvent.onMusicLoading(
                                music = it,
                                isLast = musicList.lastIndex == musicPosition,
                                isFirst = musicPosition == 0
                            )
                        )
                        playMusic(it)
                    }
                }
            }

            is MediaActivityEvent.onMusicSeeked -> seekMusic(event.progress)

            is MediaActivityEvent.onNewDataRequired -> {
                if (musicList.isEmpty()) return
                val music = musicList[musicPosition]
                checkSetListening(music.authorIds ?: music.genreId?.let { listOf(it) }, music.genreIds ?: music.genreId?.let { listOf(it) })
                updateNotification(createNotification(music))
                sendMessageToActivity(
                    MediaServiceEvent.onDataUpdate(
                        duration = mediaPlayer?.duration ?: 1,
                        position = mediaPlayer?.currentPosition ?: 0,
                        music = music,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0,
                        isMusicInProgress = mediaPlayer?.isPlaying == true,
                        type = initializationType
                    )
                )
            }

            is MediaActivityEvent.onNextRequeired -> playNextMusic(mediaPlayer?.isPlaying == true)
            is MediaActivityEvent.onPreviousRequeired -> playPreviousMusic(mediaPlayer?.isPlaying == true)
            is MediaActivityEvent.onNewInitializationRequired -> {
                if (initializationType != null && initializationType != event.type) {
                    clearServiceData()
                }
                if (initializationType != event.type && musicList != event.list) {
                    musicList = event.list
                    if (event.type != initializationType) {
                        musicPosition = 0
                    }
                    initializationType = event.type
                    if (!event.podcastId.isNullOrEmpty()) {
                        musicPosition = musicList.indexOfFirst { it.id == event.podcastId }.let {
                            if (it == -1) 0 else it
                        }
                    }

                    musicList.getOrNull(musicPosition)?.let {
                        updateNotification(createNotification(it))
                        sendMessageToActivity(
                            MediaServiceEvent.onDataUpdate(
                                duration = mediaPlayer?.duration ?: 1,
                                position = mediaPlayer?.currentPosition ?: 0,
                                music = it,
                                isLast = musicList.lastIndex == musicPosition,
                                isFirst = musicPosition == 0,
                                isMusicInProgress = mediaPlayer?.isPlaying == true,
                                type = initializationType
                            )
                        )
                    }
                }
            }

            is MediaActivityEvent.onMusicRequeired -> {
                val previousPosition = musicPosition
                musicPosition = musicList.indexOfFirst { it == event.music }
                updateNotification(createNotification(event.music))
                sendMessageToActivity(
                    MediaServiceEvent.onDataUpdate(
                        duration = 1,
                        position = 0,
                        music = event.music,
                        isLast = musicList.lastIndex == musicPosition,
                        isFirst = musicPosition == 0,
                        isMusicInProgress = mediaPlayer?.isPlaying == true,
                        type = initializationType
                    )
                )
                if (mediaPlayer?.isPlaying == true || previousPosition != musicPosition) {
                    sendMessageToActivity(MediaServiceEvent.onMusicPaused)
                    sendMessageToActivity(
                        MediaServiceEvent.onMusicLoading(
                            music = event.music,
                            isLast = musicList.lastIndex == musicPosition,
                            isFirst = musicPosition == 0
                        )
                    )
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    // timer?.cancel()
                    mediaPlayer = null
                    playMusic(event.music)
                }
            }

            null -> {}
        }

    }

    override fun onBind(intent: Intent?): IBinder {
        isBound = true
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        if (mediaPlayer?.isPlaying == false || mediaPlayer == null) {
            clearServiceData()
            stopService()
        }

        return true
    }

    override fun onRebind(intent: Intent?) {
        isBound = true
        super.onRebind(intent)
    }

    inner class PlayerEventBinder(
        val state: LiveData<MediaServiceEvent>
    ) : Binder() {

        fun processEvent(event: MediaActivityEvent) {
            processAction(event)
        }
    }
}