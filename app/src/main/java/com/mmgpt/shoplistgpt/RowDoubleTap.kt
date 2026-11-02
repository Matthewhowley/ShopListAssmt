package com.mmgpt.shoplistgpt

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Invokes [onDoubleTapRow] with the stable item id (category id) when both taps of a
 * double-tap land on the same row. Requires the adapter to use stable ids.
 */
fun RecyclerView.setRowDoubleTapListener(onDoubleTapRow: (categoryId: Long) -> Unit) {
    var prevId = RecyclerView.NO_ID
    var downId = RecyclerView.NO_ID
    val detector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (downId != RecyclerView.NO_ID && downId == prevId) {
                onDoubleTapRow(downId)
                return true
            }
            return false
        }
    })
    setOnTouchListener { _, e ->
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            prevId = downId
            downId = findChildViewUnder(e.x, e.y)?.let { getChildItemId(it) } ?: RecyclerView.NO_ID
        }
        detector.onTouchEvent(e)
        false
    }
}
