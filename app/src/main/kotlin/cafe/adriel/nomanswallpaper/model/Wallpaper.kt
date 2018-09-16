package cafe.adriel.nomanswallpaper.model

import io.mironov.smuggler.AutoParcelable

data class Wallpaper(
    val id: Long,
    val url: String,
    val mainColorHex: String = "#000",
    val author: String = ""
) : AutoParcelable