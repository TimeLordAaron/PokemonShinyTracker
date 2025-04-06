package com.example.pokemonshinytracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameSelectionAdapter(
    private val mode: Int,
    private val gameListItems: List<GameListItem>,
    private val onGameSelected: (Game) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // constants to differentiate between game items and header items
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_GAME = 1
    }

    // class for game view holders
    inner class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val gameIconBorder: FrameLayout = view.findViewById(R.id.game_icon_border)
        private val gameImage: ImageView = view.findViewById(R.id.game_image)
        private val gameName: TextView = view.findViewById(R.id.game_name)

        fun bind(game: Game) {
            // mode 0 = selecting origin game
            if (mode == 0) {
                gameIconBorder.setBackgroundResource(R.drawable.ui_game_icon_border_origin)
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_origin)
            }
            // mode 1 = selecting current game
            else {
                gameIconBorder.setBackgroundResource(R.drawable.ui_game_icon_border_current)
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_current)
            }
            gameImage.setImageResource(game.gameImage)
            gameImage.setOnClickListener { onGameSelected(game) }
            gameName.text = game.gameName
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
        Log.d("GameSelectionAdapter", "getItemViewType() started")

        return when (gameListItems[position]) {
            is GameListItem.HeaderItem -> VIEW_TYPE_HEADER
            is GameListItem.GameItem -> VIEW_TYPE_GAME
        }
    }

    // Create new Game/Header views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("GameSelectionAdapter", "onCreateViewHolder() started")

        return if (viewType == VIEW_TYPE_HEADER) {
            Log.d("GameSelectionAdapter", "View Type: Header")
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.game_header_item, parent, false)
            HeaderViewHolder(view)
        } else {
            Log.d("GameSelectionAdapter", "View Type: Game")
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.game_item, parent, false)
            GameViewHolder(view)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("GameSelectionAdapter", "onBindViewHolder() started")

        when (val item = gameListItems[position]) {
            is GameListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item.generation)
            is GameListItem.GameItem -> (holder as GameViewHolder).bind(item.game)
        }
    }

    override fun getItemCount() = gameListItems.size
}