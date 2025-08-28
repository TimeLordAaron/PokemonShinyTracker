package com.example.pokemonshinytracker

/* 4 MODES */
// 0 = Individual Hunt (selecting origin game)
// 1 = Individual Hunt (selecting current game)
// 2 = Filter Menu (selecting origin games)
// 3 = Filter Menu (selecting current games)

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class GameSelectionAdapter(
    private val mode: GameSelectionMode,
    private val gameListItems: List<GameListItem>,
    private var preselectedGames: List<Int?>,
    private val onGameSelected: (Game) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val selectedGamePositions = mutableSetOf<Int>()      // stores the positions of selected Games in the list

    // constants to differentiate between game items and header items
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_GAME = 1
    }

    // class for game view holders
    inner class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val gameLayout: ConstraintLayout = view.findViewById(R.id.game_layout)
        private val gameIconBorder: FrameLayout = view.findViewById(R.id.game_icon_border)
        private val gameImage: ImageView = view.findViewById(R.id.game_image)
        private val gameName: TextView = view.findViewById(R.id.game_name)

        fun bind(game: Game) {
            // set the background and image of the game
            // origin game(s)
            if (mode == GameSelectionMode.ORIGIN_SINGLE_SELECT || mode == GameSelectionMode.ORIGIN_MULTI_SELECT) {
                gameIconBorder.setBackgroundResource(R.drawable.ui_game_icon_border_origin)
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_origin)
            }
            // current game(s)
            else {
                gameIconBorder.setBackgroundResource(R.drawable.ui_game_icon_border_current)
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_current)
            }
            // set game image and name
            gameImage.setImageResource(game.gameImage)
            gameName.text = game.gameName

            // set background of the layout if the game is currently selected
            if (preselectedGames.contains(game.gameID - 1)) {
                selectedGamePositions.add(game.gameID - 1)
                gameLayout.setBackgroundResource(R.drawable.ui_shiny_hunt_item_container_complete)     // selected game
            } else {
                gameLayout.setBackgroundResource(0)     // unselected game (reset the background)
            }

            gameImage.setOnClickListener {
                onGameSelected(game)    // return the selected game

                when (mode) {
                    // Individual Hunt modes (single-select): clear the previous selection, and add the new selection
                    GameSelectionMode.ORIGIN_SINGLE_SELECT, GameSelectionMode.CURRENT_SINGLE_SELECT -> {
                        selectedGamePositions.clear()
                        selectedGamePositions.add(game.gameID)
                    }
                    // Filter modes (multi-select): flip the selection state
                    GameSelectionMode.ORIGIN_MULTI_SELECT, GameSelectionMode.CURRENT_MULTI_SELECT -> {
                        if (selectedGamePositions.contains(game.gameID)) {
                            selectedGamePositions.remove(game.gameID)
                        } else {
                            selectedGamePositions.add(game.gameID)
                        }
                    }
                }

                notifyDataSetChanged()
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

// GameSelectionMode: specifies whether the Game selector is in single-select or multi-select mode and selecting origin or current game(s)
enum class GameSelectionMode {
    ORIGIN_SINGLE_SELECT,
    CURRENT_SINGLE_SELECT,
    ORIGIN_MULTI_SELECT,
    CURRENT_MULTI_SELECT
}