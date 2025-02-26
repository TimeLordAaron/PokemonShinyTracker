package com.example.pokemonshinytracker

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PokemonSelectionAdapter(
    private val context: Context,
    private val pokemonListItems: List<PokemonListItem>,
    private val onPokemonSelected: (Pokemon) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_POKEMON = 1
    }

    inner class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonImage: ImageView = view.findViewById(R.id.pokemonImage)

        fun bind(pokemon: Pokemon) {
            pokemonImage.setImageResource(pokemon.pokemonImage)
            pokemonImage.setOnClickListener { onPokemonSelected(pokemon) }
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.headerText)

        fun bind(header: String) {
            headerText.text = header
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (pokemonListItems[position]) {
            is PokemonListItem.HeaderItem -> VIEW_TYPE_HEADER
            is PokemonListItem.PokemonItem -> VIEW_TYPE_POKEMON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.pokemon_header_item, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.pokemon_item, parent, false)
            PokemonViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = pokemonListItems[position]) {
            is PokemonListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.generation)
            is PokemonListItem.PokemonItem -> (holder as PokemonViewHolder).bind(item.pokemon)
        }
    }

    override fun getItemCount() = pokemonListItems.size
}
