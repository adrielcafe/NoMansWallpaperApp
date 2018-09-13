package cafe.adriel.nomanswallpaper.util

import android.app.Activity
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.app.ShareCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
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

fun String.share(activity: Activity) =
    ShareCompat.IntentBuilder
        .from(activity)
        .setText(this)
        .setType(ClipDescription.MIMETYPE_TEXT_PLAIN)
        .startChooser()

fun Uri.open(context: Context) =
    context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = this@open })

fun View.inflater() = context.getSystemService<LayoutInflater>()!!

fun ViewGroup.inflate(@LayoutRes resId: Int): View = inflater().inflate(resId, this, true)