package ca.utoronto.megaapp.ui.screens.homeScreen

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView

val itemTouchHelper by lazy {
    val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
        // Drag directions allowed
        UP or DOWN or START or END, 0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter as AppAdapter
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            // Update the state of bookmarks whenever an item moves, implemented in adapter
            adapter.moveItem(from, to)
            // After an bookmark is moved tell the the adapter about the movement, so that it can re-render with new state
            adapter.notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }
    ItemTouchHelper(simpleItemTouchCallback)
}