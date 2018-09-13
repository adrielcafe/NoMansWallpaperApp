package cafe.adriel.nomanswallpaper.util

import cafe.adriel.nomanswallpaper.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

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

    fun load(callback: () -> Unit) {
        remoteConfig.fetch(0).addOnCompleteListener {
            if(it.isSuccessful) remoteConfig.activateFetched()
            callback()
        }
    }

    fun getMinVersion() = remoteConfig.getLong(CONFIG_MIN_VERSION).toInt()

}