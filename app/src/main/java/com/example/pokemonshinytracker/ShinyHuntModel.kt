package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlin.random.Random

data class ShinyHunt(val huntID: Int, var formID: Int?, var originGameID: Int?, var method: String, var startDate: String?, var counter: Int,
                     var phase: Int, var notes: String, var isComplete: Boolean, var finishDate: String?, var currentGameID: Int?, var defaultPosition: Int?, var pokemonName: String)

object ShinyHuntData {

    // Function to insert mock shiny hunts into the database
    fun insertShinyHuntData(db: SQLiteDatabase) {
        Log.d("ShinyHuntModel", "insertShinyHuntData() started")

        val shinyHunts = mutableListOf<List<Any?>>()


        // Mock Data Set #1: One hunt for every pokemon form
        for (formID in 1470 downTo 1) {
            val originGame = Random.nextInt(0, 21)
            val counter = Random.nextInt(0, 10000)
            val phase = Random.nextInt(0, 20)
            val startYear = Random.nextInt(2020, 2025)
            val startMonth = Random.nextInt(1, 12)
            val startDay = Random.nextInt(1, 28)
            val finishYear = Random.nextInt(2020, 2025)
            val finishMonth = Random.nextInt(1, 12)
            val finishDay = Random.nextInt(1, 28)
            if (formID > 600) {
                val currentGame = Random.nextInt(22, 38)
                shinyHunts.add(
                    listOf(
                        formID,
                        originGame,
                        "Random Encounter",
                        String.format("%04d-%02d-%02d", startYear, startMonth, startDay),
                        counter,
                        phase,
                        "",
                        1,
                        String.format("%04d-%02d-%02d", finishYear, finishMonth, finishDay),
                        currentGame
                    )
                )
            } else {
                shinyHunts.add(
                    listOf(
                        formID,
                        originGame,
                        "Random Encounter",
                        String.format("%04d-%02d-%02d", startYear, startMonth, startDay),
                        counter,
                        phase,
                        "",
                        0,
                        null,
                        null
                    )
                )
            }
        }


        /*
        // Mock Data Set #2: Some of my personal shiny hunts
        shinyHunts.add(listOf(740, 33, "Soft Resets", "2024-09-20", 3111, 0, "", 1, "2025-01-06", 37))  // Darkrai
        shinyHunts.add(listOf(341, 37, "Masuda Method", "", 319, 0, "", 1, "2025-01-17", 37))           // Gligar
        shinyHunts.add(listOf(734, 28, "Soft Resets", "", 1768, 0, "", 1, "", 38))                      // Regigigas
        shinyHunts.add(listOf(733, 27, "Soft Resets", "", 1381, 0, "", 1, "", 38))                      // Heatran
        shinyHunts.add(listOf(233, 28, "Soft Resets", "", 559, 0, "", 1, "", 38))                       // Zapdos
        shinyHunts.add(listOf(738, 37, "Masuda Method", "", 296, 0, "", 1, "", 37))                     // Phione
        shinyHunts.add(listOf(932, 27, "Soft Resets", "", 952, 0, "", 1, "", 38))                       // Tornadus
        shinyHunts.add(listOf(235, 28, "Soft Resets", "", 493, 0, "", 1, "", 38))                       // Moltres
        shinyHunts.add(listOf(934, 28, "Soft Resets", "", 4, 0, "", 1, "", 38))                         // Thundurus
        shinyHunts.add(listOf(231, 27, "Soft Resets", "", 1704, 0, "", 1, "", 38))                      // Articuno
        shinyHunts.add(listOf(938, 28, "Soft Resets", "", 2058, 0, "", 1, "", 38))                      // Landorus (Ultra Moon)
        shinyHunts.add(listOf(938, 27, "Soft Resets", "", 563, 0, "", 1, "", 38))                       // Landorus (Ultra Sun)
        shinyHunts.add(listOf(737, 28, "Soft Resets", "", 835, 0, "", 1, "", 38))                       // Cresselia
        shinyHunts.add(listOf(937, 28, "Soft Resets", "", 1414, 0, "", 1, "", 28))                      // Zekrom
        shinyHunts.add(listOf(1180, 28, "Soft Resets", "2025-5-12", 122, 0, "", 1, "2025-5-13", 28))    // Pheromosa (Ultra Moon)
        shinyHunts.add(listOf(729, 27, "Soft Resets", "", 3026, 0, "", 1, "2025-5-15", 27))             // Dialga
        shinyHunts.add(listOf(958, 21, "Soft Resets", "2025-5-14", 2400, 0, "Pokemon X Shiny-Only Run", 0, "", null))           // Kalos Starter (X; ongoing)
        shinyHunts.add(listOf(936, 27, "Soft Resets", "2025-5-16", 100, 0, "", 0, "", null))            // Reshiram (ongoing)
        */

        // insert each shiny hunt into the database
        for (hunt in shinyHunts) {
            val values = ContentValues().apply {
                put(DBHelper.FORM_ID_COL, hunt[0] as Int)
                put(DBHelper.ORIGIN_GAME_ID_COL, hunt[1] as Int?)
                put(DBHelper.METHOD_COL, hunt[2] as String)
                put(DBHelper.START_DATE_COL, hunt[3] as String)
                put(DBHelper.COUNTER_COL, hunt[4] as Int)
                put(DBHelper.PHASE_COL, hunt[5] as Int)
                put(DBHelper.NOTES_COL, hunt[6] as String)
                put(DBHelper.IS_COMPLETE_COL, hunt[7] as Int)
                put(DBHelper.FINISH_DATE_COL, hunt[8] as String?)
                put(DBHelper.CURRENT_GAME_ID_COL, hunt[9] as Int?)
            }
            val newHuntID = db.insert(DBHelper.SHINY_HUNT_TABLE, null, values)
            if (newHuntID == -1L) {
                Log.e("ShinyHuntModel", "Error inserting shiny hunt into the database: $hunt")
            } else {
                Log.d("ShinyHuntModel", "Shiny hunt inserted into the database: $hunt")
                // use the returned huntID to set the shiny hunt's defaultPosition
                val updateValues = ContentValues().apply {
                    put(DBHelper.DEFAULT_POSITION_COL, newHuntID)
                }
                val result = db.update(DBHelper.SHINY_HUNT_TABLE, updateValues, "${DBHelper.HUNT_ID_COL} = ?", arrayOf(newHuntID.toString()))
                if (result == 0) {
                    Log.e("ShinyHuntModel", "Error setting $DBHelper.DEFAULT_POSITION_COL of shiny hunt: $hunt")
                } else {
                    Log.d("ShinyHuntModel", "Set defaultPosition '$newHuntID' of shiny hunt: $hunt")
                }
            }
        }

        Log.d("ShinyHuntData", "insertShinyHuntData() completed")
    }
}

// Helper enum classes for sorting/filtering the shiny hunts

// SortMethod: methods to sort the shiny hunts by
enum class SortMethod(val sortMethod: String) {
    DEFAULT(DBHelper.DEFAULT_POSITION_COL),     // sorts by defaultPosition (initially just the order the shiny hunts were created in, but allows for the position of hunts to be swapped)
    DATE_STARTED(DBHelper.START_DATE_COL),      // sorts by startDate
    DATE_FINISHED(DBHelper.FINISH_DATE_COL),    // sorts by finishDate
    NAME(DBHelper.POKEMON_NAME_COL),            // sorts by pokemonName
    GENERATION(DBHelper.FORM_ID_COL)            // sorts by formID (since it already follows Pokedex order)
}

// SortOrder: specifies if the shiny hunts should be sorted in ascending or descending order
enum class SortOrder(val order: String) {
    ASC("ASC"),
    DESC("DESC")
}

// CompletionStatus: filters for shiny hunt based on completion status
enum class CompletionStatus(val isComplete: Int) {
    IN_PROGRESS(0), // in the database, isComplete = 0 corresponds to an in-progress hunt
    COMPLETE(1),    // in the database, isComplete = 1 corresponds to a completed hunt
    BOTH(2)         // placeholder for when no completion status filter is selected
}