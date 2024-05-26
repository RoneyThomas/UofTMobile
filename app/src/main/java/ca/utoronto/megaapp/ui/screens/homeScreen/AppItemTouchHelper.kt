package ca.utoronto.megaapp.ui.screens.homeScreen

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView

val itemTouchHelper by lazy {
    // 1. Note that I am specifying all 4 directions.
    //    Specifying START and END also allows
    //    more organic dragging than just specifying UP and DOWN.
    val simpleItemTouchCallback =
        object : ItemTouchHelper.SimpleCallback(
            UP or
                    DOWN or
                    START or
                    END, 0
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val adapter = recyclerView.adapter as AppAdapter
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                // 2. Update the backing model. Custom implementation in
                //    MainRecyclerViewAdapter. You need to implement
                //    reordering of the backing model inside the method.
                adapter.moveItem(from, to)
                // 3. Tell adapter to render the model update.
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                // 4. Code block for horizontal swipe.
                //    ItemTouchHelper handles horizontal swipe as well, but
                //    it is not relevant with reordering. Ignoring here.
            }
        }
    ItemTouchHelper(simpleItemTouchCallback)
}