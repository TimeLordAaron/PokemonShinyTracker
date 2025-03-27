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

    private val expandedItems = mutableSetOf<Int>() // Stores the positions of expanded items

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
            // Define click listener for the ViewHolder's View
            background = view.findViewById(R.id.background)
            pokemonName = view.findViewById(R.id.pokemonName)
            originGameIconBorder = view.findViewById(R.id.originGameIconBorder)
            originGameIcon = view.findViewById(R.id.originGameIcon)
            currentGameIconBorder = view.findViewById(R.id.currentGameIconBorder)
            currentGameIcon = view.findViewById(R.id.currentGameIcon)
            pokemonImage = view.findViewById(R.id.pokemonImage)
            counterValue = view.findViewById(R.id.counterValue)
            counterIncrementBtn = view.findViewById(R.id.counterIncrement)
            counterDecrementBtn = view.findViewById(R.id.counterDecrement)
            longClickMenu = view.findViewById(R.id.longClickMenu)
            moveUpButton = view.findViewById(R.id.moveUpButton)
            moveDownButton = view.findViewById(R.id.moveDownButton)
            editButton = view.findViewById(R.id.editButton)
            longClickMenuOpened = false
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("MainActivity", "Creating shiny hunt View Holder")

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.shiny_hunt_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Extract the shiny hunt from the huntSet
        val hunt = huntSet[position]

        Log.d("MainActivity", "Binding View Holder for shiny hunt: $hunt")
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

        // Ensure the longClickMenu visibility is correctly set based on the item's state
        viewHolder.longClickMenu.visibility = if (expandedItems.contains(position)) View.VISIBLE else View.GONE

        // Set the move buttons' visibility
        viewHolder.moveUpButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        viewHolder.moveDownButton.visibility = if (position == huntSet.size - 1) View.INVISIBLE else View.VISIBLE

        // Click listener for each hunt item's counter increment button
        viewHolder.counterIncrementBtn.setOnClickListener {
            Log.d("MainActivity", "Increment counter button clicked for shiny hunt: $hunt")
            // increment the counter
            hunt.counter++
            viewHolder.counterValue.text = hunt.counter.toString()

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.formID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // Click listener for each hunt item's counter decrement button
        viewHolder.counterDecrementBtn.setOnClickListener {
            Log.d("MainActivity", "Decrement counter button clicked for shiny hunt: $hunt")
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

        // Long click listener for each hunt item's layout
        viewHolder.background.setOnLongClickListener {
            if (expandedItems.contains(position)) {
                expandedItems.remove(position)
                viewHolder.longClickMenu.visibility = View.GONE
            } else {
                expandedItems.add(position)
                viewHolder.longClickMenu.visibility = View.VISIBLE
            }

            notifyDataSetChanged()

            true
        }

        viewHolder.moveUpButton.setOnClickListener {
            if (position > 0) { // Ensure it's not the first item
                swapItems(position, position - 1)
            }
        }

        viewHolder.moveDownButton.setOnClickListener {
            if (position < huntSet.size - 1) { // Ensure it's not the last item
                swapItems(position, position + 1)
            }
        }

        viewHolder.editButton.setOnClickListener {
            // switch to the detailed view of the shiny hunt
            val intent = Intent(context, IndividualHunt::class.java).apply {
                putExtra("hunt_id", hunt.huntID)
            }
            Log.d("MainActivity", "Created intent for selected hunt. Switching to Individual Hunt window")
            context.startActivity(intent)
        }

    }

    private fun swapItems(fromPosition: Int, toPosition: Int) {
        // Ensure valid position for swapping
        if (fromPosition in huntSet.indices && toPosition in huntSet.indices) {
            // Convert to mutable list if huntSet is immutable
            val mutableHuntSet = huntSet.toMutableList()

            // Swap the items in the list
            Collections.swap(mutableHuntSet, fromPosition, toPosition)

            // Preserve the expanded state of both items during the swap
            val expandedStateFrom = expandedItems.contains(fromPosition)
            val expandedStateTo = expandedItems.contains(toPosition)

            // Update the huntSet (this step is important to notify the adapter)
            huntSet = mutableHuntSet

            // Update the expandedItems set based on the swapped positions
            if (expandedStateFrom) expandedItems.add(toPosition) else expandedItems.remove(toPosition)
            if (expandedStateTo) expandedItems.add(fromPosition) else expandedItems.remove(fromPosition)

            // Notify the adapter about the move with animation
            notifyItemMoved(fromPosition, toPosition)
            notifyDataSetChanged()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = huntSet.size

}