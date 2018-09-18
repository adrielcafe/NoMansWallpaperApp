package cafe.adriel.nomanswallpaper.model

import io.mironov.smuggler.AutoParcelable

data class Wallpaper(
    val id: String = "",
    val url: String = "",
    val thumbUrl: String = "",
    val colorHex: String = "#000000",
    val author: String = ""
) : AutoParcelable