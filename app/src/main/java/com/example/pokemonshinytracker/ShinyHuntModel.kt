package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlin.random.Random

data class ShinyHunt(val huntID: Int, var formID: Int?, var nickname: String, var originGameID: Int?, var location: String, var method: String, var startDate: String?, var counter: Int,
                     var phase: Int, var notes: String, var isComplete: Boolean, var finishDate: String?, var pokeballID: Int?, var currentGameID: Int?, var defaultPosition: Int?, var pokemonName: String)

object ShinyHuntData {

    // Function to insert mock shiny hunts into the database
    fun insertShinyHuntData(db: SQLiteDatabase) {
        val shinyHunts = mutableListOf<List<Any?>>()

        /*
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
                        "",
                        originGame,
                        "",
                        "Random Encounter",
                        String.format("%04d-%02d-%02d", startYear, startMonth, startDay),
                        counter,
                        phase,
                        "",
                        1,
                        String.format("%04d-%02d-%02d", finishYear, finishMonth, finishDay),
                        null,
                        currentGame
                    )
                )
            } else {
                shinyHunts.add(
                    listOf(
                        formID,
                        ""
                        originGame,
                        "",
                        "Random Encounter",
                        String.format("%04d-%02d-%02d", startYear, startMonth, startDay),
                        counter,
                        phase,
                        "",
                        0,
                        null,
                        null,
                        null
                    )
                )
            }
        }
        */


        // Mock Data Set #2: Some of my personal shiny hunts
        shinyHunts.add(listOf(740, "Salem", 33, "Newmoon Island", "Soft Resets", "2024-09-20", 3111, 0, "", 1, "2025-01-06", 4, 37))    // Darkrai
        shinyHunts.add(listOf(341, "", 37, "", "Masuda Method", "", 319, 0, "", 1, "2025-01-17", 17, 37))                               // Gligar
        shinyHunts.add(listOf(734, "Reggie", 28, "Ultra Space Wilds", "Soft Resets", "", 1768, 0, "", 1, "2025-01-28", 20, 39))         // Regigigas
        shinyHunts.add(listOf(733, "", 27, "Ultra Space Wilds", "Soft Resets", "", 1381, 0, "", 1, "2025-01-29", 22, 39))               // Heatran
        shinyHunts.add(listOf(233, "", 28, "Ultra Space Wilds", "Soft Resets", "", 559, 0, "", 1, "2025-02-05", 19, 39))                // Zapdos
        shinyHunts.add(listOf(738, "", 37, "", "Masuda Method", "", 296, 0, "", 1, "2025-02-10", 1, 37))                                // Phione
        shinyHunts.add(listOf(932, "", 27, "Ultra Space Wilds", "Soft Resets", "", 952, 0, "", 1, "2025-02-13", 16, 39))                // Tornadus
        shinyHunts.add(listOf(235, "", 28, "Ultra Space Wilds", "Soft Resets", "", 493, 0, "", 1, "2025-02-13", 22, 39))                // Moltres
        shinyHunts.add(listOf(934, "", 28, "Ultra Space Wilds", "Soft Resets", "", 4, 0, "", 1, "2025-02-15", 15, 39))                  // Thundurus
        shinyHunts.add(listOf(231, "", 27, "Ultra Space Wilds", "Soft Resets", "", 1704, 0, "", 1, "2025-03-02", 15, 39))               // Articuno
        shinyHunts.add(listOf(938, "", 28, "Ultra Space Wilds", "Soft Resets", "", 2058, 0, "", 1, "2025-03-06", 19, 39))               // Landorus (Ultra Moon)
        shinyHunts.add(listOf(938, "", 27, "Ultra Space Wilds", "Soft Resets", "", 563, 0, "", 1, "2025-03-10", 27, 39))                // Landorus (Ultra Sun)
        shinyHunts.add(listOf(737, "", 28, "Ultra Space Wilds", "Soft Resets", "", 835, 0, "", 1, "", null, 28))                        // Cresselia
        shinyHunts.add(listOf(937, "", 28, "Ultra Space Wilds", "Soft Resets", "", 1414, 0, "", 1, "", null, 28))                       // Zekrom
        shinyHunts.add(listOf(1180, "", 28, "Ultra Desert", "Soft Resets", "2025-05-12", 122, 0, "", 1, "2025-05-13", null, 28))        // Pheromosa (Ultra Moon)
        shinyHunts.add(listOf(729, "", 27, "Ultra Space Wilds", "Soft Resets", "", 3026, 0, "", 1, "2025-05-15", null, 27))             // Dialga
        shinyHunts.add(listOf(953, "", 22, "", "Soft Resets", "2025-05-14", 3600, 0,
            "Pokemon X Shiny-Only Run\n" +
                    "\n" +
                    "- Dual hunted in X and Y.\n" +
                    "- Alternated the starter every 10 encounters.", 1, "2025-07-22", null, 21))                                        // Quilladin (Y -> traded to X)
        shinyHunts.add(listOf(963, "", 21, "", "Encounters", "2025-07-25", 4548, 0,
            "Pokemon X Shiny-Only Run\n" +
                    "\n" +
                    "- Dual hunted in X and Y.\n" +
                    "- Random Encounters on Route 3.", 1, "2025-08-16", null, 21))                                                      // Diggersby (X)
        shinyHunts.add(listOf(256, "", 21, "", "Encounters", "2025-08-20", 1054, 0,
            "Pokemon X Shiny-Only Run\n" +
                    "\n" +
                    "- Dual hunted in X and Y.\n" +
                    "- Random Encounters on Route 6.", 1, "2025-08-26", null, 21))                                                      // Furret (X)
        shinyHunts.add(listOf(443, "", 22, "", "Encounters", "2025-08-30", 180, 0,
            "Pokemon X Shiny-Only Run\n" +
                    "\n" +
                    "- Dual hunted in X and Y.\n" +
                    "- Horde Encounters on Route 8.", 1, "2025-08-30", null, 21))                                                       // Pelliper (Y -> traded to X)
        shinyHunts.add(listOf(936, "Rengoku", 27, "Ultra Space Wilds", "Soft Resets", "2025-05-16", 2352, 0, "", 1, "2025-09-21", null, 27))         // Reshiram (Ultra Sun)
        shinyHunts.add(listOf(940, "", 27, "Ultra Space Wilds", "Soft Resets", "2025-09-21", 457, 0, "", 1, "2025-09-25", null, 27))                 // Kyurem (Ultra Sun)
        shinyHunts.add(listOf(120, "", 30, "Mt. Moon", "Catch Combo", "2025-09-27", 202, 0, "", 1, "2025-09-27", 3, 30))                // Geodude (Let's Go Eevee)
        shinyHunts.add(listOf(940, "", 28, "Ultra Space Wilds", "Soft Resets", "2025-09-21", 0, 0,
            "Data somehow got deleted during an Android update, so I don't remember the counter value.", 1, "2025-10-01", null, 28))    // Kyurem (Ultra Moon)                                                                          // Kyurem (Ultra Moon)
        shinyHunts.add(listOf(569, "", 27, "Ultra Space Wilds", "Soft Resets", "2025-09-25", 1372, 0, "", 1, "2025-10-09", null, 27))   // Regice (Ultra Sun)
        shinyHunts.add(listOf(1042, "", 21, "Ambrette Town", "Fossils", "2025-10-01", 1500, 0,
            "Pokemon X Shiny-Only Run\n" +
                    "\n" +
                    "- Dual hunted in X and Y.", 0, "", null, null))                                                                    // Tyrunt (in progress)

        // insert each shiny hunt into the database
        for (hunt in shinyHunts) {
            val values = ContentValues().apply {
                put(DBHelper.FORM_ID_COL, hunt[0] as Int)
                put(DBHelper.NICKNAME_COL, hunt[1] as String)
                put(DBHelper.ORIGIN_GAME_ID_COL, hunt[2] as Int?)
                put(DBHelper.LOCATION_COL, hunt[3] as String)
                put(DBHelper.METHOD_COL, hunt[4] as String)
                put(DBHelper.START_DATE_COL, hunt[5] as String)
                put(DBHelper.COUNTER_COL, hunt[6] as Int)
                put(DBHelper.PHASE_COL, hunt[7] as Int)
                put(DBHelper.NOTES_COL, hunt[8] as String)
                put(DBHelper.IS_COMPLETE_COL, hunt[9] as Int)
                put(DBHelper.FINISH_DATE_COL, hunt[10] as String?)
                put(DBHelper.POKEBALL_ID_COL, hunt[11] as Int?)
                put(DBHelper.CURRENT_GAME_ID_COL, hunt[12] as Int?)
            }
            val newHuntID = db.insert(DBHelper.SHINY_HUNT_TABLE, null, values)
            if (newHuntID == -1L) {
                Log.e("ShinyHuntModel", "Error inserting shiny hunt into the database: $hunt")
            } else {
                // use the returned huntID to set the shiny hunt's defaultPosition
                val updateValues = ContentValues().apply {
                    put(DBHelper.DEFAULT_POSITION_COL, newHuntID)
                }
                val result = db.update(DBHelper.SHINY_HUNT_TABLE, updateValues, "${DBHelper.HUNT_ID_COL} = ?", arrayOf(newHuntID.toString()))
                if (result == 0) {
                    Log.e("ShinyHuntModel", "Error setting $DBHelper.DEFAULT_POSITION_COL of shiny hunt: $hunt")
                }
            }
        }
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