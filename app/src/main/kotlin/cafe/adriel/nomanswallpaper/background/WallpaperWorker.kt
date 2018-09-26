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
import cafe.adriel.nomanswallpaper.view.main.MainActivity
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.LinkedBlockingQueue

class WallpaperWorker(val context : Context, params : WorkerParameters) : Worker(context, params) {

    companion object {
        const val TAG = "set_random_wallpaper"

        const val NOTIFICATION_ID = 0
        const val NOTIFICATION_CHANNEL = "wallpaper_set"

        const val PARAM_SHOW_NOTIFICATION = "show_notification"
    }

    private val wallpaperRepo by lazy { WallpaperRepository() }

    override fun doWork(): Result {
        val result = LinkedBlockingQueue<Result>()
        GlobalScope.launch(Dispatchers.IO) {
            val wallpapers = wallpaperRepo.getWallpapers()
            if(setRandomWallpaper(wallpapers)) {
                val showNotification = inputData.getBoolean(PARAM_SHOW_NOTIFICATION, false)
                if(showNotification) {
                    showSuccessNotification()
                }
                result.put(Result.SUCCESS)
            } else {
                result.put(Result.RETRY)
            }
        }
        return result.take()
    }

    private fun setRandomWallpaper(wallpapers: List<Wallpaper>): Boolean = try {
        val randomPosition = (0 until wallpapers.size).shuffled()[0]
        val wallpaper = wallpapers[randomPosition]
        val wallpaperFile = Glide.with(context)
            .downloadOnly()
            .load(wallpaper.url)
            .submit()
            .get()
        WallpaperManager.getInstance(context).setStream(wallpaperFile.inputStream())
        true
    } catch (e: Exception){
        Crashlytics.logException(e)
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