package com.example.pokemonshinytracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getStringOrNull
import java.sql.SQLException

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // Function to create the database
    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DBHelper", "onCreate() started")

        try {
            db.beginTransaction()   // start the database transaction

            // STEP 1: Create Pokemon Table
            try {
                val query1 = ("""
                CREATE TABLE $POKEMON_TABLE (
                    $POKEMON_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                    $POKEMON_NAME_COL TEXT
                )
                """).trimIndent()
                db.execSQL(query1)
                Log.d("DBHelper", "$POKEMON_TABLE table created")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error creating $POKEMON_TABLE table: ${e.message}")
            }

            // STEP 2: Create PokemonForm Table
            try {
                val query2 = ("""
                CREATE TABLE $POKEMON_FORM_TABLE (
                    $FORM_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                    $POKEMON_ID_COL INTEGER,
                    $FORM_NAME_COL TEXT,
                    $FORM_IMAGE_COL INTEGER,
                    $IS_DEFAULT_FORM_COL INTEGER,
                    FOREIGN KEY ($POKEMON_ID_COL) REFERENCES $POKEMON_TABLE($POKEMON_ID_COL)
                )
                """).trimIndent()
                db.execSQL(query2)
                Log.d("DBHelper", "$POKEMON_FORM_TABLE table created")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error creating $POKEMON_FORM_TABLE table: ${e.message}")
            }

            // STEP 3: Create Game Table
            try {
                val query3 = ("""
                CREATE TABLE $GAME_TABLE (
                    $GAME_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                    $GAME_NAME_COL TEXT,
                    $GAME_IMAGE_COL INTEGER,
                    $GENERATION_COL INTEGER
                )
                """).trimIndent()
                db.execSQL(query3)
                Log.d("DBHelper", "$GAME_TABLE table created")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error creating $GAME_TABLE table: ${e.message}")
            }

            // STEP 4: Create ShinyHunt Table
            try {
                val query4 = ("""
                CREATE TABLE $SHINY_HUNT_TABLE (
                    $HUNT_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                    $FORM_ID_COL INTEGER,
                    $ORIGIN_GAME_ID_COL INTEGER,
                    $METHOD_COL TEXT,
                    $START_DATE_COL TEXT,
                    $COUNTER_COL INTEGER,
                    $PHASE_COL INTEGER,
                    $IS_COMPLETE_COL INTEGER,
                    $FINISH_DATE_COL TEXT,
                    $CURRENT_GAME_ID_COL INTEGER,
                    $DEFAULT_POSITION_COL INTEGER,
                    FOREIGN KEY ($FORM_ID_COL) REFERENCES $POKEMON_FORM_TABLE($FORM_ID_COL),
                    FOREIGN KEY ($ORIGIN_GAME_ID_COL) REFERENCES $GAME_TABLE($GAME_ID_COL),
                    FOREIGN KEY ($CURRENT_GAME_ID_COL) REFERENCES $GAME_TABLE($GAME_ID_COL)	
                )
                """).trimIndent()
                db.execSQL(query4)
                Log.d("DBHelper", "$SHINY_HUNT_TABLE table created")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error creating $SHINY_HUNT_TABLE table: ${e.message}")
            }

            // STEP 5: Insert initial data into the database
            try {
                PokemonData.insertPokemonData(db, POKEMON_TABLE, POKEMON_NAME_COL)
                PokemonFormData.insertPokemonFormData(db, POKEMON_FORM_TABLE, POKEMON_ID_COL, FORM_NAME_COL, FORM_IMAGE_COL, IS_DEFAULT_FORM_COL)
                GameData.insertGameData(db, GAME_TABLE, GAME_NAME_COL, GAME_IMAGE_COL, GENERATION_COL)
                ShinyHuntData.insertShinyHuntData(db, SHINY_HUNT_TABLE, HUNT_ID_COL, FORM_ID_COL, ORIGIN_GAME_ID_COL, METHOD_COL, START_DATE_COL, COUNTER_COL, PHASE_COL, IS_COMPLETE_COL, FINISH_DATE_COL, CURRENT_GAME_ID_COL, DEFAULT_POSITION_COL)
                db.setTransactionSuccessful()   // mark the database transaction as successful
            } catch (e: Exception) {
                Log.e("DBHelper", "Error inserting initial data: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Database creation failed: ${e.message}")
        } finally {
            db.endTransaction()     // end the database transaction; rollback if not set as successful
        }

        Log.d("DBHelper", "onCreate() completed")
    }

    // Function to upgrade the database when the version changes
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Log.d("DBHelper", "onUpgrade() started")

        try {
            db.beginTransaction()   // start the database transaction

            // STEP 1: Drop the ShinyHunt Table (to remove foreign keys to the other tables)
            try {
                db.execSQL("DROP TABLE IF EXISTS $SHINY_HUNT_TABLE")
                Log.d("DBHelper", "$SHINY_HUNT_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $SHINY_HUNT_TABLE table: ${e.message}")
            }

            // STEP 2: Drop the PokemonForm Table (to remove foreign keys to the Pokemon table)
            try {
                db.execSQL("DROP TABLE IF EXISTS $POKEMON_FORM_TABLE")
                Log.d("DBHelper", "$POKEMON_FORM_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $POKEMON_FORM_TABLE table: ${e.message}")
            }

            // STEP 3: Drop the Pokemon Table
            try {
                db.execSQL("DROP TABLE IF EXISTS $POKEMON_TABLE")
                Log.d("DBHelper", "$POKEMON_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $POKEMON_TABLE table: ${e.message}")
            }

            // STEP 4: Drop the Game Table
            try {
                db.execSQL("DROP TABLE IF EXISTS $GAME_TABLE")
                Log.d("DBHelper", "$GAME_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $GAME_TABLE table: ${e.message}")
            }

            // STEP 5: Recreate the database
            try {
                onCreate(db)
                Log.d("DBHelper", "Database recreated successfully")
                db.setTransactionSuccessful()   // mark the database transaction as successful
            } catch (e: Exception) {
                Log.e("DBHelper", "Error recreating database: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Database upgrade failed: ${e.message}")
        } finally {
            db.endTransaction()     // end the database transaction; rollback if not set as successful
        }

        Log.d("DBHelper", "onUpgrade() completed")
    }

    // Function to force an upgrade on the database (for debug purposes)
    fun forceUpgrade() {
        Log.d("DBHelper", "forceUpgrade() started")

        var db: SQLiteDatabase? = null

        try {
            // attempt to get a writable instance of the database
            try {
                db = writableDatabase
                Log.d("DBHelper", "Writable instance of the database created")
            } catch (e: SQLiteException) {
                Log.e("DBHelper", "Error creating a writable instance of the database: ${e.message}")
                return  // exit early if the database instance can't be created
            }

            db.beginTransaction()   // start the database transaction

            // STEP 1: Drop the ShinyHunt Table (to remove foreign keys to the other tables)
            try {
                db.execSQL("DROP TABLE IF EXISTS $SHINY_HUNT_TABLE")
                Log.d("DBHelper", "$SHINY_HUNT_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $SHINY_HUNT_TABLE table: ${e.message}")
            }

            // STEP 2: Drop the PokemonForm Table (to remove foreign keys to the Pokemon table)
            try {
                db.execSQL("DROP TABLE IF EXISTS $POKEMON_FORM_TABLE")
                Log.d("DBHelper", "$POKEMON_FORM_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $POKEMON_FORM_TABLE table: ${e.message}")
            }

            // STEP 3: Drop the Pokemon Table
            try {
                db.execSQL("DROP TABLE IF EXISTS $POKEMON_TABLE")
                Log.d("DBHelper", "$POKEMON_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $POKEMON_TABLE table: ${e.message}")
            }

            // STEP 4: Drop the Game Table
            try {
                db.execSQL("DROP TABLE IF EXISTS $GAME_TABLE")
                Log.d("DBHelper", "$GAME_TABLE table dropped")
            } catch (e: SQLException) {
                Log.e("DBHelper", "Error dropping $GAME_TABLE table: ${e.message}")
            }

            // STEP 5: Recreate the database
            try {
                onCreate(db)
                Log.d("DBHelper", "Database recreated successfully")
                db.setTransactionSuccessful()   // mark the database transaction as successful
            } catch (e: Exception) {
                Log.e("DBHelper", "Error recreating database: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Database force upgrade failed: ${e.message}")
        } finally {
            db?.endTransaction()    // end the database transaction; rollback if not set as successful
            db?.close()     // close the database
        }

        Log.d("DBHelper", "forceUpgrade() completed")
    }


    // Function to add new shiny hunts to the database
    private fun addHunt(db: SQLiteDatabase, formID: Int?, originGameID: Int?, method: String, startDate: String?,
                counter: Int, phase: Int, isComplete: Boolean, finishDate: String?, currentGameID: Int?, defaultPosition: Int?) {
        Log.d("DBHelper", "addHunt() started")

        val values = ContentValues().apply {
            put(FORM_ID_COL, formID)
            put(ORIGIN_GAME_ID_COL, originGameID)
            put(METHOD_COL, method)
            put(START_DATE_COL, startDate)
            put(COUNTER_COL, counter)
            put(PHASE_COL, phase)
            put(IS_COMPLETE_COL, if (isComplete) 1 else 0)  // store as integer since SQLite doesn't support Boolean type
            put(FINISH_DATE_COL, finishDate)
            put(CURRENT_GAME_ID_COL, currentGameID)
        }

        // insert the new hunt into the database
        try {
            // db.beginTransaction()   // start the database transaction

            val newHuntID = db.insert(SHINY_HUNT_TABLE, null, values)
            if (newHuntID == -1L) {
                Log.e("DBHelper", "Failed to insert shiny hunt with formID: $formID")
            } else {
                Log.d("DBHelper", "Inserted shiny hunt with formID: $formID, ID: $newHuntID")
                // use the returned huntID to set the shiny hunt's defaultPosition
                val updateValues = ContentValues().apply {
                    put(DEFAULT_POSITION_COL, newHuntID)
                }

                // specify the row to update (via the huntID)
                val selection = "$HUNT_ID_COL = ?"
                val selectionArgs = arrayOf(newHuntID.toInt().toString())
                Log.d("DBHelper", "selectionArgs: ${selectionArgs[0]}")

                val result = db.update(SHINY_HUNT_TABLE, updateValues, selection, selectionArgs)
                if (result == 0) {
                    Log.e("DBHelper", "Error setting $DEFAULT_POSITION_COL of the new shiny hunt")
                } else {
                    Log.d("DBHelper", "Set defaultPosition '$newHuntID' of the new shiny hunt")
                    db.setTransactionSuccessful()   // set the database transaction as successful
                }
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error inserting shiny hunt: ${e.message}")
        } finally {
            // db.endTransaction()     // end the database transaction; rollback if not successful
        }

        Log.d("DBHelper", "addHunt() completed")
    }

    // Function to delete a shiny hunt from the database
    fun deleteHunt(huntID : Int) {
        Log.d("DBHelper", "deleteHunt() started")

        var db: SQLiteDatabase? = null

        try {
            // attempt to get a writable instance of the database
            try {
                db = writableDatabase
                Log.d("DBHelper", "Writable instance of the database created")
            } catch (e: SQLiteException) {
                Log.e("DBHelper", "Error creating a writable instance of the database: ${e.message}")
                return  // exit early if the database instance can't be created
            }

            db.beginTransaction()   // start the database transaction

            // delete the hunt from the database
            val result = db.delete(SHINY_HUNT_TABLE, "huntID = ?", arrayOf(huntID.toString()))
            Log.d("DBHelper", "$result row(s) deleted from the $SHINY_HUNT_TABLE table")

            // set the database transaction as successful
            db.setTransactionSuccessful()

        } catch (e: Exception) {
            Log.e("DBHelper", "Error deleting shiny hunt: ${e.message}")
        } finally {
            db?.endTransaction()     // end the database transaction; rollback if not successful
            db?.close()     // close the database
        }

        Log.d("DBHelper", "deleteHunt() completed")
    }

    // Function to update a hunt in the database
    fun updateHunt(huntID: Int, formID: Int?, originGameID: Int?, method: String, startDate: String?,
                   counter: Int, phase: Int, isComplete: Boolean, finishDate: String?, currentGameID: Int?, defaultPosition: Int?) {
        Log.d("DBHelper", "updateHunt() started")

        var db: SQLiteDatabase? = null

        try {
            // attempt to get a writable instance of the database
            try {
                db = writableDatabase
                Log.d("DBHelper", "Writable instance of the database created")
            } catch (e: SQLiteException) {
                Log.e("DBHelper", "Error creating a writable instance of the database: ${e.message}")
                return  // exit early if the database instance can't be created
            }

            db.beginTransaction()

            // huntID == 0 => creating a new shiny hunt; call addHunt()
            if (huntID == 0) {
                Log.d("DBHelper", "huntID is 0. Calling addHunt() to create a new hunt")
                addHunt(
                    db,
                    formID,
                    originGameID,
                    method,
                    startDate,
                    counter,
                    phase,
                    isComplete,
                    finishDate,
                    currentGameID,
                    defaultPosition
                )
            }

            // huntID >= 0 => updating an existing shiny hunt
            else {
                Log.d("DBHelper", "huntID is $huntID. Creating ContentValues for the updated row")

                val values = ContentValues().apply {
                    put(HUNT_ID_COL, huntID)
                    put(FORM_ID_COL, formID)
                    put(ORIGIN_GAME_ID_COL, originGameID)
                    put(METHOD_COL, method)
                    put(START_DATE_COL, startDate)
                    put(COUNTER_COL, counter)
                    put(PHASE_COL, phase)
                    put(IS_COMPLETE_COL, if (isComplete) 1 else 0)      // store as integer since SQLite doesn't support Boolean type
                    put(FINISH_DATE_COL, finishDate)
                    put(CURRENT_GAME_ID_COL, currentGameID)
                    put(DEFAULT_POSITION_COL, defaultPosition)
                }

                // specify the row to update (via the huntID)
                val selection = "$HUNT_ID_COL = ?"
                val selectionArgs = arrayOf(huntID.toString())
                Log.d("DBHelper", "selectionArgs: ${selectionArgs[0]}")

                // update the table
                val result = db.update(SHINY_HUNT_TABLE, values, selection, selectionArgs)
                Log.d("DBHelper", "$result row(s) updated in the $SHINY_HUNT_TABLE table")

                // set the database transaction as successful
                db.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            Log.e("DBHelper", "Error updating shiny hunt: ${e.message}")
        } finally {
            db?.endTransaction()    // end the database transaction; rollback if not successful
            db?.close()     // close the database
        }

        Log.d("DBHelper", "updateHunt() completed")
    }

    // Function to retrieve all shiny hunts from the database (by ID or all hunts if no ID is provided)
    fun getHunts(huntID: Int? = null): List<ShinyHunt> {
        Log.d("DBHelper", "getHunts() started")

        val huntList = mutableListOf<ShinyHunt>()
        var db: SQLiteDatabase? = null

        try {
            // attempt to get a readable instance of the database
            db = this.readableDatabase
            Log.d("DBHelper", "Readable instance of the database created")

            var query: String
            val args: Array<String>?

            Log.d("DBHelper", "Creating static part of the getHunts SQL query")
            query = ("""
            SELECT s.*, p.$POKEMON_NAME_COL
            FROM $SHINY_HUNT_TABLE AS s
            JOIN $POKEMON_FORM_TABLE AS pf USING ($FORM_ID_COL)
            JOIN $POKEMON_TABLE AS p USING ($POKEMON_ID_COL)
            """.trimIndent())

            if (huntID != null) {
                Log.d("DBHelper", "huntID is $huntID. Appending WHERE clause to filter by huntID")
                query += " WHERE $HUNT_ID_COL = ?"
                args = arrayOf(huntID.toString())
            } else {
                Log.d("DBHelper", "huntID is null. Appending ORDER BY clause to retrieve and sort all saved shiny hunts")
                query += " ORDER BY $DEFAULT_POSITION_COL DESC"
                args = null
            }

            db.rawQuery(query, args).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val hunt = ShinyHunt(
                            huntID = cursor.getInt(0),
                            formID = if (cursor.isNull(1)) null else cursor.getInt(1),
                            originGameID = if (cursor.isNull(2)) null else cursor.getInt(2),
                            method = cursor.getString(3),
                            startDate = cursor.getString(4),
                            counter = cursor.getInt(5),
                            phase = cursor.getInt(6),
                            isComplete = cursor.getInt(7) == 1, // Convert 0/1 to Boolean
                            finishDate = cursor.getString(8),
                            currentGameID = if (cursor.isNull(9)) null else cursor.getInt(9),
                            defaultPosition = cursor.getInt(10),
                            pokemonName = cursor.getString(11)
                        )
                        huntList.add(hunt)
                    } while (cursor.moveToNext())
                }
            }

            Log.d("DBHelper", "getHunts() completed. Retrieved ${huntList.size} shiny hunts")

        } catch (e: SQLiteException) {
            Log.e("DBHelper", "Error opening readable database: ${e.message}")
        } catch (e: Exception) {
            Log.e("DBHelper", "Unexpected error retrieving shiny hunts: ${e.message}")
        } finally {
            db?.close()  // close the database
        }

        return huntList
    }

    // Function to swap the default position of shiny hunts
    fun swapHunts(firstHunt: ShinyHunt, secondHunt: ShinyHunt) {
        Log.d("DBHelper", "swapHunts() started")

        var db: SQLiteDatabase? = null

        try {
            // attempt to get a writable instance of the database
            try {
                db = writableDatabase
                Log.d("DBHelper", "Writable instance of the database created")
            } catch (e: SQLiteException) {
                Log.e("DBHelper", "Error creating a writable instance of the database: ${e.message}")
                return  // exit early if the database instance can't be created
            }

            db.beginTransaction()

            // set firstHunt's defaultPosition to secondHunt's defaultPosition
            val firstHuntValues = ContentValues().apply {
                put(DEFAULT_POSITION_COL, secondHunt.defaultPosition)
            }
            db.update(
                SHINY_HUNT_TABLE,
                firstHuntValues,
                "$HUNT_ID_COL = ?",
                arrayOf(firstHunt.huntID.toString())
            )

            // set secondHunt's defaultPosition to firstHunt's original defaultPosition
            val secondHuntValues = ContentValues().apply {
                put(DEFAULT_POSITION_COL, firstHunt.defaultPosition)
            }
            db.update(
                SHINY_HUNT_TABLE,
                secondHuntValues,
                "$HUNT_ID_COL = ?",
                arrayOf(secondHunt.huntID.toString())
            )

            db.setTransactionSuccessful()
            Log.d("DBHelper", "Swapped defaultPosition of hunt ${firstHunt.huntID} and ${secondHunt.huntID}")
        } catch (e: SQLException) {
            Log.e("DBHelper", "Error swapping shiny hunts: ${e.message}")
        } finally {
            db?.endTransaction()
            db?.close()
            Log.d("DBHelper", "swapHunts() completed")
        }
    }

    // Function to retrieve pokemon from the database (by pokemonID or all pokemon if no ID is provided)
    fun getPokemon(pokemonID: Int? = null, formID: Int? = null): List<Pokemon> {
        Log.d("DBHelper", "getPokemon() started")

        val pokemonMap = mutableMapOf<Int, Pokemon>()
        var db: SQLiteDatabase? = null

        try {
            // attempt to get a readable instance of the database
            db = this.readableDatabase
            Log.d("DBHelper", "Readable instance of the database created")

            // construct query dynamically based on the parameters
            val query = StringBuilder(
                """
                SELECT p.$POKEMON_ID_COL, p.$POKEMON_NAME_COL, 
                       f.$FORM_ID_COL, f.$FORM_NAME_COL, f.$FORM_IMAGE_COL, f.$IS_DEFAULT_FORM_COL
                FROM $POKEMON_TABLE p
                LEFT JOIN $POKEMON_FORM_TABLE f ON p.$POKEMON_ID_COL = f.$POKEMON_ID_COL
                """)
            val args = mutableListOf<String>()

            // apply filtering conditions
            if (pokemonID != null) {
                query.append(" WHERE p.$POKEMON_ID_COL = ?")
                args.add(pokemonID.toString())
            } else if (formID != null) {
                query.append(" WHERE f.$FORM_ID_COL = ?")
                args.add(formID.toString())
            }

            db.rawQuery(query.toString(), args.toTypedArray()).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val pID = cursor.getInt(0)
                        val pName = cursor.getString(1)

                        // ensure the Pokemon is in the map
                        val pokemon = pokemonMap.getOrPut(pID) {
                            Pokemon(pID, pName, mutableListOf())
                        }

                        // if form data is not null, add it to the Pokemon's forms list
                        if (!cursor.isNull(2)) {
                            val form = PokemonForm(
                                formID = cursor.getInt(2),
                                formName = cursor.getStringOrNull(3),
                                formImage = cursor.getInt(4),
                                isDefaultForm = cursor.getInt(5) == 1
                            )
                            (pokemon.forms as MutableList).add(form)
                        }
                    } while (cursor.moveToNext())
                }
            }

            Log.d("DBHelper", "getPokemon() completed. Retrieved ${pokemonMap.size} Pokemon")

        } catch (e: SQLiteException) {
            Log.e("DBHelper", "Error opening readable database: ${e.message}")
        } catch (e: Exception) {
            Log.e("DBHelper", "Unexpected error retrieving Pokemon: ${e.message}")
        } finally {
            db?.close()  // close the database
        }

        return pokemonMap.values.toList()
    }

    // Function to retrieve games from the database (by ID or all games if no ID is provided
    fun getGames(gameID: Int? = null): List<Game> {
        Log.d("DBHelper", "getGames() started")

        val gameList = mutableListOf<Game>()
        var db: SQLiteDatabase? = null

        try {
            // attempt to get a readable instance of the database
            db = this.readableDatabase
            Log.d("DBHelper", "Readable instance of the database created")

            val query: String
            val args: Array<String>?

            // check if a gameID was provided
            if (gameID != null) {
                Log.d("DBHelper", "Game ID: $gameID")
                query = "SELECT * FROM $GAME_TABLE WHERE gameID = ?"
                args = arrayOf(gameID.toString())
            } else {
                Log.d("DBHelper", "No Game ID. Selecting all games")
                query = "SELECT * FROM $GAME_TABLE"
                args = null
            }

            db.rawQuery(query, args).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val game = Game(
                            gameID = cursor.getInt(0),
                            gameName = cursor.getString(1),
                            gameImage = cursor.getInt(2),
                            generation = cursor.getInt(3)
                        )
                        gameList.add(game)
                    } while (cursor.moveToNext())
                }
            }

            Log.d("DBHelper", "getGames() completed. Retrieved ${gameList.size} games")

        } catch (e: SQLiteException) {
            Log.e("DBHelper", "Error opening readable database: ${e.message}")
        } catch (e: Exception) {
            Log.e("DBHelper", "Unexpected error retrieving games: ${e.message}")
        } finally {
            db?.close()  // close the database
        }

        return gameList
    }

    // Database variables
    companion object{

        // Database name and version
        const val DATABASE_NAME = "SHINY_TRACKER_DB"
        const val DATABASE_VERSION = 4

        // Pokemon Table
        const val POKEMON_TABLE = "Pokemon"
        const val POKEMON_ID_COL = "pokemonID"              // primary key of Pokemon Table
        const val POKEMON_NAME_COL = "pokemonName"

        // PokemonForm Table
        const val POKEMON_FORM_TABLE = "PokemonForm"
        const val FORM_ID_COL = "formID"                    // primary key of PokemonForm Table
        // const val POKEMON_ID_COL = "pokemonName"         // foreign key to Pokemon Table
        const val FORM_NAME_COL = "formName"
        const val FORM_IMAGE_COL = "formImage"
        const val IS_DEFAULT_FORM_COL = "isDefaultForm"

        // Game Table
        const val GAME_TABLE = "Game"
        const val GAME_ID_COL = "gameID"                    // primary key of Game Table
        const val GAME_NAME_COL = "gameName"
        const val GAME_IMAGE_COL = "gameImage"
        const val GENERATION_COL = "generation"

        // ShinyHunt Table
        const val SHINY_HUNT_TABLE = "ShinyHunt"
        const val HUNT_ID_COL = "huntID"                    // primary key of ShinyHunt Table
        // const val FORM_ID_COL = "pokemonID"              // foreign key to PokemonForm Table
        const val ORIGIN_GAME_ID_COL = "originGameID"       // foreign key to Game Table
        const val METHOD_COL = "method"
        const val START_DATE_COL = "startDate"
        const val COUNTER_COL = "counter"
        const val PHASE_COL = "phase"
        const val IS_COMPLETE_COL = "isComplete"
        const val FINISH_DATE_COL = "finishDate"
        const val CURRENT_GAME_ID_COL = "currentGameID"     // foreign key to Game Table
        const val DEFAULT_POSITION_COL = "defaultPosition"  // handles the default order of the shiny hunts
    }
}