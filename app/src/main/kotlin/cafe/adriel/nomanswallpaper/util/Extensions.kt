package cafe.adriel.nomanswallpaper.util

import android.app.Activity
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.ConnectivityManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.app.ShareCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import cafe.adriel.nomanswallpaper.R
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crashlytics.android.Crashlytics
import org.greenrobot.eventbus.EventBus

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

inline fun <reified T : Any> classTag(): String = T::class.java.simpleName

fun <T> postEvent(event: T) = EventBus.getDefault().post(event)

fun ImageView.loadImage(url: String, @ColorInt placeholderColor: Int = Color.TRANSPARENT) =
    GlideApp.with(context.applicationContext)
        .load(url)
        .placeholder(ColorDrawable(placeholderColor))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

fun ImageView.loadImage(@DrawableRes resId: Int, @ColorInt placeholderColor: Int = Color.TRANSPARENT) =
    GlideApp.with(context.applicationContext)
        .load(resId)
        .placeholder(ColorDrawable(placeholderColor))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

fun ImageView.clearImage() = GlideApp.with(context.applicationContext).clear(this)

fun Context.colorFrom(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, theme)

fun Context.isConnected(showMessage: Boolean = true): Boolean {
    val connected = getSystemService<ConnectivityManager>()
        ?.activeNetworkInfo?.isConnectedOrConnecting ?: false
    if(!connected && showMessage){
        Toast.makeText(this, R.string.connect_internet, Toast.LENGTH_SHORT).show()
    }
    return connected
}

fun Fragment.isConnected(showMessage: Boolean = true) = context?.isConnected(showMessage) ?: false

fun String.share(activity: Activity) =
    ShareCompat.IntentBuilder
        .from(activity)
        .setText(this)
        .setType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        .startChooser()

fun Uri.open(context: Context) =
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, this))
    } catch (e: Exception){
        Crashlytics.logException(e)
        e.printStackTrace()
        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
    }

fun View.inflater() = context.getSystemService<LayoutInflater>()!!

fun ViewGroup.inflate(@LayoutRes resId: Int): View = inflater().inflate(resId, this, true)