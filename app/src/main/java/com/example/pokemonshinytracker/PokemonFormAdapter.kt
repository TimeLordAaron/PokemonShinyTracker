package com.example.pokemonshinytracker

import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class PokemonFormAdapter(
    private val forms: List<PokemonForm>,
    private val selectedFormIds: MutableSet<Int>,
    private val onSelectedFormsChanged: () -> Unit)
    : RecyclerView.Adapter<PokemonFormAdapter.PokemonFormHolder>()
{

    class PokemonFormHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val formLayout: LinearLayout = itemView.findViewById(R.id.form_layout)
        val formImage: ImageView = itemView.findViewById(R.id.form_image)
        val formName: TextView = itemView.findViewById(R.id.form_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonFormHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pokemon_form_item, parent, false)
        return PokemonFormHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonFormHolder, position: Int) {
        val form = forms[position]
        val isSelected = selectedFormIds.contains(form.formID)

        // set up the form image and name
        holder.formImage.setImageResource(form.formImage)
        holder.formName.text = form.formName?.let { "($it)" } ?: ""

        // apply transition drawable to the foreground
        val transitionDrawable = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.ui_pokemon_item_border_transition
        ) as TransitionDrawable
        holder.formLayout.foreground = transitionDrawable

        // jump to correct initial state
        if (isSelected) {
            transitionDrawable.startTransition(0)   // show selected state immediately
        } else {
            transitionDrawable.resetTransition()    // show unselected state
        }

        // invert the background color and return the selected form
        holder.itemView.setOnClickListener {
            if (selectedFormIds.contains(form.formID)) {
                // deselect the form
                selectedFormIds.remove(form.formID)
                transitionDrawable.reverseTransition(MyApplication.TRANSITION_DURATION)
            } else {
                // select the form
                selectedFormIds.add(form.formID)
                transitionDrawable.startTransition(MyApplication.TRANSITION_DURATION)
            }
            notifyItemChanged(position) // refresh background
            onSelectedFormsChanged()
        }
    }

    override fun getItemCount(): Int = forms.size
}