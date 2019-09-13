package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.clearImage
import cafe.adriel.nomanswallpaper.util.drawableFrom
import cafe.adriel.nomanswallpaper.util.loadImage
import cafe.adriel.nomanswallpaper.util.px
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.item_wallpaper.view.*

class WallpaperAdapterItem(val wallpaper: Wallpaper, private val isFavorite: () -> Boolean) : AbstractItem<WallpaperAdapterItem, WallpaperAdapterItem.ViewHolder>() {

    override fun getLayoutRes() = R.layout.item_wallpaper

    @SuppressLint("ResourceType")
    override fun getType() = layoutRes

    override fun getViewHolder(v: View) = ViewHolder(v)

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        with(holder.itemView) {
            val imageColor = try {
                Color.parseColor(wallpaper.colorHex)
            } catch (e: Exception){
                Color.TRANSPARENT
            }
            val placeholder = CircularProgressDrawable(context).apply {
                centerRadius = 25f
                strokeWidth = 5f
                setColorSchemeColors(Color.WHITE)
                start()
            }

            vWallpaper.setBackgroundColor(imageColor)
            vWallpaper.loadImage(wallpaper.url, placeholder)

            if (wallpaper.author.isNotBlank()) {
                vAuthor.visibility = View.VISIBLE
                vAuthor.text = wallpaper.author

                context.drawableFrom(R.drawable.ic_person)?.let {
                    DrawableCompat.setTint(it, Color.WHITE)
                    vAuthor.setCompoundDrawablesRelativeWithIntrinsicBounds(it, null, null, null)
                    it.setBounds(0, 0, 20.px, 20.px)
                }
            } else {
                vAuthor.visibility = View.INVISIBLE
                vAuthor.text = ""
            }

            updateFavorite(this)
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

            updateFavorite(this)
        }
    }

    fun updateFavorite(view: View){
        with(view){
            if(isFavorite()){
                vFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                vFavorite.setImageResource(R.drawable.ic_favorite_outline)
            }
            vFavorite.drawable.alpha = 255
        }
    }

    fun isLoading(view: View): Boolean = view.run {
        vWallpaper.drawable is CircularProgressDrawable
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}
