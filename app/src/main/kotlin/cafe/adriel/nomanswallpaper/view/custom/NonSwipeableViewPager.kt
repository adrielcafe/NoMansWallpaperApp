package cafe.adriel.nomanswallpaper.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?) = false

    override fun onTouchEvent(ev: MotionEvent?) = false

}