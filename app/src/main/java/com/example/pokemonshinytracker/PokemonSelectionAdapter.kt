package com.example.pokemonshinytracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PokemonSelectionAdapter(
    private var pokemonListItems: List<PokemonListItem>,
    private val onPokemonSelected: (Pokemon) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // constants to differentiate between Pokemon items and header items
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_POKEMON = 1
    }

    // class for Pokemon view holders
    inner class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemon_image)

        fun bind(pokemon: Pokemon) {
            pokemonImage.setImageResource(pokemon.forms.find { it.isDefaultForm }!!.formImage)
            pokemonImage.setOnClickListener { onPokemonSelected(pokemon) }
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
        Log.d("PokemonSelectionAdapter", "getItemViewType() started")

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
            is PokemonListItem.PokemonItem -> (holder as PokemonViewHolder).bind(item.pokemon)
        }
    }

    override fun getItemCount() = pokemonListItems.size


    // Function to update the Pokemon list
    fun updateList(newList: List<PokemonListItem>) {
        pokemonListItems = newList
        notifyDataSetChanged()
    }

}
