package cafe.adriel.nomanswallpaper.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wallpaper(
    val id: String = "",
    val url: String = "",
    val thumbUrl: String = "",
    val colorHex: String = "#000000",
    val author: String = ""
) : Parcelable