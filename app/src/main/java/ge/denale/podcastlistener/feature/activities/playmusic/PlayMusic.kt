package ge.denale.podcastlistener.feature.activities.playmusic

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.slider.Slider
import ge.denale.podcastlistener.R
import ge.denale.podcastlistener.common.SCREEN_PODCAST_ID_DATA
import ge.denale.podcastlistener.common.convertMillisToString
import ge.denale.podcastlistener.data.Music
import ge.denale.podcastlistener.feature.advertisment.InterstitialAdActivity
import ge.denale.podcastlistener.services.ImageLoadingService
import kotlinx.android.synthetic.main.activity_play_music.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

private const val ADVERTISMENET_CODE = 23

class PlayMusic : AppCompatActivity() {
    val playMusicViewModel: PlayMusicViewModel by viewModel { parametersOf(intent.extras) }
    val imageLoadingService: ImageLoadingService by inject()
    private var musicPosition: Int = 0
    private var musicList: List<Music> = emptyList()
    private var mediaPlayer: MediaPlayer? = null
    private var isDragging: Boolean = false
    private var timer: Timer? = null
    private var downloadManager: DownloadManager? = null
    private var wasDragged = false
    private var isNotified = false
    private var isAdvertisementAvialable = true
    private var advWasShown = false
    private var isAdvertisementDisplaying = false
    private var isFirstPlayerInitializationReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)

        // val audioManager: AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        intent.data?.getQueryParameter(SCREEN_PODCAST_ID_DATA)?.let {
            playMusicViewModel.loadPodcastId(it)
        }

        playMusicViewModel.musicLiveData.observe(this) { list ->
            musicList = list
            btn_skip_next.isVisible = musicList.size > 1
            btn_skip_previous.isVisible = false
            musicList.firstOrNull()?.let { displayMusicInfo(it) }
        }

        playMusicViewModel.isAdvertisementAvailableData.observe(this) {
            isAdvertisementAvialable = it
        }

        img_share_podcast.setOnClickListener {
            shareText(
                resources.getString(R.string.deeplink_scheme) + "://" + resources.getString(R.string.deeplink_path) + resources.getString(
                    R.string.deeplink_podcast_path
                ) + "?" + SCREEN_PODCAST_ID_DATA + "=" + musicList[musicPosition].id
            )
        }

        img_back_playMusic.setOnClickListener {
            finish()
        }

        slider.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            tv_time_playing.text = convertMillisToString(value.toLong())
        })

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isDragging = true
                wasDragged = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isDragging = false
                if (mediaPlayer != null) {
                    mediaPlayer?.seekTo(slider.value.toInt())
                }
            }
        })

        btn_play_music.setOnClickListener {
            if (isAdvertisementAvialable && !advWasShown) {
                isAdvertisementDisplaying = true
                startActivityForResult(
                    Intent(this, InterstitialAdActivity::class.java),
                    ADVERTISMENET_CODE,
                    null
                )
            }
            playButtonAction()
            advWasShown = true
        }
        btn_skip_previous.setOnClickListener {
            btn_play_music.setImageResource(R.drawable.ic_play)
            playPreviousMusic(false)
        }
        btn_skip_next.setOnClickListener {
//            if (isAdvertisementAvialable && !advWasShown) {
//                isAdvertisementDisplaying = true
//                startActivityForResult(Intent(this, InterstitialAdActivity::class.java), ADVERTISMENET_CODE, null)
//            }
            btn_play_music.setImageResource(R.drawable.ic_play)
            playNextMusic(false)
            //advWasShown = true
        }
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Поделиться через"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADVERTISMENET_CODE) {
            isAdvertisementDisplaying = false
            if (isFirstPlayerInitializationReady) {
                mediaPlayer?.start()
            }
        }
    }

    private fun playButtonAction() {
        if (musicList.isNotEmpty()) {
            if (mediaPlayer == null) {
                val music = musicList[musicPosition]
                playMusic(music)
            } else {
                if (mediaPlayer?.isPlaying == true) {
                    btn_play_music.setImageResource(R.drawable.ic_play)
                    mediaPlayer?.pause()
                } else {
                    btn_play_music.setImageResource(R.drawable.ic_pause)
                    mediaPlayer?.start()
                }
            }
        }
    }

    private fun playPreviousMusic(shouldPlay: Boolean = true) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        timer?.cancel()
        mediaPlayer = null
        slider.value = 0F

        if (musicPosition == 0) return
        musicPosition--
        musicList[musicPosition].run {
            displayMusicInfo(this)
            if (shouldPlay) {
                playMusic(this)
            }
        }
        if (musicPosition == 0) {
            btn_skip_previous.isVisible = false
        }
        btn_skip_next.isVisible = true
    }

    private fun playNextMusic(shouldPlay: Boolean = true) {
        wasDragged = false
        isNotified = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        timer?.cancel()
        mediaPlayer = null
        slider.value = 0F

        if (musicList.lastIndex == musicPosition) return
        musicPosition++
        musicList[musicPosition].run {
            displayMusicInfo(this)
            if (shouldPlay) {
                playMusic(this)
            }
        }
        if (musicList.lastIndex == musicPosition) {
            btn_skip_next.isVisible = false
        }
        btn_skip_previous.isVisible = true
    }

    private fun playMusic(music: Music) {
        btn_play_music.isClickable = false
        //  btn_skip_previous.isClickable = false
        //    btn_skip_next.isClickable = false
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }

        playMusicViewModel.markTrackSeen(music.id)

        val title = "${music.author} - ${music.title}"
        if (checkDownloaded(title)) {
            mediaPlayer?.setDataSource(this, Uri.fromFile(getMusicFile(title)))
        } else {
            PlayProgress.show()
            mediaPlayer?.setDataSource(this, Uri.parse(music.mediaUrl))
        }

        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener { player ->
            btn_play_music.isClickable = true
            tv_time_music.isVisible = true
            isFirstPlayerInitializationReady = true
            tv_time_music.text = convertSecondsToMMSS(player.duration.toLong())
            //  btn_skip_previous.isClickable = true
            //  btn_skip_next.isClickable = true
            runOnUiThread {
                slider.value = 0F
                slider.valueTo = player.duration.toFloat()
            }
            PlayProgress.hide()
            btn_play_music.setImageResource(R.drawable.ic_pause)
            slider.valueTo = player.duration.toFloat()
            if (!isAdvertisementDisplaying) {
                player.start()
            }

            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        player.currentPosition.let { current ->
                            val currentPosition: Float = current.toFloat()
                            if (!isNotified && !wasDragged && currentPosition >= player.duration * 0.7) {
                                isNotified = true
                                playMusicViewModel.setTrackListened(music.genreId, music.authorId)
                            }
                            if (!isDragging && currentPosition <= player.duration.toFloat()) {
                                runOnUiThread {
                                    slider.value = currentPosition
                                }
                            }
                        }
                    } catch (e: Exception) {

                    }
                    //  }
                }

            }, 1000, 1000)
        }
        mediaPlayer?.setOnCompletionListener {
            //  slider.value = 0F
            mediaPlayer?.setOnPreparedListener(null)
            playNextMusic()
            btn_play_music.setImageResource(R.drawable.ic_play)
        }
        mediaPlayer?.setOnErrorListener { mp, what, extra ->
            Toast.makeText(this@PlayMusic, "Ошибка вопроизведения", Toast.LENGTH_SHORT).show()
            playNextMusic()
            true
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

    private fun displayMusicInfo(music: Music) {
        val title = "${music.author} - ${music.title}"
        imageLoadingService.load(cover_music, music.imageUrl, this)
        tv_name_music.text = music.title
        tv_singer_music.text = music.author
        tv_time_music.isVisible = !music.durationString.isNullOrEmpty()
        tv_time_music.text = music.durationString
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        wasDragged = false
        isNotified = false
        timer?.cancel()
    }


    private fun download(title: String, description: String?, uri: String) {
        if (checkDownloaded(title)) {
            return
        }

        val request = DownloadManager.Request(Uri.parse(uri))
        val appName = getString(R.string.app_name)
        val file = File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "${title}.mp3")
        request.setTitle("$appName: $title")

        description?.let {
            request.setDescription(description)
        }

        request.setDestinationUri(Uri.fromFile(file))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)


        downloadManager?.enqueue(request)
    }

    private fun checkDownloaded(title: String): Boolean {
        val file = File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "${title}.mp3")
        return file.isFile
    }

    private fun getMusicFile(title: String): File {
        val file = File(getExternalFilesDir(DIRECTORY_DOWNLOADS), "${title}.mp3")
        return file
    }
}