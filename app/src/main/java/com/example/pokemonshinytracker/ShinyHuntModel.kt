package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlin.random.Random

data class ShinyHunt(val huntID: Int, var pokemonID: Int, var originGameID: Int?, var method: String, var startDate: String?,
                     var counter: Int, var phase: Int, var isComplete: Boolean, var finishDate: String?, var currentGameID: Int?)

object ShinyHuntData {
    fun insertShinyHuntData(db: SQLiteDatabase, SHINY_HUNT_TABLE: String, POKEMON_ID_COL: String, ORIGIN_GAME_ID_COL: String,
                            METHOD_COL: String, START_DATE_COL: String, COUNTER_COL: String, PHASE_COL: String,
                            IS_COMPLETE_COL: String, FINISH_DATE_COL: String, CURRENT_GAME_ID_COL: String) {
        val shinyHunts = mutableListOf<List<Any?>>()

        // Loop from 648 down to 0, adding each shiny hunt entry
        for (pokemonID in 648 downTo 0) {
            val originGame = Random.nextInt(1, 21)
            val counter = Random.nextInt(0, 10000)
            if (pokemonID > 492) {
                val currentGame = Random.nextInt(22, 38)
                shinyHunts.add(
                    listOf(
                        pokemonID,
                        originGame,
                        "Random Encounter",
                        "4/20/2024",
                        counter,
                        0,
                        1,
                        null,
                        currentGame
                    )
                )
            } else {
                shinyHunts.add(
                    listOf(
                        pokemonID,
                        originGame,
                        "Random Encounter",
                        "4/20/2024",
                        counter,
                        0,
                        0,
                        null,
                        null
                    )
                )
            }
        }

        // Insert each shiny hunt into the database
        for (hunt in shinyHunts) {
            Log.d("ShinyHuntData", "currentGameID: ${hunt[8]}")
            val values = ContentValues().apply {
                put(POKEMON_ID_COL, hunt[0] as Int)
                put(ORIGIN_GAME_ID_COL, hunt[1] as Int?)
                put(METHOD_COL, hunt[2] as String)
                put(START_DATE_COL, hunt[3] as String)
                put(COUNTER_COL, hunt[4] as Int)
                put(PHASE_COL, hunt[5] as Int)
                put(IS_COMPLETE_COL, hunt[6] as Int)
                put(FINISH_DATE_COL, hunt[7] as String?)
                put(CURRENT_GAME_ID_COL, hunt[8] as Int?)
            }
            db.insert(SHINY_HUNT_TABLE, null, values)
        }
        Log.d("ShinyHuntData", "Shiny hunts inserted into the database")
    }
}
