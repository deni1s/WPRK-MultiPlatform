package ge.denale.podcastlistener.services.http

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import ge.denale.podcastlistener.data.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val LOCAL_URL = "http://10.0.2.2:8080/"
private const val BACKEND_URL = "https://podcast-listener.herokuapp.com/"

interface ApiService {
    @GET("banners")
    fun getBanners(): Single<List<Banner>>

    @GET("genres")
    fun getCategories(@Query("offset") offset: Int, @Query("limit") limit: Int): Single<List<Genre>>

    @GET("popular-genres")
    fun getCategories(): Single<List<Genre>>

    @GET("authors")
    fun getAuthors(@Query("offset") offset: Int, @Query("limit") limit: Int): Single<List<Author>>

    @GET("popular-authors")
    fun getAuthors(): Single<List<Author>>

    @GET("podcasts")
    fun getMusics(
        @Query("genreId") category_id: String?,
        @Query("offset") offset: Int
    ): Single<List<Music>>

    @GET("podcastsInfo")
    fun getMusicsInfo(
        @Query("podcastId") podcastId: String
    ): Single<Music>

    @POST("account/info")
    fun getUserInfo(): Single<User>

    @GET("podcasts")
    fun getMusicsByAuthor(
        @Query("authorId") author_id: String?,
        @Query("offset") offset: Int
    ): Single<List<Music>>

    @POST("podcasts/markListened")
    fun markTrackListened(
        @Query("genreId") genreId: String,
        @Query("authorId") authorId: String
    ): Completable

    @POST("podcasts/markSeen")
    fun markTrackSeen(
        @Query("podcastId") podcastId: String,
    ): Completable
}

fun createApiService(client: OkHttpClient): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl(BACKEND_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    return retrofit.create(ApiService::class.java)
}

fun createClient(application: Application, sharedPreferences: SharedPreferences): OkHttpClient {
    val client = OkHttpClient.Builder()
        .addInterceptor(AppInfoInterceptor(getAppVersionName(application), sharedPreferences))
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .cache(null)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
    return client.build()
}

private fun getAppVersionName(application: Application): String {
    return try {
        val manager = application.packageManager
        val info = manager.getPackageInfo(application.packageName, 0)
        info.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "n/a"
    }
}

