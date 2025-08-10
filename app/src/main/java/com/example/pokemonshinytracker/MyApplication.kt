package com.example.pokemonshinytracker

import android.app.Application

class MyApplication : Application() {

    // global constants
    companion object {
        const val POKEMON_SPAN_LANDSCAPE = 8    // span for pokemon recycler view - landscape mode
        const val POKEMON_SPAN_PORTRAIT = 5     // span for pokemon recycler view - portrait mode
        const val GAME_SPAN_LANDSCAPE = 5       // span for game recycler view - landscape mode
        const val GAME_SPAN_PORTRAIT = 3        // span for game recycler view - portrait mode
        const val SHINY_HUNT_SPAN_LANDSCAPE = 2 // span for shiny hunt recycler view - landscape mode
        const val SHINY_HUNT_SPAN_PORTRAIT = 1  // span for shiny hunt recycler view - portrait mode
    }

    // global variables
    var counterMultiplier: Int = 1      // multiplier for all counters in MainActivity (default is 1)

}