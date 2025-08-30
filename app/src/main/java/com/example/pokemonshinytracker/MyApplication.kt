package com.example.pokemonshinytracker

import android.app.Application

class MyApplication : Application() {

    // global variables / constants
    companion object {
        const val TRANSITION_DURATION: Int = 200        // duration of cross-fade animations
        const val COUNTER_MULTIPLIER_MAX: Int = 999     // max value for the counter multiplier
        const val COUNTER_MAX: Int = 99999              // max value for the counter
        const val PHASE_MAX: Int = 99999                // max value for the phase

        var counterMultiplier: Int = 1                  // global counter multiplier (default value of 1)
    }


}