package cafe.adriel.nomanswallpaper.repository

import cafe.adriel.nomanswallpaper.model.Wallpaper
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.withContext

class WallpaperRepository {

    // TODO dummy data
    suspend fun getWallpapers() = withContext(IO){
        listOf(
            Wallpaper(0, "https://nmswp.azureedge.net/wp-content/uploads/2017/02/Diplo.png", "#B95644", "/u/user1"),
            Wallpaper(1, "https://nmswp.azureedge.net/wp-content/uploads/2017/02/Fleet.png", "#952074", "@user2"),
            Wallpaper(2, "https://nmswp.azureedge.net/wp-content/uploads/2017/02/Walkers.png", "#CFF2B9", "/u/user3"),
            Wallpaper(3, "https://nmswp.azureedge.net/wp-content/uploads/2017/02/NightDrone.png", "#2F1631", "@user4"),
            Wallpaper(4, "https://nmswp.azureedge.net/wp-content/uploads/2017/02/BlueSpace.png", "#2F395F")
        ).shuffled()
    }

}