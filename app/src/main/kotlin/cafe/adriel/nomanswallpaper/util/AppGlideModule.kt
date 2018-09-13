package cafe.adriel.nomanswallpaper.util

import android.content.Context
import android.util.Log
import cafe.adriel.nomanswallpaper.BuildConfig
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class AppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.apply {
            setLogLevel(if (BuildConfig.RELEASE) Log.ERROR else Log.DEBUG)
            setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
        }
    }

}