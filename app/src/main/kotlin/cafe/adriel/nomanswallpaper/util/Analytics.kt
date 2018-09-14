package cafe.adriel.nomanswallpaper.util

import android.content.Context
import android.os.Bundle
import cafe.adriel.nomanswallpaper.model.Wallpaper
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    private const val EVENT_SET_WALLPAPER = "set_wallpaper"
    private const val EVENT_DOWNLOAD_WALLPAPER = "download_wallpaper"
    private const val EVENT_SHARE_WALLPAPER = "share_wallpaper"
    private const val EVENT_COPY_WALLPAPER_URL = "copy_wallpaper_url"
    private const val EVENT_RATE_APP = "rate_app"
    private const val EVENT_SHARE_APP = "share_app"
    private const val EVENT_SEND_EMAIL = "send_email"
    private const val EVENT_OPEN_URL = "open_url"
    private const val EVENT_DONATE = "donate"

    private const val PARAM_URL = "url"
    private const val PARAM_SKU = "sku"

    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context){
        if(analytics == null){
            analytics = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    fun logSetWallpaper(wallpaper: Wallpaper){
        val params = Bundle().apply {
            putString(PARAM_URL, wallpaper.url)
        }
        analytics?.logEvent(EVENT_SET_WALLPAPER, params)
    }

    fun logDownloadWallpaper(wallpaper: Wallpaper){
        val params = Bundle().apply {
            putString(PARAM_URL, wallpaper.url)
        }
        analytics?.logEvent(EVENT_DOWNLOAD_WALLPAPER, params)
    }

    fun logRateApp(){
        analytics?.logEvent(EVENT_RATE_APP, null)
    }

    fun logShareApp(){
        analytics?.logEvent(EVENT_SHARE_APP, null)
    }

    fun logShareWallpaper(wallpaper: Wallpaper){
        val params = Bundle().apply {
            putString(PARAM_URL, wallpaper.url)
        }
        analytics?.logEvent(EVENT_SHARE_WALLPAPER, params)
    }

    fun logCopyWallpaperUrl(wallpaper: Wallpaper){
        val params = Bundle().apply {
            putString(PARAM_URL, wallpaper.url)
        }
        analytics?.logEvent(EVENT_COPY_WALLPAPER_URL, params)
    }

    fun logSendEmail(){
        analytics?.logEvent(EVENT_SEND_EMAIL, null)
    }

    fun logOpenUrl(url: String){
        val params = Bundle().apply {
            putString(PARAM_URL, url)
        }
        analytics?.logEvent(EVENT_OPEN_URL, params)
    }

    fun logDonate(sku: String){
        val params = Bundle().apply {
            putString(PARAM_SKU, sku)
        }
        analytics?.logEvent(EVENT_DONATE, params)
    }

}