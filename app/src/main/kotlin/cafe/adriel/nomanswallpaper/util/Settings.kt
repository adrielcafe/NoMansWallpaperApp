package cafe.adriel.nomanswallpaper.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.*
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.background.WallpaperWorker
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

object Settings : SharedPreferences.OnSharedPreferenceChangeListener {
    private const val NOTIFICATION_TOPIC = "wallpaper_added"

    private const val PREF_NOTIFICATIONS = "notifications"
    private const val PREF_AUTO_CHANGE = "auto_change"
    private const val PREF_AUTO_CHANGE_FREQUENCY = "auto_change_frequency"
    private const val PREF_AUTO_CHANGE_NOTIFICATION = "auto_change_notification"

    private const val DEFAULT_AUTO_CHANGE_FREQUENCY = "7" // days

    fun init(context: Context){
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)

        val notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_NOTIFICATIONS, false)
        setNotificationEnabled(notificationsEnabled)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key){
            PREF_NOTIFICATIONS -> sharedPreferences?.run {
                val enabled = getBoolean(PREF_NOTIFICATIONS, false)
                setNotificationEnabled(enabled)
            }
            PREF_AUTO_CHANGE,
            PREF_AUTO_CHANGE_FREQUENCY -> sharedPreferences?.run {
                val enabled = getBoolean(PREF_AUTO_CHANGE, false)
                val frequency = getString(PREF_AUTO_CHANGE_FREQUENCY, DEFAULT_AUTO_CHANGE_FREQUENCY)!!
                val notification = getBoolean(PREF_AUTO_CHANGE_NOTIFICATION, false)
                setAutoChangeEnabled(enabled, frequency.toInt(), notification)
            }
        }
    }

    private fun setNotificationEnabled(enabled: Boolean){
        if(enabled){
            FirebaseMessaging.getInstance().subscribeToTopic(NOTIFICATION_TOPIC)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(NOTIFICATION_TOPIC)
        }
    }

    private fun setAutoChangeEnabled(enabled: Boolean, frequency: Int, showNotification: Boolean){
        WorkManager.getInstance().cancelAllWorkByTag(WallpaperWorker.TAG)
        if(enabled){
            val data = workDataOf(WallpaperWorker.PARAM_SHOW_NOTIFICATION to showNotification)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val work = PeriodicWorkRequestBuilder<WallpaperWorker>(frequency.toLong(), TimeUnit.DAYS)
                .addTag(WallpaperWorker.TAG)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                WallpaperWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, work)
        }
    }

}