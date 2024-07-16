package ru.denale.podcastlistener.services.http

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.text.TextUtils
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.denale.podcastlistener.feature.home.USER_ID_KEY
import java.io.IOException
import java.util.UUID

class AppInfoInterceptor(
    val version: String,
    val context: Context,
    val sharedPreferences: SharedPreferences
) : Interceptor {

    private var userId: String = sharedPreferences.getString(USER_ID_KEY, "").let {
        if (it.isNullOrEmpty()) {
            val newUserId = getDeviceId(context)
            sharedPreferences.edit().putString(USER_ID_KEY, newUserId).apply()
            newUserId
        } else {
            it
        }
    }

    fun getDeviceId(context: Context): String {
        val secureId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val uuid =
            if (!TextUtils.isEmpty(secureId)) UUID.nameUUIDFromBytes(secureId.toByteArray()) else UUID.randomUUID()
        return uuid.toString()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val url: HttpUrl = request.url.newBuilder()
            .addQueryParameter("appVersion", version)
            .addQueryParameter("userId", userId)
            .addQueryParameter("os", "android")
            .build()

        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}