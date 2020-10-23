package cafe.adriel.nomanswallpaper.background

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.repository.WallpaperRepository
import cafe.adriel.nomanswallpaper.util.GlideApp
import cafe.adriel.nomanswallpaper.util.mmkv
import cafe.adriel.nomanswallpaper.view.main.MainActivity
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.LinkedBlockingQueue

@OptIn(KoinApiExtension::class)
class WallpaperWorker(val context : Context, params : WorkerParameters) : Worker(context, params), KoinComponent {

    companion object {
        // WorkManager
        const val TAG = "set_random_wallpaper"
        const val PARAM_SHOW_NOTIFICATION = "show_notification"
        const val PARAM_ONLY_FAVORITES = "only_favorites"

        // Notification
        const val NOTIFICATION_ID = 0
        const val NOTIFICATION_CHANNEL = "wallpaper_set"

        // MMKV
        const val KEY_WORKER_FIRST_RUN = "worker_first_run"
    }

    private val wallpaperRepo by inject<WallpaperRepository>()

    override fun doWork(): Result {
        val firstRun = mmkv.decodeBool(KEY_WORKER_FIRST_RUN, false)
        if(firstRun){
            mmkv.encode(KEY_WORKER_FIRST_RUN, false)
            return Result.success()
        }

        val result = LinkedBlockingQueue<Result>()
        GlobalScope.launch(Dispatchers.IO) {
            val showNotification = inputData.getBoolean(PARAM_SHOW_NOTIFICATION, false)
            val onlyFavorites = inputData.getBoolean(PARAM_ONLY_FAVORITES, false)
            val wallpapers = wallpaperRepo.getWallpapers().run {
                if(onlyFavorites){
                    val favorites = wallpaperRepo.getFavorites()
                    if(favorites.isNotEmpty()){
                        return@run filter { it.id in favorites }
                    }
                }
                this
            }

            if(setRandomWallpaper(wallpapers)) {
                if(showNotification) {
                    showSuccessNotification()
                }
                result.put(Result.success())
            } else {
                result.put(Result.retry())
            }
        }
        return result.take()
    }

    private fun setRandomWallpaper(wallpapers: List<Wallpaper>): Boolean = try {
        val randomPosition = wallpapers.indices.shuffled()[0]
        val wallpaper = wallpapers[randomPosition]
        val wallpaperFile = Glide.with(context)
            .downloadOnly()
            .load(wallpaper.url)
            .submit()
            .get()
        WallpaperManager.getInstance(context).setStream(wallpaperFile.inputStream())
        true
    } catch (e: Exception){
        FirebaseCrashlytics.getInstance().recordException(e)
        e.printStackTrace()
        false
    }

    private fun showSuccessNotification(){
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent= PendingIntent.getActivity(context, 0, openAppIntent, 0)

        val largeIcon = GlideApp.with(context)
            .asBitmap()
            .load(R.mipmap.ic_launcher_foreground)
            .submit()
            .get()

        val notificationManager = context.getSystemService<NotificationManager>()
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setLargeIcon(largeIcon)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(context.getString(R.string.new_wallpaper_set))
            .setContentIntent(pendingIntent)
            .setColor(Color.BLACK)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentChannel = notificationManager?.getNotificationChannel(NOTIFICATION_CHANNEL)
            if(currentChannel == null) {
                val channel = NotificationChannel(NOTIFICATION_CHANNEL,
                    context.getString(R.string.wallpapers), NotificationManager.IMPORTANCE_LOW
                ).apply {
                    enableLights(true)
                    lightColor = Color.RED
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                notificationManager?.createNotificationChannel(channel)
            }
        }

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

}
