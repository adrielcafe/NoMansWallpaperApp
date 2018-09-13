package cafe.adriel.nomanswallpaper

import android.app.Application
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import cafe.adriel.nomanswallpaper.util.Analytics
import cafe.adriel.nomanswallpaper.view.main.MainViewModel
import cafe.adriel.nomanswallpaper.view.main.wallpaperlist.WallpaperListViewModel
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperViewModel
import com.github.ajalt.timberkt.Timber
import org.koin.android.ext.android.startKoin
import org.koin.android.logger.AndroidLogger
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.core.Koin
import org.koin.dsl.module.module
import org.koin.log.EmptyLogger

class App : Application() {

    companion object {
        // Contact Links
        const val EMAIL = "me@adriel.cafe"
        const val WEBSITE = "http://adriel.cafe"
        const val GITHUB_PROFILE_URL = "https://github.com/adrielcafe"
        const val LINKEDIN_PROFILE_URL = "https://linkedin.com/in/adrielcafe"
        const val PROJECT_REPO_URL = "https://github.com/adrielcafe/NoMansWallpaperApp"

        // App Links
        const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        const val MARKET_URL = "market://details?id=${BuildConfig.APPLICATION_ID}"

        // In-App Purchase
        const val PRODUCT_SKU_COFFEE_1 = "coffee_1"
        const val PRODUCT_SKU_COFFEE_3 = "coffee_3"
        const val PRODUCT_SKU_COFFEE_5 = "coffee_5"
    }

    // DI
    private val viewModelsModule = module {
        viewModel { MainViewModel(this@App) }
        viewModel { WallpaperViewModel(this@App) }
        viewModel { WallpaperListViewModel(this@App, get()) }
    }
    private val repositoriesModule = module {
        single { WallpaperRepository() }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(repositoriesModule, viewModelsModule))
        Analytics.init(this)
        initLogging()
    }

    private fun initLogging(){
        if (BuildConfig.RELEASE) {
            Koin.logger = EmptyLogger()
        } else {
            Timber.plant(Timber.DebugTree())
            Koin.logger = AndroidLogger()
        }
    }

}