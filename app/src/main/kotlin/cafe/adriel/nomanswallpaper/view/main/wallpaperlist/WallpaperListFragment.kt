package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.isConnected
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperActivity
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperViewModel
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_wallpaper_list.*
import kotlinx.android.synthetic.main.fragment_wallpaper_list.view.*
import kotlinx.android.synthetic.main.item_wallpaper.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class WallpaperListFragment: Fragment() {

    companion object {
        fun newInstance() = WallpaperListFragment()
    }

    private val singleViewModel by viewModel<WallpaperViewModel>()
    private val listViewModel by viewModel<WallpaperListViewModel>()
    private lateinit var adapter: FastItemAdapter<WallpaperAdapterItem>
    private var loadingWallpaper = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallpaper_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!::adapter.isInitialized) {
            adapter = FastItemAdapter()
            adapter.apply {
                setHasStableIds(true)
                withOnClickListener { view, _, item, _ ->
                    view?.vWallpaper?.run {
                        showWallpaper(this, item.wallpaper)
                    }
                    true
                }
                withEventHook(object : ClickEventHook<WallpaperAdapterItem>() {
                    override fun onBindMany(viewHolder: RecyclerView.ViewHolder) =
                        viewHolder.itemView.run { listOf(vSet) }

                    override fun onClick(view: View?, position: Int, fastAdapter: FastAdapter<WallpaperAdapterItem>?, item: WallpaperAdapterItem?) {
                        if (view != null && item != null) {
                            onListItemClicked(view, item)
                        }
                    }
                })
            }
        }

        with(view){
            vRefresh.isRefreshing = true
            vRefresh.setOnRefreshListener {
                if(isConnected()) listViewModel.loadWallpapers()
                else vRefresh.isRefreshing = false
            }

            vWallpaperList.adapter = adapter
        }

        singleViewModel.getWallpaperUpdated().observe(this, Observer { onWallpaperUpdated(it) })
        listViewModel.getWallpapers().observe(this, Observer { showWallpapers(it) })
        listViewModel.loadWallpapers()
    }

    private fun onListItemClicked(view: View, item: WallpaperAdapterItem){
        when(view.id){
            R.id.vSet -> if(isConnected()) singleViewModel.setWallpaper(item.wallpaper, false)
        }
    }

    private fun onWallpaperUpdated(success: Boolean){
        activity?.run {
            Snackbar.make(findViewById(R.id.vRoot),
                if(success) R.string.wallpaper_set else R.string.something_went_wrong,
                Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadingWallpaper = false
    }

    private fun showWallpapers(wallpapers: List<Wallpaper>){
        launch(UI) {
            if (wallpapers.isNotEmpty()) {
                val adapterItems = withContext(CommonPool){
                    wallpapers.map { WallpaperAdapterItem(it) }
                }
                adapter.clear()
                adapter.add(adapterItems)
            }
            vRefresh.isRefreshing = false
        }
    }

    private fun showWallpaper(view: View, wallpaper: Wallpaper){
        if(isConnected()) {
            activity?.run {
                if (!loadingWallpaper) {
                    loadingWallpaper = true
                    WallpaperActivity.start(this, wallpaper, view)
                }
            }
        }
    }

}