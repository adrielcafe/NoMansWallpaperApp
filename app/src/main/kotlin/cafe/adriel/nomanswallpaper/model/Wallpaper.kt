package cafe.adriel.nomanswallpaper.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Wallpaper(
    val id: String = "",
    val url: String = "",
    val thumbUrl: String = "",
    val colorHex: String = "#000000",
    val author: String = "",
    val createdAt: Date = Date()
) : Parcelable