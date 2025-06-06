package com.example.pokemonshinytracker

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

// DiffUtil class to efficiently convert the old list into the new list
class ShinyHuntDiffCallback : DiffUtil.ItemCallback<ShinyHunt>() {
    override fun areItemsTheSame(oldItem: ShinyHunt, newItem: ShinyHunt): Boolean {
        return oldItem.huntID == newItem.huntID
    }

    override fun areContentsTheSame(oldItem: ShinyHunt, newItem: ShinyHunt): Boolean {
        return oldItem == newItem
    }
}

class ShinyHuntListAdapter(
    private val context: Context,
    private val pokemonSet: List<Pokemon>,
    private val gameSet: List<Game>
) : ListAdapter<ShinyHunt, ShinyHuntListAdapter.ViewHolder>(ShinyHuntDiffCallback()) {

    private val expandedItems = mutableSetOf<Int>()                     // stores the huntIDs of all currently expanded shiny hunts
    private var currentSortMethod: SortMethod = SortMethod.DEFAULT      // stores the current sort method
    private var currentSortOrder: SortOrder = SortOrder.DESC            // stores the current sort order
    var onScrollToPosition: ((Int) -> Unit)? = null                     // variable to scroll to position of the swapped hunt
    var onExpandStateChanged: ((allExpanded: Boolean) -> Unit)? = null  // variable to toggle the state of the expand all checkbox in MainActivity when an individual hunt is expand/collapsed

    // View holder for shiny hunts
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout = view.findViewById(R.id.background)
        val pokemonName: TextView = view.findViewById(R.id.pokemon_name)
        val originGameIconBorder: FrameLayout = view.findViewById(R.id.origin_game_icon_border)
        val originGameIcon: ImageView = view.findViewById(R.id.origin_game_icon)
        val currentGameIconBorder: FrameLayout = view.findViewById(R.id.current_game_icon_border)
        val currentGameIcon: ImageView = view.findViewById(R.id.current_game_icon)
        val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)
        val counterValue: TextView = view.findViewById(R.id.counter_value)
        val counterIncrementBtn: Button = view.findViewById(R.id.increment_counter_button)
        val counterDecrementBtn: Button = view.findViewById(R.id.decrement_counter_button)
        val longClickMenu: LinearLayout = view.findViewById(R.id.long_click_menu)
        val moveUpButton: ImageButton = view.findViewById(R.id.move_up_button)
        val moveDownButton: ImageButton = view.findViewById(R.id.move_down_button)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
    }

    // Create new shiny hunt views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("ShinyHuntListAdapter", "onCreateViewHolder() started")

        // create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shiny_hunt_item, parent, false)

        Log.d("ShinyHuntListAdapter", "onCreateViewHolder() completed. Returning the view holder")
        return ViewHolder(view)
    }

    // Replace the contents of a shiny hunt view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("ShinyHuntListAdapter", "onBindViewHolder() started")

        // extract the shiny hunt from the huntSet
        val hunt = getItem(position)

        Log.d("ShinyHuntListAdapter", "Binding View Holder for shiny hunt: $hunt")
        holder.background.setBackgroundResource(
            if (hunt.isComplete) R.drawable.ui_container_complete_hunt
            else R.drawable.ui_container_incomplete_hunt
        )

        val pokemon = pokemonSet.find { p -> p.forms.any { it.formID == hunt.formID } }
        holder.pokemonName.text = pokemon?.pokemonName ?: "N/A"

        hunt.originGameID?.let {
            holder.originGameIcon.setImageResource(gameSet[it].gameImage)
            holder.originGameIconBorder.visibility = View.VISIBLE
        } ?: run {
            holder.originGameIconBorder.visibility = View.GONE
        }

        hunt.currentGameID?.let {
            holder.currentGameIcon.setImageResource(gameSet[it].gameImage)
            holder.currentGameIconBorder.visibility = View.VISIBLE
        } ?: run {
            holder.currentGameIconBorder.visibility = View.GONE
        }

        holder.pokemonImage.setImageResource(
            if (hunt.formID == null) R.drawable.etc_default
            else pokemon!!.forms.find { it.formID == hunt.formID }!!.formImage
        )

        holder.counterValue.text = hunt.counter.toString()

        // ensure the longClickMenu visibility is correctly set based on the item's state
        holder.longClickMenu.visibility = if (expandedItems.contains(hunt.huntID)) View.VISIBLE else View.GONE

        // set the move buttons' visibility (only enabled when sort method is DEFAULT)
        if (currentSortMethod != SortMethod.DEFAULT) {
            holder.moveUpButton.visibility = View.INVISIBLE
            holder.moveDownButton.visibility = View.INVISIBLE
        } else {
            holder.moveUpButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            holder.moveDownButton.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
        }

        // disable the increment/decrement counter buttons if the hunt is completed (so the user doesn't accidentally click them)
        holder.counterIncrementBtn.isEnabled = !hunt.isComplete
        holder.counterDecrementBtn.isEnabled = !hunt.isComplete

        // on click listener for the increment counter button
        holder.counterIncrementBtn.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Increment counter button clicked for shiny hunt: $hunt")

            // increment the counter
            hunt.counter++
            holder.counterValue.text = hunt.counter.toString()

            // update the database
            DBHelper(context, null).updateHunt(
                hunt.huntID, hunt.formID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID, hunt.defaultPosition
            )
        }

        // on click listener for the decrement counter button
        holder.counterDecrementBtn.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Decrement counter button clicked for shiny hunt: $hunt")

            // decrement the counter (if greater than 0)
            if (hunt.counter > 0) {
                hunt.counter--
                holder.counterValue.text = hunt.counter.toString()
            }

            // update the database
            DBHelper(context, null).updateHunt(
                hunt.huntID, hunt.formID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID, hunt.defaultPosition
            )
        }

        // on long click listener for the view
        holder.background.setOnLongClickListener {
            Log.d("ShinyHuntListAdapter", "View long clicked for shiny hunt: $hunt")

            if (expandedItems.contains(hunt.huntID)) {
                expandedItems.remove(hunt.huntID)
                holder.longClickMenu.visibility = View.GONE
            } else {
                expandedItems.add(hunt.huntID)
                holder.longClickMenu.visibility = View.VISIBLE
            }

            notifyDataSetChanged()

            // notify MainActivity whether all items are now expanded
            onExpandStateChanged?.invoke(expandedItems.size == currentList.size)

            true
        }

        // on click listener for the move up button
        holder.moveUpButton.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Move up button clicked for shiny hunt: $hunt")

            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition > 0) {
                swapItems(currentPosition, currentPosition - 1)
            }
        }

        // on click listener for the move down button
        holder.moveDownButton.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Move down button clicked for shiny hunt: $hunt")

            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition < itemCount - 1) {
                swapItems(currentPosition, currentPosition + 1)
            }
        }

        // on click listener for the edit button
        holder.editButton.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Edit button clicked for shiny hunt: $hunt")

            // switch to the detailed view of the shiny hunt
            val intent = Intent(context, IndividualHunt::class.java).apply {
                putExtra("hunt_id", hunt.huntID)
            }

            Log.d("ShinyHuntListAdapter", "Created intent for selected hunt. Switching to Individual Hunt window")
            context.startActivity(intent)
        }

        Log.d("ShinyHuntListAdapter", "onBindViewHolder() completed")
    }

    // Function to update the current sort method
    fun updateSortMethod(method: SortMethod, order: SortOrder) {
        currentSortMethod = method
        currentSortOrder = order
        notifyDataSetChanged()
    }

    // Function to swap the position of shiny hunts
    private fun swapItems(fromPosition: Int, toPosition: Int) {
        Log.d("ShinyHuntListAdapter", "swapItems() started")

        if (fromPosition in currentList.indices && toPosition in currentList.indices) {
            val mutableHuntSet = currentList.toMutableList()

            val firstHunt = mutableHuntSet[fromPosition]
            val secondHunt = mutableHuntSet[toPosition]

            // commit to database
            val db = DBHelper(context, null)
            db.swapHunts(firstHunt, secondHunt)

            // swap the defaultPosition values in the in-memory objects
            val tempPosition = firstHunt.defaultPosition
            firstHunt.defaultPosition = secondHunt.defaultPosition
            secondHunt.defaultPosition = tempPosition

            // swap items in the list (visual swap)
            Collections.swap(mutableHuntSet, fromPosition, toPosition)

            // update the dataset of the recycler view
            submitList(mutableHuntSet.toList()) {
                // force rebinding after the new list is committed
                notifyItemChanged(fromPosition)
                notifyItemChanged(toPosition)
                onScrollToPosition?.invoke(toPosition)
            }

        }

        Log.d("ShinyHuntListAdapter", "swapItems() completed")
    }

    // Function for expanding all of the shiny hunts' long click menus
    fun expandAll() {
        expandedItems.clear()
        currentList.forEach { hunt -> expandedItems.add(hunt.huntID) }
        notifyDataSetChanged()
    }

    // Function for unexpanding all of the shiny hunts' long click menus
    fun collapseAll() {
        expandedItems.clear()
        notifyDataSetChanged()
    }

    // Function to check if all shiny hunts' long click menus are currently expanded
    fun areAllExpanded(): Boolean {
        return expandedItems.size == currentList.size
    }

}