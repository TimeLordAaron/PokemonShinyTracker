package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlin.random.Random

data class ShinyHunt(val huntID: Int, var formID: Int?, var originGameID: Int?, var method: String, var startDate: String?,
                     var counter: Int, var phase: Int, var isComplete: Boolean, var finishDate: String?, var currentGameID: Int?, var defaultPosition: Int?)

object ShinyHuntData {

    // Function to insert mock shiny hunts into the database
    fun insertShinyHuntData(db: SQLiteDatabase, SHINY_HUNT_TABLE: String, HUNT_ID_COL: String, FORM_ID_COL: String, ORIGIN_GAME_ID_COL: String,
                            METHOD_COL: String, START_DATE_COL: String, COUNTER_COL: String, PHASE_COL: String,
                            IS_COMPLETE_COL: String, FINISH_DATE_COL: String, CURRENT_GAME_ID_COL: String, DEFAULT_POSITION_COL: String) {
        Log.d("ShinyHuntModel", "insertShinyHuntData() started")

        val shinyHunts = mutableListOf<List<Any?>>()

        /*
        // Mock Data Set #1: One hunt for every pokemon form
        for (formID in 1199 downTo 1) {
            val originGame = Random.nextInt(1, 21)
            val counter = Random.nextInt(0, 10000)
            if (formID > 600) {
                val currentGame = Random.nextInt(22, 38)
                shinyHunts.add(
                    listOf(
                        formID,
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
                        formID,
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
        */

        // Mock Data Set #2: Some of my personal shiny hunts
        shinyHunts.add(listOf(740, 33, "Soft Resets", "9/20/2024", 3111, 0, 1, "1/6/2025", 37)) // Darkrai
        shinyHunts.add(listOf(341, 37, "Masuda Method", "", 319, 0, 1, "1/17/2025", 37))        // Gligar
        shinyHunts.add(listOf(734, 28, "Soft Resets", "", 1768, 0, 1, "", 38))                  // Regigigas
        shinyHunts.add(listOf(733, 27, "Soft Resets", "", 1381, 0, 1, "", 38))                  // Heatran
        shinyHunts.add(listOf(233, 28, "Soft Resets", "", 559, 0, 1, "", 38))                   // Zapdos
        shinyHunts.add(listOf(738, 37, "Masuda Method", "", 296, 0, 1, "", 37))                 // Phione
        shinyHunts.add(listOf(932, 27, "Soft Resets", "", 952, 0, 1, "", 38))                   // Tornadus
        shinyHunts.add(listOf(235, 28, "Soft Resets", "", 493, 0, 1, "", 38))                   // Moltres
        shinyHunts.add(listOf(934, 28, "Soft Resets", "", 4, 0, 1, "", 38))                     // Thundurus
        shinyHunts.add(listOf(231, 27, "Soft Resets", "", 1704, 0, 1, "", 38))                  // Articuno
        shinyHunts.add(listOf(938, 28, "Soft Resets", "", 2058, 0, 1, "", 38))                  // Landorus (Ultra Moon)
        shinyHunts.add(listOf(938, 27, "Soft Resets", "", 563, 0, 1, "", 38))                   // Landorus (Ultra Sun)
        shinyHunts.add(listOf(737, 28, "Soft Resets", "", 835, 0, 1, "", 38))                   // Cresselia
        shinyHunts.add(listOf(937, 28, "Soft Resets", "", 1414, 0, 1, "", 28))                  // Zekrom
        shinyHunts.add(listOf(729, 27, "Soft Resets", "", 2015, 0, 0, "", null))                // Dialga (ongoing)

        // insert each shiny hunt into the database
        for (hunt in shinyHunts) {
            val values = ContentValues().apply {
                put(FORM_ID_COL, hunt[0] as Int)
                put(ORIGIN_GAME_ID_COL, hunt[1] as Int?)
                put(METHOD_COL, hunt[2] as String)
                put(START_DATE_COL, hunt[3] as String)
                put(COUNTER_COL, hunt[4] as Int)
                put(PHASE_COL, hunt[5] as Int)
                put(IS_COMPLETE_COL, hunt[6] as Int)
                put(FINISH_DATE_COL, hunt[7] as String?)
                put(CURRENT_GAME_ID_COL, hunt[8] as Int?)
            }
            val newHuntID = db.insert(SHINY_HUNT_TABLE, null, values)
            if (newHuntID == -1L) {
                Log.e("ShinyHuntModel", "Error inserting shiny hunt into the database: $hunt")
            } else {
                Log.d("ShinyHuntModel", "Shiny hunt inserted into the database: $hunt")
                // use the returned huntID to set the shiny hunt's defaultPosition
                val updateValues = ContentValues().apply {
                    put(DEFAULT_POSITION_COL, newHuntID)
                }
                val result = db.update(SHINY_HUNT_TABLE, updateValues, "$HUNT_ID_COL = ?", arrayOf(newHuntID.toString()))
                if (result == 0) {
                    Log.e("ShinyHuntModel", "Error setting $DEFAULT_POSITION_COL of shiny hunt: $hunt")
                } else {
                    Log.d("ShinyHuntModel", "Set defaultPosition '$newHuntID' of shiny hunt: $hunt")
                }
            }
        }

        Log.d("ShinyHuntData", "insertShinyHuntData() completed")
    }
}
