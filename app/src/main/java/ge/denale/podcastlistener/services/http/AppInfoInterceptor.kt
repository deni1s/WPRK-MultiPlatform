package ge.denale.podcastlistener.services.http

import android.content.SharedPreferences
import ge.denale.podcastlistener.feature.home.USER_ID_KEY
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.UUID

class AppInfoInterceptor(
    val version: String,
    val sharedPreferences: SharedPreferences
) : Interceptor {

    private var userId: String = sharedPreferences.getString(USER_ID_KEY, "").let {
        if (it.isNullOrEmpty()) {
            val newUserId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(USER_ID_KEY, newUserId).apply()
            newUserId
        } else {
            it
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val url: HttpUrl = request.url().newBuilder()
            .addQueryParameter("appVersion", version)
            .addQueryParameter("userId", userId)
            .build()

        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}