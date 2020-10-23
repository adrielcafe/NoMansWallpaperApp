package cafe.adriel.nomanswallpaper.util

import cafe.adriel.nomanswallpaper.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RemoteConfig {
    private const val CONFIG_MIN_VERSION = "android_min_version"

    private val remoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            val settings = FirebaseRemoteConfigSettings
                .Builder()
                .apply {
                    if (BuildConfig.RELEASE) setMinimumFetchIntervalInSeconds(0)
                }
                .build()
            setConfigSettingsAsync(settings)
        }
    }

    suspend fun load() = withContext(Dispatchers.IO){
        with(remoteConfig.fetch(0)) {
            try {
                Tasks.await(this)
                if (isSuccessful) remoteConfig.activate()
            } catch (e: Exception){
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
            }
        }
    }

    fun getMinVersion() = remoteConfig.getLong(CONFIG_MIN_VERSION).toInt()

}
