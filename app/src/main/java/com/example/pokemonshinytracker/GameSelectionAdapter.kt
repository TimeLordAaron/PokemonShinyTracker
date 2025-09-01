package com.example.pokemonshinytracker

import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
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
        private val gameItemContainer: FrameLayout = view.findViewById(R.id.game_item_container)
        private val gameImage: ImageView = view.findViewById(R.id.game_image)
        private val gameName: TextView = view.findViewById(R.id.game_name)

        fun bind(game: Game) {
            // set game image and name
            gameImage.setImageResource(game.gameImage)
            gameName.text = game.gameName

            val gamePosition = game.gameID
            var isSelected = selectedGamePositions.contains(gamePosition)

            // determine the correct resources to use for unselected/selected
            val unselectedRes: Int
            val selectedRes: Int

            if (mode == GameSelectionMode.ORIGIN_SINGLE_SELECT ||
                mode == GameSelectionMode.ORIGIN_MULTI_SELECT
            ) {
                unselectedRes = R.drawable.ui_origin_game_unselected_item_border
                selectedRes = R.drawable.ui_origin_game_selected_item_border
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_origin)  // set background of the image
            } else {
                unselectedRes = R.drawable.ui_current_game_unselected_item_border
                selectedRes = R.drawable.ui_current_game_selected_item_border
                gameImage.setBackgroundResource(R.drawable.ui_game_icon_border_current) // set background of the image
            }

            // if game is preselected and not already marked, initialize as selected
            if (preselectedGames.contains(gamePosition - 1) && !selectedGamePositions.contains(gamePosition)) {
                selectedGamePositions.add(gamePosition)
                isSelected = true
            }

            val transitionDrawable = TransitionDrawable(
                arrayOf(
                    gameItemContainer.context.getDrawable(unselectedRes),
                    gameItemContainer.context.getDrawable(selectedRes)
                )
            )
            gameItemContainer.foreground = transitionDrawable

            // jump to the correct initial state
            if (isSelected) {
                transitionDrawable.startTransition(0)   // immediately show selected state
            } else {
                transitionDrawable.resetTransition()    // show unselected state
            }

            // on click listener for the image
            gameImage.setOnClickListener {
                onGameSelected(game)    // return the selected game

                when (mode) {
                    // Individual Hunt modes (single-select): clear the previous selection, and add the new selection
                    GameSelectionMode.ORIGIN_SINGLE_SELECT, GameSelectionMode.CURRENT_SINGLE_SELECT -> {
                        selectedGamePositions.clear()
                        selectedGamePositions.add(game.gameID)
                        notifyDataSetChanged()
                    }
                    // Filter modes (multi-select): flip the selection state
                    GameSelectionMode.ORIGIN_MULTI_SELECT, GameSelectionMode.CURRENT_MULTI_SELECT -> {
                        if (selectedGamePositions.contains(gamePosition)) {
                            // deselect the game
                            selectedGamePositions.remove(gamePosition)
                            transitionDrawable.reverseTransition(MyApplication.TRANSITION_DURATION)
                        } else {
                            // select the game
                            selectedGamePositions.add(gamePosition)
                            transitionDrawable.startTransition(MyApplication.TRANSITION_DURATION)
                        }
                    }
                }
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
        return when (gameListItems[position]) {
            is GameListItem.HeaderItem -> VIEW_TYPE_HEADER
            is GameListItem.GameItem -> VIEW_TYPE_GAME
        }
    }

    // Create new Game/Header views (invoked by the layout manager)
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

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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