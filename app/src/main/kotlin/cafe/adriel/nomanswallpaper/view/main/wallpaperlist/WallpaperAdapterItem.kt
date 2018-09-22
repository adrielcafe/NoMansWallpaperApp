package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
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
        val imageColor = try {
            Color.parseColor(wallpaper.colorHex)
        } catch (e: Exception){
            Color.BLACK
        }
        with(holder.itemView) {
            setTag(R.id.vItemRoot, this@WallpaperAdapterItem)
            if(wallpaper.thumbUrl.isNotBlank()) {
                vWallpaper.loadImage(wallpaper.thumbUrl, imageColor)
            } else {
                vWallpaper.loadImage(wallpaper.url, imageColor)
            }
            if (wallpaper.author.isNotBlank()) {
                vAuthor.visibility = View.VISIBLE
                vAuthor.text = wallpaper.author

                val userIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_person, null)
                userIcon?.let {
                    DrawableCompat.setTint(it, Color.WHITE)
                    vAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        InsetDrawable(it, 2.px), null, null, null)
                }
            } else {
                vAuthor.visibility = View.INVISIBLE
                vAuthor.text = ""
            }
        }
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        with(holder.itemView) {
            vWallpaper.clearImage()
            vDetailsLayout.setBackgroundColor(Color.TRANSPARENT)
            vAuthor.text = ""
            vAuthor.visibility = View.INVISIBLE
            vAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}