package cafe.adriel.nomanswallpaper.view.main.wallpaperlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.androidcoroutinescopes.appcompat.CoroutineScopedFragment
import cafe.adriel.nomanswallpaper.R
import cafe.adriel.nomanswallpaper.model.Wallpaper
import cafe.adriel.nomanswallpaper.util.isConnected
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperActivity
import cafe.adriel.nomanswallpaper.view.wallpaper.WallpaperViewModel
import com.github.ajalt.timberkt.e
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_wallpaper_list.*
import kotlinx.android.synthetic.main.fragment_wallpaper_list.view.*
import kotlinx.android.synthetic.main.item_wallpaper.view.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class WallpaperListFragment : CoroutineScopedFragment() {

    private val singleViewModel by viewModel<WallpaperViewModel>()
    private val listViewModel by viewModel<WallpaperListViewModel>()
    private lateinit var adapter: FastItemAdapter<WallpaperAdapterItem>
    private var loadingWallpaper = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wallpaper_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!::adapter.isInitialized) {
            adapter = FastItemAdapter()
            adapter.apply {
                setHasStableIds(true)
                withUseIdDistributor(true)
                withOnClickListener { view, _, item, _ ->
                    view?.run {
                        onListItemClicked(this, item)
                    }
                    true
                }
                withEventHook(object : ClickEventHook<WallpaperAdapterItem>() {
                    override fun onBindMany(viewHolder: RecyclerView.ViewHolder) =
                        viewHolder.itemView.run { listOf(vSet) }

                    override fun onClick(
                        view: View?,
                        position: Int,
                        fastAdapter: FastAdapter<WallpaperAdapterItem>?,
                        item: WallpaperAdapterItem?
                    ) {
                        if (view != null && item != null) {
                            onListItemClicked(view, item)
                        }
                    }
                })
            }
        }

        with(view) {
            vRefresh.isRefreshing = true
            vRefresh.setOnRefreshListener {
                listViewModel.loadWallpapers()
            }

            vWallpaperList.adapter = adapter
            vWallpaperList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val manager = recyclerView.layoutManager as LinearLayoutManager
                    vRefresh.isEnabled = manager.findFirstCompletelyVisibleItemPosition() == 0
                }
            })
        }

        singleViewModel.wallpaperUpdated.observe(this, Observer { onWallpaperUpdated(it) })
        listViewModel.wallpapers.observe(this, Observer { showWallpapers(it) })
    }

    override fun onResume() {
        super.onResume()
        loadingWallpaper = false
    }

    private fun onListItemClicked(view: View, item: WallpaperAdapterItem) {
        when (view.id) {
            R.id.vItemRoot -> showWallpaper(view.vWallpaper, item.wallpaper)
            R.id.vSet ->  activity?.run {
                if (isConnected()) {
                    Snackbar.make(findViewById(R.id.vRoot),
                        R.string.downloading_wallpaper, Snackbar.LENGTH_LONG).show()
                    singleViewModel.setWallpaper(item.wallpaper, false)
                }
            }
        }
    }

    private fun onWallpaperUpdated(success: Boolean) {
        activity?.run {
            Snackbar.make(
                findViewById(R.id.vRoot),
                if (success) R.string.wallpaper_set else R.string.something_went_wrong,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showWallpapers(wallpapers: List<Wallpaper>) {
        launch {
            if (wallpapers.isNotEmpty()) {
                val adapterItems = withContext(Dispatchers.Default) {
                    wallpapers.map { WallpaperAdapterItem(it) }
                }
                adapter.clear()
                adapter.add(adapterItems)
            }
            vRefresh.isRefreshing = false
        }
    }

    private fun showWallpaper(view: View, wallpaper: Wallpaper) {
        activity?.run {
            if (!loadingWallpaper) {
                loadingWallpaper = true
                WallpaperActivity.start(this, wallpaper, view)
            }
        }
    }

}