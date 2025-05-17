package com.example.pokemonshinytracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PokemonFormAdapter(
    private val forms: List<PokemonForm>,
    private val selectedFormIds: MutableSet<Int>,
    private val onFormSelected: (PokemonForm) -> Unit,
    private val onSelectionChanged: () -> Unit)
    : RecyclerView.Adapter<PokemonFormAdapter.PokemonFormHolder>()
{
    class PokemonFormHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formLayout: LinearLayout = itemView.findViewById(R.id.form_layout)
        val formImage: ImageView = itemView.findViewById(R.id.form_image)
        val formName: TextView = itemView.findViewById(R.id.form_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonFormHolder {
        Log.d("PokemonFormAdapter", "onCreateViewHolder() started")

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pokemon_form_item, parent, false)
        return PokemonFormHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonFormHolder, position: Int) {
        Log.d("PokemonFormAdapter", "onBindViewHolder() started")

        val form = forms[position]

        val isSelected = selectedFormIds.contains(form.formID)

        // set up the form image and name
        holder.formLayout.setBackgroundResource(
            if (isSelected) R.drawable.ui_container_complete_hunt
            else R.drawable.ui_container_transparent
        )
        holder.formImage.setImageResource(form.formImage)
        holder.formName.text = form.formName?.let { "($it)" } ?: ""

        // invert the background color and return the selected form
        holder.itemView.setOnClickListener {
            if (isSelected) {
                selectedFormIds.remove(form.formID)
            } else {
                selectedFormIds.add(form.formID)
            }
            notifyItemChanged(position) // refresh background
            onFormSelected(form)
            onSelectionChanged()
        }
    }

    override fun getItemCount(): Int = forms.size
}