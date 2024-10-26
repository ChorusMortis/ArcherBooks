package com.mobdeve.s12.mco

import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val displayMetrics: DisplayMetrics, private val spaceSize: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        val layoutManager = parent.layoutManager

        if (layoutManager?.canScrollHorizontally() == true) {
            outRect.left = spaceSize
            outRect.right = spaceSize

            if (position == 0) {
                outRect.left = 0
            }

            if (position == itemCount - 1) {
                outRect.right = 0
            }
        } else if (layoutManager?.canScrollVertically() == true) {
            outRect.top = spaceSize
            outRect.bottom = spaceSize

            if (position == 0) {
                outRect.top = 0
            }

            if (position == itemCount - 1) {
                outRect.bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spaceSize.toFloat(), displayMetrics).toInt()
            }
        }
    }
}