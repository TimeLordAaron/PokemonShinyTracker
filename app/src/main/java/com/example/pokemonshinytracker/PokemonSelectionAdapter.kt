package com.example.pokemonshinytracker

import android.graphics.drawable.TransitionDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
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
        private val pokemonItemContainer: ConstraintLayout = view.findViewById(R.id.pokemon_item_container)
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
            // load image with coil
            val defaultForm = pokemon.forms.find { it.isDefaultForm }!!
            pokemonImage.load(defaultForm.formImage) {
                placeholder(R.drawable.etc_default)
                crossfade(true)
                size(pokemonImage.width.takeIf { it > 0 } ?: 128)   // approximate size if not measured yet
                transformations(
                    ShadowTransformation(
                        shadowRadius = MyApplication.SHADOW_RADIUS,
                        dx = MyApplication.SHADOW_OFFSET,
                        dy = MyApplication.SHADOW_OFFSET,
                    )
                )
            }

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
                        transitionDrawable.reverseTransition(MyApplication.TRANSITION_DURATION)
                    } else {
                        // select the pokemon
                        selectedPokemonPositions.add(position)
                        transitionDrawable.startTransition(MyApplication.TRANSITION_DURATION)
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
            is PokemonListItem.PokemonItem -> (holder as PokemonViewHolder).bind(item.pokemon, position)
        }
    }

    override fun getItemCount() = pokemonListItems.size

    override fun getItemId(position: Int): Long {
        return when (val item = pokemonListItems[position]) {
            is PokemonListItem.PokemonItem -> item.pokemon.pokemonID.toLong()
            is PokemonListItem.HeaderItem -> item.generation.hashCode().toLong()
        }
    }

    // Function to update the Pokemon list
    fun updateList(newList: List<PokemonListItem>) {
        val oldList = pokemonListItems
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]

                return when {
                    oldItem is PokemonListItem.PokemonItem && newItem is PokemonListItem.PokemonItem ->
                        oldItem.pokemon.pokemonID == newItem.pokemon.pokemonID
                    oldItem is PokemonListItem.HeaderItem && newItem is PokemonListItem.HeaderItem ->
                        oldItem.generation == newItem.generation
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem == newItem
            }
        })

        pokemonListItems = newList
        selectedPokemonPositions.clear()

        newList.forEachIndexed { index, item ->
            if (item is PokemonListItem.PokemonItem && preselectedPokemon.contains(item.pokemon)) {
                selectedPokemonPositions.add(index)
            }
        }

        diffResult.dispatchUpdatesTo(this)
    }

    fun updateSelectedPokemon(selected: List<Pokemon>) {
        preselectedPokemon = selected
        val oldSelected = selectedPokemonPositions.toSet()
        selectedPokemonPositions.clear()

        pokemonListItems.forEachIndexed { index, item ->
            if (item is PokemonListItem.PokemonItem && preselectedPokemon.any { it.pokemonID == item.pokemon.pokemonID }) {
                selectedPokemonPositions.add(index)
            }
        }

        // determine the positions that have changed
        val changed = (oldSelected union selectedPokemonPositions) subtract (oldSelected intersect selectedPokemonPositions)

        // only update those positions
        changed.forEach { pos -> notifyItemChanged(pos) }
    }

    fun getPositionOfPokemon(pokemonID: Int?): Int? {
        if (pokemonID == null) return null
        return pokemonListItems.indexOfFirst { item ->
            item is PokemonListItem.PokemonItem && item.pokemon.pokemonID == pokemonID
        }.takeIf { it != -1 }
    }

}

// PokemonSelectionMode: specifies whether the Pokemon selector is in single-select or multi-select mode
enum class PokemonSelectionMode {
    SINGLE_SELECT,
    MULTI_SELECT
}