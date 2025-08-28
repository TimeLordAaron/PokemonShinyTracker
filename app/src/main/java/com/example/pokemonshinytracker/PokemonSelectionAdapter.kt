package com.example.pokemonshinytracker

import android.graphics.drawable.TransitionDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PokemonSelectionAdapter(
    private val mode: PokemonSelectionMode,
    private var pokemonListItems: List<PokemonListItem>,
    private var preselectedPokemon: List<Pokemon>,
    private val onPokemonSelected: (Pokemon) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val selectedPokemonPositions = mutableSetOf<Int>()      // stores the positions of selected Pokemon in the list

    // constants to differentiate between Pokemon items and header items
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_POKEMON = 1
    }

    // class for Pokemon view holders
    inner class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val pokemonItemContainer: FrameLayout = view.findViewById(R.id.pokemon_item_container)
        private val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)
        private val transitionDrawable = ContextCompat.getDrawable(
            itemView.context,
            R.drawable.ui_pokemon_item_border_transition
        ) as TransitionDrawable

        // apply transition drawable as foreground
        init {
            pokemonItemContainer.foreground = transitionDrawable
        }

        fun bind(pokemon: Pokemon, position: Int) {
            pokemonImage.load(pokemon.forms.find { it.isDefaultForm }!!.formImage)

            // determine the initial selection state
            val isSelected = selectedPokemonPositions.contains(position) ||
                    preselectedPokemon.any { it.pokemonID == pokemon.pokemonID }

            if (isSelected) selectedPokemonPositions.add(position)

            // jump to the correct initial state
            if (isSelected) {
                transitionDrawable.startTransition(0)   // immediately show selected state
            } else {
                transitionDrawable.resetTransition()    // show unselected state
            }

            // on click listener for the image
            pokemonImage.setOnClickListener {
                // in filter (multi-select) mode, invert the foreground of the frame layout
                if (mode == PokemonSelectionMode.MULTI_SELECT) {
                    if (selectedPokemonPositions.contains(position)) {
                        // deselect the pokemon
                        selectedPokemonPositions.remove(position)
                        transitionDrawable.reverseTransition(250)
                    } else {
                        // select the pokemon
                        selectedPokemonPositions.add(position)
                        transitionDrawable.startTransition(250)
                    }
                }

                // return the clicked pokemon
                onPokemonSelected(pokemon)
            }
        }
    }

    // class for header view holders
    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.generation_header_text)

        fun bind(header: String) {
            headerText.text = header
        }
    }

    // Get the type of the view (invoked by the layout manager)
    override fun getItemViewType(position: Int): Int {
        //Log.d("PokemonSelectionAdapter", "getItemViewType() started")

        return when (pokemonListItems[position]) {
            is PokemonListItem.HeaderItem -> VIEW_TYPE_HEADER
            is PokemonListItem.PokemonItem -> VIEW_TYPE_POKEMON
        }
    }

    // Create new Pokemon/Header views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("PokemonSelectionAdapter", "onCreateViewHolder() started")

        return if (viewType == VIEW_TYPE_HEADER) {
            Log.d("PokemonSelectionAdapter", "View Type: Header")
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.pokemon_header_item, parent, false)
            HeaderViewHolder(view)
        } else {
            Log.d("PokemonSelectionAdapter", "View Type: Pokemon")
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.pokemon_item, parent, false)
            PokemonViewHolder(view)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("PokemonSelectionAdapter", "onBindViewHolder() started")

        when (val item = pokemonListItems[position]) {
            is PokemonListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.generation)
            is PokemonListItem.PokemonItem -> {
                (holder as PokemonViewHolder).bind(item.pokemon, position)
            }
        }
    }

    override fun getItemCount() = pokemonListItems.size

    override fun getItemId(position: Int): Long {
        return when (val item = pokemonListItems[position]) {
            is PokemonListItem.PokemonItem -> item.pokemon.pokemonID.toLong()
            is PokemonListItem.HeaderItem -> -position.toLong() // headers can use negative unique values
        }
    }

    // Function to update the Pokemon list
    fun updateList(newList: List<PokemonListItem>) {
        pokemonListItems = newList
        selectedPokemonPositions.clear()

        newList.forEachIndexed { index, item ->
            if (item is PokemonListItem.PokemonItem && preselectedPokemon.contains(item.pokemon)) {
                selectedPokemonPositions.add(index)
            }
        }

        notifyDataSetChanged()
    }

    fun updateSelectedPokemon(selected: List<Pokemon>) {
        preselectedPokemon = selected
        selectedPokemonPositions.clear()
        pokemonListItems.forEachIndexed { index, item ->
            if (item is PokemonListItem.PokemonItem && preselectedPokemon.any { it.pokemonID == item.pokemon.pokemonID }) {
                selectedPokemonPositions.add(index)
            }
        }

        notifyDataSetChanged()

    }

}

// PokemonSelectionMode: specifies whether the Pokemon selector is in single-select or multi-select mode
enum class PokemonSelectionMode {
    SINGLE_SELECT,
    MULTI_SELECT
}