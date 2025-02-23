package com.example.pokemonshinytracker

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log

data class Game(val gameID: Int, val gameName: String, val gameImage: Int, val generation: Int)

object GameData {
    fun insertGameData(db: SQLiteDatabase, GAME_TABLE: String, GAME_NAME_COL: String, GAME_IMAGE_COL: String, GENERATION_COL: String) {
        val gameList = listOf(
            Triple("Default", R.drawable.etc_default, 0),
            Triple("Bank", R.drawable.game_0_bank, 0),
            Triple("Home", R.drawable.game_0_home, 0),
            Triple("GO", R.drawable.game_0_go, 0),
            Triple("Red", R.drawable.game_1_red, 1),
            Triple("Blue", R.drawable.game_1_blue, 1),
            Triple("Green", R.drawable.game_1_green, 1),
            Triple("Yellow", R.drawable.game_1_yellow, 1),
            Triple("Gold", R.drawable.game_2_gold, 2),
            Triple("Silver", R.drawable.game_2_silver, 2),
            Triple("Crystal", R.drawable.game_2_crystal, 2),
            Triple("Ruby", R.drawable.game_3_ruby, 3),
            Triple("Sapphire", R.drawable.game_3_sapphire, 3),
            Triple("Emerald", R.drawable.game_3_emerald, 3),
            Triple("FireRed", R.drawable.game_3_fire_red, 3),
            Triple("LeafGreen", R.drawable.game_3_leaf_green, 3),
            Triple("Diamond", R.drawable.game_4_diamond, 4),
            Triple("Pearl", R.drawable.game_4_pearl, 4),
            Triple("Platinum", R.drawable.game_4_platinum, 4),
            Triple("HeartGold", R.drawable.game_4_heart_gold, 4),
            Triple("SoulSilver", R.drawable.game_4_soul_silver, 4),
            Triple("Black", R.drawable.game_5_black, 5),
            Triple("White", R.drawable.game_5_white, 5),
            Triple("Black 2", R.drawable.game_5_black_2, 5),
            Triple("White 2", R.drawable.game_5_white_2, 5),
            Triple("X", R.drawable.game_6_x, 6),
            Triple("Y", R.drawable.game_6_y, 6),
            Triple("Omega Ruby", R.drawable.game_6_omega_ruby, 6),
            Triple("Alpha Sapphire", R.drawable.game_6_alpha_sapphire, 6),
            Triple("Sun", R.drawable.game_7_sun, 7),
            Triple("Moon", R.drawable.game_7_moon, 7),
            Triple("Ultra Sun", R.drawable.game_7_ultra_sun, 7),
            Triple("Ultra Moon", R.drawable.game_7_ultra_moon, 7),
            Triple("Let's Go, Pikachu!", R.drawable.game_7_lets_go_pikachu, 7),
            Triple("Let's Go, Eevee!", R.drawable.game_7_lets_go_eevee, 7),
            Triple("Sword", R.drawable.game_8_sword, 8),
            Triple("Shield", R.drawable.game_8_shield, 8),
            Triple("Brilliant Diamond", R.drawable.game_8_brilliant_diamond, 8),
            Triple("Shining Pearl", R.drawable.game_8_shining_pearl, 8),
            Triple("Legends: Arceus", R.drawable.game_8_legends_arceus, 8),
            Triple("Scarlet", R.drawable.game_9_scarlet, 9),
            Triple("Violet", R.drawable.game_9_violet, 9)
        )

        for ((name, image, generation) in gameList) {
            val values = ContentValues().apply {
                put(GAME_NAME_COL, name)
                put(GAME_IMAGE_COL, image)
                put(GENERATION_COL, generation)
            }
            db.insert(GAME_TABLE, null, values)
            Log.d("GameModel", "Game \"$name\" inserted into the database")
        }
        Log.d("GameModel", "All games have been inserted into the database")
    }
}