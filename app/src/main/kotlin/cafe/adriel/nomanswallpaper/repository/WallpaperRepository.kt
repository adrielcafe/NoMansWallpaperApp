package cafe.adriel.nomanswallpaper.repository

import cafe.adriel.nomanswallpaper.model.Wallpaper
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.withContext

class WallpaperRepository {

    companion object {
        private const val COLLECTION_WALLPAPERS = "wallpapers"
        private const val QUERY_LIMIT = 1000L
    }

    // https://firestore.googleapis.com/v1beta1/projects/nmw-app/databases/(default)/documents/wallpapers
    suspend fun getWallpapers(): List<Wallpaper> = withContext(Dispatchers.IO) {
        val collectionQuery = FirebaseFirestore.getInstance()
            .collection(COLLECTION_WALLPAPERS)
            .limit(QUERY_LIMIT)
            .get()
        with(collectionQuery) {
            Tasks.await(this)
            if(isSuccessful){
                FirebaseFirestore.getInstance().disableNetwork()
                result.toObjects(Wallpaper::class.java).apply {
                    shuffle()
                }
            } else {
                Crashlytics.logException(exception)
                exception?.printStackTrace()
                emptyList()
            }
        }
    }

}