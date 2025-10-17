package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

data class Pokeball(val pokeballID: Int, val pokeballName: String, val pokeballImage: Int)

object PokeballData {

    // Function to insert the pokeballs into the database
    fun insertPokeballData(db: SQLiteDatabase) {
        // format: pokeball name, pokeball image
        val pokeballList = listOf(
            ("Poké Ball" to R.drawable.pokeball_001_poke_ball),
            ("Great Ball" to R.drawable.pokeball_002_great_ball),
            ("Ultra Ball" to R.drawable.pokeball_003_ultra_ball),
            ("Master Ball" to R.drawable.pokeball_004_master_ball),
            ("Safari Ball" to R.drawable.pokeball_005_safari_ball),
            ("Fast Ball" to R.drawable.pokeball_006_fast_ball),
            ("Level Ball" to R.drawable.pokeball_007_level_ball),
            ("Lure Ball" to R.drawable.pokeball_008_lure_ball),
            ("Heavy Ball" to R.drawable.pokeball_009_heavy_ball),
            ("Love Ball" to R.drawable.pokeball_010_love_ball),
            ("Friend Ball" to R.drawable.pokeball_011_friend_ball),
            ("Moon Ball" to R.drawable.pokeball_012_moon_ball),
            ("Sport Ball" to R.drawable.pokeball_013_sport_ball),
            ("Net Ball" to R.drawable.pokeball_014_net_ball),
            ("Dive Ball" to R.drawable.pokeball_015_dive_ball),
            ("Nest Ball" to R.drawable.pokeball_016_nest_ball),
            ("Repeat Ball" to R.drawable.pokeball_017_repeat_ball),
            ("Timer Ball" to R.drawable.pokeball_018_timer_ball),
            ("Luxury Ball" to R.drawable.pokeball_019_luxury_ball),
            ("Premier Ball" to R.drawable.pokeball_020_premier_ball),
            ("Dusk Ball" to R.drawable.pokeball_021_dusk_ball),
            ("Heal Ball" to R.drawable.pokeball_022_heal_ball),
            ("Quick Ball" to R.drawable.pokeball_023_quick_ball),
            ("Cherish Ball" to R.drawable.pokeball_024_cherish_ball),
            ("Park Ball" to R.drawable.pokeball_025_park_ball),
            ("Dream Ball" to R.drawable.pokeball_026_dream_ball),
            ("Beast Ball" to R.drawable.pokeball_027_beast_ball),
            ("Strange Ball" to R.drawable.pokeball_028_strange_ball),
            ("Poké Ball (Hisui)" to R.drawable.pokeball_029_poke_ball_hisui),
            ("Great Ball (Hisui)" to R.drawable.pokeball_030_great_ball_hisui),
            ("Ultra Ball (Hisui)" to R.drawable.pokeball_031_ultra_ball_hisui),
            ("Feather Ball" to R.drawable.pokeball_032_feather_ball),
            ("Wing Ball" to R.drawable.pokeball_033_wing_ball),
            ("Jet Ball" to R.drawable.pokeball_034_jet_ball),
            ("Heavy Ball (Hisui)" to R.drawable.pokeball_035_heavy_ball_hisui),
            ("Leaden Ball" to R.drawable.pokeball_036_leaden_ball),
            ("Gigaton Ball" to R.drawable.pokeball_037_gigaton_ball),
            ("Origin Ball" to R.drawable.pokeball_038_origin_ball)
        )

        // insert each pokeball into the database
        for ((name, image) in pokeballList) {
            val values = ContentValues().apply {
                put(DBHelper.POKEBALL_NAME_COL, name)
                put(DBHelper.POKEBALL_IMAGE_COL, image)
            }
            val result = db.insert(DBHelper.POKEBALL_TABLE, null, values)
            if (result == -1L) {
                Log.e("GameModel", "Error inserting pokeball into the database: \"$name\"")
            }
        }
    }

}