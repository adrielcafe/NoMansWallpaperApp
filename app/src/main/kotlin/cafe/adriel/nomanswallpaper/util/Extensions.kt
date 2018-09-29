package cafe.adriel.nomanswallpaper.util

import android.app.Activity
import android.content.*
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import androidx.annotation.LayoutRes
import androidx.core.app.ShareCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import cafe.adriel.nomanswallpaper.R
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.crashlytics.android.Crashlytics

// General
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun <T> classTag(): String = T::class.java.simpleName

fun String.share(activity: Activity) =
    ShareCompat.IntentBuilder
        .from(activity)
        .setText(this)
        .setType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        .startChooser()

fun String.copyToClipboard(context: Context) = try {
    val clipData = ClipData.newPlainText("Wallpaper URL", this)
    context.getSystemService<ClipboardManager>()?.primaryClip = clipData
    true
} catch (e: java.lang.Exception) {
    false
}

// Context
fun Context.colorFrom(@ColorRes colorRes: Int) = ResourcesCompat.getColor(resources, colorRes, theme)

fun Context.isConnected(showErrorMessage: Boolean = true): Boolean {
    val connected = getSystemService<ConnectivityManager>()
        ?.activeNetworkInfo?.isConnectedOrConnecting ?: false
    if (!connected && showErrorMessage) {
        Toast.makeText(this, R.string.connect_internet, Toast.LENGTH_SHORT).show()
    }
    return connected
}

fun Fragment.isConnected(showMessage: Boolean = true) = context?.isConnected(showMessage) ?: false

fun Uri.open(context: Context, showErrorMessage: Boolean = true) = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, this))
} catch (e: Exception) {
    if(showErrorMessage) {
        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
    }
    Crashlytics.logException(e)
    e.printStackTrace()
}

// View
fun ViewGroup.inflate(@LayoutRes resId: Int): View? =
    context.getSystemService<LayoutInflater>()?.inflate(resId, this, true)

fun ImageView.loadImage(url: Any, @ColorInt placeholder: Int = Color.TRANSPARENT, listener: RequestListener<Drawable>? = null) =
    loadImage(url, ColorDrawable(placeholder), listener)

fun ImageView.loadImage(url: Any, @ColorInt placeholder: Drawable? = null, listener: RequestListener<Drawable>? = null) =
    GlideApp.with(context.applicationContext)
        .load(url)
        .placeholder(placeholder)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(listener)
        .into(this)

fun ImageView.clearImage() = GlideApp.with(context.applicationContext).clear(this)