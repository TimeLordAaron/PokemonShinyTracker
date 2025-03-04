package com.example.pokemonshinytracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameSelectionAdapter(
    private val context: Context,
    private val gameListItems: List<GameListItem>,
    private val onGameSelected: (Game) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_GAME = 1
    }

    inner class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gameImage: ImageView = view.findViewById(R.id.gameImage)
        val gameName: TextView = view.findViewById(R.id.gameName)

        fun bind(game: Game) {
            gameImage.setImageResource(game.gameImage)
            gameImage.setOnClickListener { onGameSelected(game) }
            gameName.text = game.gameName
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerText: TextView = view.findViewById(R.id.headerText)

        fun bind(header: String) {
            headerText.text = header
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (gameListItems[position]) {
            is GameListItem.HeaderItem -> VIEW_TYPE_HEADER
            is GameListItem.GameItem -> VIEW_TYPE_GAME
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.game_header_item, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.game_item, parent, false)
            GameViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = gameListItems[position]) {
            is GameListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.generation)
            is GameListItem.GameItem -> (holder as GameViewHolder).bind(item.game)
        }
    }

    override fun getItemCount() = gameListItems.size
}