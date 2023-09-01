package ru.denale.podcastlistener

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.mobile.ads.common.MobileAds
import ru.denale.podcastlistener.data.database.MusicDatabase
import ru.denale.podcastlistener.data.repo.*
import ru.denale.podcastlistener.data.repo.source.*
import ru.denale.podcastlistener.feature.activities.musics.MusicsViewModel
import ru.denale.podcastlistener.feature.activities.playmusic.PlayMusicViewModel
import ru.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ru.denale.podcastlistener.feature.adapter.CategoryAdapter
import ru.denale.podcastlistener.feature.adapter.MusicAdapter
import ru.denale.podcastlistener.feature.authors.AuthorsViewModel
import ru.denale.podcastlistener.feature.category.CategoryViewModel
import ru.denale.podcastlistener.feature.home.HomeViewModel
import ru.denale.podcastlistener.services.FrescoImageLoadingServiceImpl
import ru.denale.podcastlistener.services.ImageLoadingService
import ru.denale.podcastlistener.services.http.createApiService
import ru.denale.podcastlistener.services.http.createClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

private const val PREFERENCES_NAME = "podcast_preferences"
private const val DATABASE_NAME = "podcast-types-db"

class App : Application() {

    private val YANDEX_MOBILE_ADS_TAG = "YandexMobileAds"

    override fun onCreate() {
        super.onCreate()
        //Timber.plant(Timber.DebugTree())
        //  Fresco.initialize(this)

        MobileAds.initialize(
            this
        ) { Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized") }
        MobileAds.enableDebugErrorIndicator(true)

        if (!BuildConfig.DEBUG) {
            val config =
                YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_APP_METRICS).build();
            // Initializing the AppMetrica SDK.
            YandexMetrica.activate(applicationContext, config);
            // Automatic tracking of user activity.
            YandexMetrica.enableActivityAutoTracking(this)
        }

        val myModules = module {
            single { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }
            single { createClient(this@App, get()) }
            single { createApiService(get()) }
            single {
                Room.databaseBuilder(this@App, MusicDatabase::class.java, DATABASE_NAME)
                    .build().musicDao()
            }
            //single { database.musicDao() }
            single<ImageLoadingService> { FrescoImageLoadingServiceImpl() }
            factory<BannerRepository> { BannerRepositoryImpl(BannerRemoteDataSource(get())) }
            factory<UserRepository> { UserRepositoryImpl(UserRemoteDataSource(get())) }
            factory<CategoryRepository> { CategoryRepositoryImpl(CategoryRemoteDataSource(get())) }
            factory<AuthorRepository> { AuthorRepositoryImpl(AuthorRemoteDataSource(get())) }
            factory<AdvertisementRepository> { AdvertismentRepositoryImpl(get()) }
            factory { CategoryAdapter(get()) }
            factory { AuthorsAdapter(get()) }
            factory<MusicRepository> {
                MusicRepositoryImpl(
                    MusicRemoteDataSource(get()),
                    get(),
                    get()
                )
            }
            factory { MusicAdapter(get()) }
            viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
            viewModel { AuthorsViewModel(get(), get()) }
            viewModel { CategoryViewModel(get(), get()) }
            viewModel { (bundle: Bundle?) -> MusicsViewModel(bundle, get(), get()) }
            viewModel { (bundle: Bundle?) -> PlayMusicViewModel(bundle, get(), get(), get()) }
        }

        startKoin {
            androidContext(this@App)
            modules(myModules)
        }
    }

    @GlideModule
    class MyAppGlideModule :
        AppGlideModule() { // Empty class required for Glide's annotation processor
    }
}