package com.example.pokemonshinytracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SelectedPokemonAdapter(
    private var selectedPokemon: MutableList<Pokemon>,
    private var selectedPokemonForms: MutableSet<Int>,
    private val onPokemonUnselected: (Pokemon) -> Unit,
    private val onSelectionChanged: () -> Unit)
    : RecyclerView.Adapter<SelectedPokemonAdapter.SelectedPokemonHolder>()
{

    class SelectedPokemonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pokemonName: TextView = itemView.findViewById(R.id.pokemon_name)
        val selectAll: CheckBox = itemView.findViewById(R.id.select_all_checkbox)
        val dropdown: ImageButton = itemView.findViewById(R.id.dropdown_button)
        val unselect: ImageButton = itemView.findViewById(R.id.unselect_pokemon_button)
        val pokemonFormRecyclerView: RecyclerView = itemView.findViewById(R.id.pokemon_forms_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedPokemonHolder {
        Log.d("SelectedPokemonAdapter", "onCreateViewHolder() started")

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_pokemon_item, parent, false)
        return SelectedPokemonHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedPokemonHolder, position: Int) {
        Log.d("SelectedPokemonAdapter", "onBindViewHolder() started")

        val pokemon = selectedPokemon[position]

        // set up the pokemon name
        holder.pokemonName.text = pokemon.pokemonName

        // get the pokemon's forms
        val forms = pokemon.forms.sortedBy { it.formID }

        // set up the pokemon forms recycler view
        holder.pokemonFormRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.pokemonFormRecyclerView.adapter = PokemonFormAdapter(
            pokemon.forms.sortedBy { it.formID },
            selectedPokemonForms,
            { selectedForm ->
                Log.d("SelectedPokemonAdapter", "Clicked form: ${selectedForm.formName}")
            },
            onSelectedFormsChanged =  {
                toggleSelectAll(holder, position)
            }
        )

        // toggle visibility of the pokemon forms recycler view
        var isExpanded = true
        holder.dropdown.setOnClickListener {
            isExpanded = !isExpanded
            holder.pokemonFormRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.dropdown.setImageResource(
                if (isExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            )
        }

        // select all forms
        holder.selectAll.setOnCheckedChangeListener(null) // prevent recursion
        holder.selectAll.isChecked = forms.all { selectedPokemonForms.contains(it.formID) }
        holder.selectAll.setOnClickListener {
            val formIDs = pokemon.forms.map { it.formID }

            if (holder.selectAll.isChecked) {
                selectedPokemonForms.addAll(formIDs)
            } else {
                selectedPokemonForms.removeAll(formIDs)
            }

            holder.pokemonFormRecyclerView.adapter?.notifyDataSetChanged()

            onSelectionChanged()
        }


        // on click listener for the unselect pokemon button
        holder.unselect.setOnClickListener {
            val removed = selectedPokemon.removeAt(position)
            removed.forms.forEach { selectedPokemonForms.remove(it.formID) }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, selectedPokemon.size)
            onPokemonUnselected(removed) // notify the outer list
        }

    }

    override fun getItemCount(): Int = selectedPokemon.size

    fun updateList(newList: MutableList<Pokemon>) {
        selectedPokemon = newList
        notifyDataSetChanged()
    }

    // Helper function to toggle the select all checkbox of a selected Pokemon
    fun toggleSelectAll(holder: SelectedPokemonHolder, position: Int) {
        Log.d("SelectedPokemonAdapter", "toggleSelectAll() started")

        // check the select all checkbox if all of a selected Pokemon's forms are currently selected
        holder.selectAll.isChecked = selectedPokemon[position].forms
            .all { selectedPokemonForms.contains(it.formID) }

        onSelectionChanged()

        Log.d("SelectedPokemonAdapter", "toggleSelectAll() completed")
    }

}