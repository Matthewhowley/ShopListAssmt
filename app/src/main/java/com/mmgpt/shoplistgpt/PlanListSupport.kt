package com.mmgpt.shoplistgpt

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mmgpt.shoplistgpt.adapter.ItemAdapter

/** Plan lists show an "add" placeholder row before and after the real content. */
fun List<ShoppingItem>.withPlaceholders(): List<ShoppingItem> =
    listOf(ShoppingItem(-1, "", isPlaceholder = true)) +
        this +
        ShoppingItem(-2, "", isPlaceholder = true)

/**
 * Drag-reorder helper for Plan lists. [onReorder] receives indices into the real content,
 * i.e. already adjusted for the leading placeholder row.
 */
fun createPlanDragHelper(
    adapterProvider: () -> ItemAdapter,
    onReorder: (from: Int, to: Int) -> Unit,
): ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val adapter = adapterProvider()
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition
        val itemFrom = adapter.currentList.getOrNull(from)
        val itemTo = adapter.currentList.getOrNull(to)
        if (itemFrom?.isPlaceholder == true || itemTo?.isPlaceholder == true) return false
        onReorder(from - 1, to - 1)
        adapter.moveItem(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isLongPressDragEnabled(): Boolean = false
})

fun Fragment.showTextEditDialog(current: String, onConfirm: (String) -> Unit) {
    val input = EditText(requireContext()).apply { setText(current) }
    AlertDialog.Builder(requireContext())
        .setTitle(getString(R.string.edit))
        .setView(input)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) onConfirm(text)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}
