package cafe.adriel.nomanswallpaper.view.custom

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class WallpaperImageView(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs) {

    companion object {
        private const val HD_WIDTH = 1080 / 2
        private const val HD_HEIGHT = 1920 / 2
        private val DEVICE_WIDTH = Resources.getSystem().displayMetrics.widthPixels / 2
        private val IMAGE_HEIGHT = (DEVICE_WIDTH * HD_HEIGHT) / HD_WIDTH
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = IMAGE_HEIGHT
        setMeasuredDimension(width, height)
    }

}