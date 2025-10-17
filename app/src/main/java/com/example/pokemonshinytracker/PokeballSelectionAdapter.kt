package com.example.pokemonshinytracker

import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PokeballSelectionAdapter(
    private val mode: PokeballSelectionMode,
    private val pokeballListItems: List<Pokeball>,
    private var preselectedPokeballs: List<Int?>,
    private val onPokeballSelected: (Pokeball) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedPokeballPositions = mutableSetOf<Int>()      // stores the positions of selected Pokeballs in the list

    // class for pokeball view holders
    inner class PokeballViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val pokeballItemContainer: FrameLayout = view.findViewById(R.id.pokeball_item_container)
        private val pokeballImage: ImageView = view.findViewById(R.id.pokeball_image)
        private val pokeballName: TextView = view.findViewById(R.id.pokeball_name)
        private val transitionDrawable = ContextCompat.getDrawable(
            itemView.context,
            R.drawable.ui_pokeball_item_border_transition
        ) as TransitionDrawable

        // apply transition drawable as foreground
        init {
            pokeballItemContainer.foreground = transitionDrawable
        }

        fun bind(pokeball: Pokeball, position: Int) {
            // set pokeball image and name
            pokeballImage.setImageResource(pokeball.pokeballImage)
            pokeballName.text = pokeball.pokeballName

            // determine the initial selection state
            val pokeballPosition = pokeball.pokeballID
            var isSelected = selectedPokeballPositions.contains(pokeballPosition)

            // if pokeball is preselected and not already marked, initialize as selected
            if (preselectedPokeballs.contains(pokeballPosition) && !selectedPokeballPositions.contains(pokeballPosition)) {
                selectedPokeballPositions.add(pokeballPosition)
                isSelected = true
            }

            // jump to the correct initial state
            if (isSelected) {
                transitionDrawable.startTransition(0)   // immediately show selected state
            } else {
                transitionDrawable.resetTransition()    // show unselected state
            }

            // on click listener for the image
            pokeballImage.setOnClickListener {
                // in filter (multi-select) mode, invert the foreground of the frame layout
                if (mode == PokeballSelectionMode.MULTI_SELECT) {
                    if (selectedPokeballPositions.contains(position)) {
                        // deselect the pokeball
                        selectedPokeballPositions.remove(position)
                        transitionDrawable.reverseTransition(MyApplication.TRANSITION_DURATION)
                    } else {
                        // select the pokeball
                        selectedPokeballPositions.add(position)
                        transitionDrawable.startTransition(MyApplication.TRANSITION_DURATION)
                    }
                }

                // return the clicked pokeball
                onPokeballSelected(pokeball)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pokeball_item, parent, false)
        return PokeballViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PokeballViewHolder).bind(pokeballListItems[position], position)
    }

    override fun getItemCount() = pokeballListItems.size
}

// PokeballSelectionMode: specifies whether the Pokeball selector is in single-select or multi-select mode
enum class PokeballSelectionMode {
    SINGLE_SELECT,
    MULTI_SELECT
}