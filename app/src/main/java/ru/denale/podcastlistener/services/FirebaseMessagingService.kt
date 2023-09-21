package ru.denale.podcastlistener.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ru.denale.podcastlistener.R
import ru.denale.podcastlistener.common.SCREEN_TITLE_DATA

private const val CHANNEL_ID = "445"
const val PODCAST_ID_KEY = "podcastId"
const val CATEGORY_ID_KEY = "categoryId"
const val AUTHOR_ID_KEY = "authorId"
const val WAVE_ID_KEY = "waveId"

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            val title = notification.title
            val message = notification.body
            if (title != null && message != null) {
                val extras = configureExtras(remoteMessage.data)
                showNotification(title, message, extras)
            }
        }
    }

    private fun configureExtras(data: MutableMap<String, String>): Bundle {
        val bundle = Bundle()
        data[PODCAST_ID_KEY]?.let { bundle.putString(PODCAST_ID_KEY, it) }
        data[CATEGORY_ID_KEY]?.let { bundle.putString(CATEGORY_ID_KEY, it) }
        data[AUTHOR_ID_KEY]?.let { bundle.putString(AUTHOR_ID_KEY, it) }
        data[WAVE_ID_KEY]?.let { bundle.putString(WAVE_ID_KEY, it) }
        data[SCREEN_TITLE_DATA]?.let { bundle.putString(SCREEN_TITLE_DATA, it) }
        return bundle
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun showNotification(title: String, message: String, data: Bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Da Success Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_monetization_on_24)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentTitle(title)
            .setContentText(message)
            .addExtras(data)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(35, builder.build())
    }
}
