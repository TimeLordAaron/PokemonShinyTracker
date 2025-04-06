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
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class ShinyHuntListAdapter(private val context: Context, private var huntSet: List<ShinyHunt>, private val pokemonSet: List<Pokemon>, private val gameSet: List<Game>) :
    RecyclerView.Adapter<ShinyHuntListAdapter.ViewHolder>() {

    private val expandedItems = mutableSetOf<Int>() // stores the positions of expanded items

    // View holder for shiny hunts
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout
        val pokemonName: TextView
        val originGameIconBorder: FrameLayout
        val originGameIcon: ImageView
        val currentGameIconBorder: FrameLayout
        val currentGameIcon: ImageView
        val pokemonImage: ImageView
        val counterValue: TextView
        val counterIncrementBtn: Button
        val counterDecrementBtn: Button
        val longClickMenu: LinearLayout
        val moveUpButton: ImageButton
        val moveDownButton: ImageButton
        val editButton: ImageButton
        var longClickMenuOpened: Boolean

        init {
            background = view.findViewById(R.id.background)
            pokemonName = view.findViewById(R.id.pokemon_name)
            originGameIconBorder = view.findViewById(R.id.origin_game_icon_border)
            originGameIcon = view.findViewById(R.id.origin_game_icon)
            currentGameIconBorder = view.findViewById(R.id.current_game_icon_border)
            currentGameIcon = view.findViewById(R.id.current_game_icon)
            pokemonImage = view.findViewById(R.id.pokemon_image)
            counterValue = view.findViewById(R.id.counter_value)
            counterIncrementBtn = view.findViewById(R.id.increment_counter_button)
            counterDecrementBtn = view.findViewById(R.id.decrement_counter_button)
            longClickMenu = view.findViewById(R.id.long_click_menu)
            moveUpButton = view.findViewById(R.id.move_up_button)
            moveDownButton = view.findViewById(R.id.move_down_button)
            editButton = view.findViewById(R.id.edit_button)
            longClickMenuOpened = false
        }
    }

    // Create new shiny hunt views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("ShinyHuntListAdapter", "onCreateViewHolder() started")

        // create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.shiny_hunt_item, viewGroup, false)

        Log.d("ShinyHuntListAdapter", "onCreateViewHolder() completed")
        return ViewHolder(view)
    }

    // Replace the contents of a shiny hunt view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d("ShinyHuntListAdapter", "onBindViewHolder() started")

        // extract the shiny hunt from the huntSet
        val hunt = huntSet[position]

        Log.d("ShinyHuntListAdapter", "Binding View Holder for shiny hunt: $hunt")
        viewHolder.background.setBackgroundResource( if (hunt.isComplete) R.drawable.ui_container_complete_hunt else R.drawable.ui_container_incomplete_hunt )
        val pokemon = pokemonSet.find { p -> p.forms.any { it.formID == hunt.formID } }
        viewHolder.pokemonName.text = pokemon?.pokemonName ?: "N/A"
        if (hunt.originGameID != null) {
            viewHolder.originGameIcon.setImageResource(gameSet[hunt.originGameID!!].gameImage)
            viewHolder.originGameIconBorder.visibility = View.VISIBLE
        } else {
            viewHolder.originGameIconBorder.visibility = View.GONE
        }
        if (hunt.currentGameID != null) {
            viewHolder.currentGameIcon.setImageResource(gameSet[hunt.currentGameID!!].gameImage)
            viewHolder.currentGameIconBorder.visibility = View.VISIBLE
        } else {
            viewHolder.currentGameIconBorder.visibility = View.GONE
        }
        viewHolder.pokemonImage.setImageResource(if (hunt.formID == null) R.drawable.etc_default else pokemon!!.forms.find { it.formID == hunt.formID }!!.formImage)
        viewHolder.counterValue.text = hunt.counter.toString()

        // ensure the longClickMenu visibility is correctly set based on the item's state
        viewHolder.longClickMenu.visibility = if (expandedItems.contains(position)) View.VISIBLE else View.GONE

        // set the move buttons' visibility
        viewHolder.moveUpButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        viewHolder.moveDownButton.visibility = if (position == huntSet.size - 1) View.INVISIBLE else View.VISIBLE

        // on click listener for the increment counter button
        viewHolder.counterIncrementBtn.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Increment counter button clicked for shiny hunt: $hunt")

            // increment the counter
            hunt.counter++
            viewHolder.counterValue.text = hunt.counter.toString()

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.formID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // on click listener for the decrement counter button
        viewHolder.counterDecrementBtn.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Decrement counter button clicked for shiny hunt: $hunt")

            // decrement the counter (if greater than 0)
            if (hunt.counter > 0) {
                hunt.counter--
                viewHolder.counterValue.text = hunt.counter.toString()
            }

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.formID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // on long click listener for the view
        viewHolder.background.setOnLongClickListener {
            Log.d("ShinyHuntListAdapter", "View long clicked for shiny hunt: $hunt")

            if (expandedItems.contains(position)) {
                Log.d("ShinyHuntListAdapter", "Closing long click menu for shiny hunt: $hunt")
                expandedItems.remove(position)
                viewHolder.longClickMenu.visibility = View.GONE
            } else {
                Log.d("ShinyHuntListAdapter", "Opening long click menu for shiny hunt: $hunt")
                expandedItems.add(position)
                viewHolder.longClickMenu.visibility = View.VISIBLE
            }

            notifyDataSetChanged()
            true
        }

        // on click listener for the move up button
        viewHolder.moveUpButton.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Move up button clicked for shiny hunt: $hunt")

            if (position > 0) { // ensure it's not the first item
                swapItems(position, position - 1)
            }
        }

        // on click listener for the move down button
        viewHolder.moveDownButton.setOnClickListener {
            Log.d("ShinyHuntListAdapter", "Move down button clicked for shiny hunt: $hunt")

            if (position < huntSet.size - 1) { // ensure it's not the last item
                swapItems(position, position + 1)
            }
        }

        // on click listener for the edit button
        viewHolder.editButton.setOnClickListener {
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

    // Function to swap the position of shiny hunts
    // TODO: Commit the position changes to the database
    private fun swapItems(fromPosition: Int, toPosition: Int) {
        Log.d("ShinyHuntListAdapter", "swapItems() started")

        // ensure valid position for swapping
        if (fromPosition in huntSet.indices && toPosition in huntSet.indices) {
            // convert to mutable list if huntSet is immutable
            val mutableHuntSet = huntSet.toMutableList()

            // swap the items in the list
            Collections.swap(mutableHuntSet, fromPosition, toPosition)

            // preserve the expanded state of both items during the swap
            val expandedStateFrom = expandedItems.contains(fromPosition)
            val expandedStateTo = expandedItems.contains(toPosition)

            // update the huntSet (this step is important to notify the adapter)
            huntSet = mutableHuntSet

            // update the expandedItems set based on the swapped positions
            if (expandedStateFrom) expandedItems.add(toPosition) else expandedItems.remove(toPosition)
            if (expandedStateTo) expandedItems.add(fromPosition) else expandedItems.remove(fromPosition)

            // notify the adapter about the move with animation
            notifyItemMoved(fromPosition, toPosition)
            notifyDataSetChanged()
        }

        Log.d("ShinyHuntListAdapter", "swapItems() completed")
    }

    // return the size of the dataset (invoked by the layout manager)
    override fun getItemCount() = huntSet.size

}