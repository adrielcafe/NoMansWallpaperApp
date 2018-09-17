package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.graphics.Color
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.clearImage
import cafe.adriel.nomanswallpaper.util.loadImage
import cafe.adriel.nomanswallpaper.util.px
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.item_wallpaper.view.*

class WallpaperAdapterItem(val wallpaper: Wallpaper) :
    AbstractItem<WallpaperAdapterItem, WallpaperAdapterItem.ViewHolder>() {

    override fun getLayoutRes() = R.layout.item_wallpaper

    override fun getType() = layoutRes

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val mainColor = try {
            Color.parseColor(wallpaper.mainColorHex)
        } catch (e: Exception){
            Color.BLACK
        }
        val mainColorAlpha = ColorUtils.setAlphaComponent(mainColor, 150)
        with(holder.itemView) {
            setTag(R.id.vItemRoot, this@WallpaperAdapterItem)
            vWallpaper.loadImage(wallpaper.url, mainColor)
            vDetailsLayout.setBackgroundColor(mainColorAlpha)
            vAuthor.text = if (wallpaper.author.isNotBlank()) wallpaper.author else "?"
            vAuthor.compoundDrawablesRelative[0]?.run {
                setTint(Color.WHITE)
                bounds.inset(2.px, 2.px)
            }
        }
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        with(holder.itemView) {
            vWallpaper.clearImage()
            vDetailsLayout.setBackgroundColor(Color.TRANSPARENT)
            vAuthor.text = ""
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}