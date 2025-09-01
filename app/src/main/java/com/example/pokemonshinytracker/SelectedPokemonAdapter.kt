package com.example.pokemonshinytracker

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

    // track the selected pokemon items that are currently expanded
    private val expandedPokemon = mutableMapOf<Int, Boolean>()  // key = pokemonID

    class SelectedPokemonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pokemonName: TextView = itemView.findViewById(R.id.pokemon_name)
        val selectedFormCount: TextView = itemView.findViewById(R.id.selected_form_count)
        val selectAll: CheckBox = itemView.findViewById(R.id.select_all_checkbox)
        val dropdown: ImageButton = itemView.findViewById(R.id.dropdown_button)
        val unselect: ImageButton = itemView.findViewById(R.id.unselect_pokemon_button)
        val pokemonFormRecyclerView: RecyclerView = itemView.findViewById(R.id.pokemon_forms_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedPokemonHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_pokemon_item, parent, false)
        return SelectedPokemonHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedPokemonHolder, position: Int) {
        val pokemon = selectedPokemon[position]

        // set up pokemon name
        holder.pokemonName.text = pokemon.pokemonName

        // track expansion state
        val isExpanded = expandedPokemon[pokemon.pokemonID] ?: false
        holder.pokemonFormRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.dropdown.setImageResource(
            if (isExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
        )

        // initialize selected form count
        val selectedFormsForThisPokemon = pokemon.forms.count { selectedPokemonForms.contains(it.formID) }
        val totalPokemonFormsCount = pokemon.forms.count()
        holder.selectedFormCount.text = "$selectedFormsForThisPokemon/$totalPokemonFormsCount forms selected."

        // set up pokemon forms recycler view
        val forms = pokemon.forms.sortedBy { it.formID }
        holder.pokemonFormRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.pokemonFormRecyclerView.adapter = PokemonFormAdapter(
            forms,
            selectedPokemonForms,
            onSelectedFormsChanged =  {
                toggleSelectAll(holder, position)
                updateSelectedFormCount(holder, pokemon)
            }
        )

        // on click listener for select all
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

            updateSelectedFormCount(holder, pokemon)
        }

        // on click listener for dropdown
        holder.dropdown.setOnClickListener {
            val currentlyExpanded = expandedPokemon[pokemon.pokemonID] ?: false
            val newExpanded = !currentlyExpanded
            expandedPokemon[pokemon.pokemonID] = newExpanded
            holder.pokemonFormRecyclerView.visibility = if (newExpanded) View.VISIBLE else View.GONE
            holder.dropdown.setImageResource(
                if (newExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
            )
        }

        // on click listener for unselect
        holder.unselect.setOnClickListener {
            unselectPokemon(selectedPokemon[position])
        }

    }

    override fun getItemCount(): Int = selectedPokemon.size

    fun updateList(newList: MutableList<Pokemon>) {
        selectedPokemon = newList
        notifyDataSetChanged()
    }

    // Helper function to update the selected form count text
    private fun updateSelectedFormCount(holder: SelectedPokemonHolder, pokemon: Pokemon) {
        val selectedFormsForThisPokemon = pokemon.forms.count { selectedPokemonForms.contains(it.formID) }
        val totalForms = pokemon.forms.size
        holder.selectedFormCount.text = "$selectedFormsForThisPokemon/$totalForms forms selected."
    }

    // Helper function to unselect a Pokemon
    fun unselectPokemon(pokemon: Pokemon) {
        val position = selectedPokemon.indexOfFirst { it.pokemonID == pokemon.pokemonID }
        if (position != -1) {
            selectedPokemon.removeAt(position)
            expandedPokemon.remove(pokemon.pokemonID)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, selectedPokemon.size)
            onPokemonUnselected(pokemon)
        }
    }

    // Helper function to toggle the select all checkbox of a selected Pokemon
    fun toggleSelectAll(holder: SelectedPokemonHolder, position: Int) {
        // check the select all checkbox if all of a selected Pokemon's forms are currently selected
        holder.selectAll.isChecked = selectedPokemon[position].forms
            .all { selectedPokemonForms.contains(it.formID) }

        onSelectionChanged()
    }

}