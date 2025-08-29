package com.example.pokemonshinytracker

import android.app.Application

class MyApplication : Application() {

    // global variables / constants
    companion object {
        const val TRANSITION_DURATION: Int = 200   // duration of cross-fade animations
    }
    var counterMultiplier: Int = 1      // multiplier for all counters in MainActivity (default is 1)

}