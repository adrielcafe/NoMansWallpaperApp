package cafe.adriel.nomanswallpaper.util

import cafe.adriel.nomanswallpaper.BuildConfig
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.withContext

object RemoteConfig {
    private const val CONFIG_MIN_VERSION = "android_min_version"

    private val remoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(!BuildConfig.RELEASE)
                .build()
            setConfigSettings(settings)
        }
    }

    suspend fun load() = withContext(Dispatchers.IO){
        with(remoteConfig.fetch(0)) {
            try {
                Tasks.await(this)
                if (isSuccessful) remoteConfig.activateFetched()
            } catch (e: Exception){
                Crashlytics.logException(e)
                e.printStackTrace()
            }
        }
    }

    fun getMinVersion() = remoteConfig.getLong(CONFIG_MIN_VERSION).toInt()

}