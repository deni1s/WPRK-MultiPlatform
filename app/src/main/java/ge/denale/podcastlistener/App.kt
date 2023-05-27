package ge.denale.podcastlistener

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.nativeads.NativeBulkAdLoader
import ge.denale.podcastlistener.data.AdvertisementMixer
import ge.denale.podcastlistener.data.repo.*
import ge.denale.podcastlistener.data.repo.source.*
import ge.denale.podcastlistener.feature.activities.musics.MusicsViewModel
import ge.denale.podcastlistener.feature.activities.playmusic.PlayMusicViewModel
import ge.denale.podcastlistener.feature.adapter.AuthorsAdapter
import ge.denale.podcastlistener.feature.adapter.CategoryAdapter
import ge.denale.podcastlistener.feature.adapter.MusicAdapter
import ge.denale.podcastlistener.feature.authors.AuthorsViewModel
import ge.denale.podcastlistener.feature.category.CategoryViewModel
import ge.denale.podcastlistener.feature.home.HomeViewModel
import ge.denale.podcastlistener.services.FrescoImageLoadingServiceImpl
import ge.denale.podcastlistener.services.ImageLoadingService
import ge.denale.podcastlistener.services.http.createApiService
import ge.denale.podcastlistener.services.http.createClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

private const val PREFERENCES_NAME = "podcast_preferences"

class App : Application() {

    private val YANDEX_MOBILE_ADS_TAG = "YandexMobileAds"

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
      //  Fresco.initialize(this)

        MobileAds.initialize(
            this
        ) { Log.d(YANDEX_MOBILE_ADS_TAG, "SDK initialized") }
        MobileAds.enableDebugErrorIndicator(true)

        if (!BuildConfig.DEBUG) {
            val config =
                YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_METRICA_APP_KEY).build()
            // Initializing the AppMetrica SDK.
            // Initializing the AppMetrica SDK.
            YandexMetrica.activate(applicationContext, config)
            // Automatic tracking of user activity.
            // Automatic tracking of user activity.
            YandexMetrica.enableActivityAutoTracking(this)
        }

        val myModules = module {
            single { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }
            single { createClient(this@App, get()) }
            single { createApiService(get()) }
            single { NativeBulkAdLoader(this@App) }
            single { AdvertisementMixer() }
            single<ImageLoadingService> { FrescoImageLoadingServiceImpl() }
            factory<BannerRepository> { BannerRepositoryImpl(BannerRemoteDataSource(get())) }
            factory<UserRepository> { UserRepositoryImpl(UserRemoteDataSource(get())) }
            factory<CategoryRepository> { CategoryRepositoryImpl(CategoryRemoteDataSource(get())) }
            factory<AuthorRepository> { AuthorRepositoryImpl(AuthorRemoteDataSource(get())) }
            factory<AdvertisementRepository> { AdvertismentRepositoryImpl(get(), get()) }
            factory { CategoryAdapter(get()) }
            factory { AuthorsAdapter(get()) }
            factory<MusicRepository> { MusicRepositoryImpl(MusicRemoteDataSource(get())) }
            factory { MusicAdapter(get()) }
            viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
            viewModel { AuthorsViewModel(get(), get(), get()) }
            viewModel { CategoryViewModel(get(), get(), get()) }
            viewModel { (bundle: Bundle?) -> MusicsViewModel(bundle, get(), get(), get()) }
            viewModel { (bundle: Bundle) -> PlayMusicViewModel(bundle, get(), get()) }
        }

        startKoin {
            androidContext(this@App)
            modules(myModules)
        }
    }
}