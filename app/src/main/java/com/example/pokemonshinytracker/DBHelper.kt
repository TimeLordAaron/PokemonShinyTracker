package com.example.pokemonshinytracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getStringOrNull
import java.text.Normalizer.Form

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    // Method to create the database
    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DBHelper", "Creating database")
        // STEP 1: Create Pokemon Table
        val query1 = ("""
            CREATE TABLE $POKEMON_TABLE (
                $POKEMON_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $POKEMON_NAME_COL TEXT
            )
            """)
        db.execSQL(query1)
        Log.d("DBHelper", "Pokemon table created")

        // STEP 2: Create PokemonForm Table
        val query2 = ("""
            CREATE TABLE $POKEMON_FORM_TABLE (
            	$FORM_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
            	$POKEMON_ID_COL INTEGER,
            	$FORM_NAME_COL TEXT,
            	$FORM_IMAGE_COL INTEGER,
            	$IS_DEFAULT_FORM_COL INTEGER,
            	FOREIGN KEY ($POKEMON_ID_COL) REFERENCES $POKEMON_TABLE($POKEMON_ID_COL)
            )
            """)
        db.execSQL(query2)
        Log.d("DBHelper", "Pokemon Form table created")

        // STEP 3: Create Game Table
        val query3 = ("""
            CREATE TABLE $GAME_TABLE (
                $GAME_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT,
                $GAME_NAME_COL TEXT,
                $GAME_IMAGE_COL INTEGER,
                $GENERATION_COL INTEGER
            )
            """)
        db.execSQL(query3)
        Log.d("DBHelper", "Game table created")

        // STEP 4: Create ShinyHunt Table
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
                FOREIGN KEY ($FORM_ID_COL) REFERENCES $POKEMON_FORM_TABLE($FORM_ID_COL),
                FOREIGN KEY ($ORIGIN_GAME_ID_COL) REFERENCES $GAME_TABLE($GAME_ID_COL),
                FOREIGN KEY ($CURRENT_GAME_ID_COL) REFERENCES $GAME_TABLE($GAME_ID_COL)	
            )
            """)
        db.execSQL(query4)
        Log.d("DBHelper", "Shiny Hunt table created")

        // STEP 4: Insert the Pokemon into the database
        PokemonData.insertPokemonData(db, POKEMON_TABLE, POKEMON_NAME_COL)

        // STEP 5: Insert the Pokemon Forms into the database
        PokemonFormData.insertPokemonFormData(db, POKEMON_FORM_TABLE, POKEMON_ID_COL, FORM_NAME_COL, FORM_IMAGE_COL, IS_DEFAULT_FORM_COL)

        // STEP 6: Insert the Games into the database
        GameData.insertGameData(db, GAME_TABLE, GAME_NAME_COL, GAME_IMAGE_COL, GENERATION_COL)

        // STEP 7: Insert mock shiny hunts into the database (for test purposes)
        ShinyHuntData.insertShinyHuntData(db, SHINY_HUNT_TABLE, FORM_ID_COL, ORIGIN_GAME_ID_COL, METHOD_COL, START_DATE_COL,
            COUNTER_COL, PHASE_COL, IS_COMPLETE_COL, FINISH_DATE_COL, CURRENT_GAME_ID_COL)
    }

    // Method to upgrade the database when the version changes
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        Log.d("DBHelper", "Upgrading the database")
        // STEP 1: Drop the ShinyHunt Table (to remove foreign keys to the other tables)
        db.execSQL("DROP TABLE IF EXISTS $SHINY_HUNT_TABLE")
        Log.d("DBHelper", "Shiny Hunt table dropped")

        // STEP 2: Drop the PokemonForm Table (to remove foreign keys to the Pokemon table)
        db.execSQL("DROP TABLE IF EXISTS $POKEMON_FORM_TABLE")
        Log.d("DBHelper", "Pokemon Form table dropped")

        // STEP 3: Drop the Pokemon Table
        db.execSQL("DROP TABLE IF EXISTS $POKEMON_TABLE")
        Log.d("DBHelper", "Pokemon table dropped")

        // STEP 4: Drop the Game Table
        db.execSQL("DROP TABLE IF EXISTS $GAME_TABLE")
        Log.d("DBHelper", "Game table dropped")

        // STEP 5: Recreate the database
        onCreate(db)

    }

    // Method to force update the database (for debug purposes)
    fun forceUpgrade() {

        val db = writableDatabase
        Log.d("DBHelper", "Forcing database upgrade")

        // STEP 1: Drop the ShinyHunt Table (to remove foreign keys to the other tables)
        db.execSQL("DROP TABLE IF EXISTS $SHINY_HUNT_TABLE")
        Log.d("DBHelper", "Shiny Hunt table dropped")

        // STEP 2: Drop the PokemonForm Table (to remove foreign keys to the Pokemon table)
        db.execSQL("DROP TABLE IF EXISTS $POKEMON_FORM_TABLE")
        Log.d("DBHelper", "Pokemon Form table dropped")

        // STEP 3: Drop the Pokemon Table
        db.execSQL("DROP TABLE IF EXISTS $POKEMON_TABLE")
        Log.d("DBHelper", "Pokemon table dropped")

        // STEP 4: Drop the Game Table
        db.execSQL("DROP TABLE IF EXISTS $GAME_TABLE")
        Log.d("DBHelper", "Game table dropped")

        Log.d("DBHelper", "All tables dropped, recreating the database")

        // Recreate the database
        onCreate(db)

        Log.d("DBHelper", "Database recreation complete")
    }


    // Method to add new shiny hunts to the database
    fun addHunt(db: SQLiteDatabase, formID: Int?, originGameID: Int?, method: String, startDate: String?,
                counter: Int, phase: Int, isComplete: Boolean, finishDate: String?, currentGameID: Int?){
        Log.d("DBHelper", "addHunt() started")
        // content variable to store the column values
        val values = ContentValues()

        // insert the column values
        values.put(FORM_ID_COL, formID)
        values.put(ORIGIN_GAME_ID_COL, originGameID)
        values.put(METHOD_COL, method)
        values.put(START_DATE_COL, startDate)
        values.put(COUNTER_COL, counter)
        values.put(PHASE_COL, phase)
        values.put(IS_COMPLETE_COL, isComplete)
        values.put(FINISH_DATE_COL, finishDate)
        values.put(CURRENT_GAME_ID_COL, currentGameID)

        // insert the new hunt into the database
        val result = db.insert(SHINY_HUNT_TABLE, null, values)
        if (result == -1L) {
            Log.e("DBHelper", "Failed to insert Hunt with formID: $formID")
        } else {
            Log.d("DBHelper", "Inserted Hunt with formID: $formID with ID: $result")
        }

        // close the database connection
        //db.close()
    }

    // Method to delete a shiny hunt from the database
    fun deleteHunt(huntID : Int) {
        Log.d("DBHelper", "deleteHunt() started")
        // create a writable variable of the database
        val db = this.writableDatabase
        Log.d("DBHelper", "Writable database created")

        // delete the hunt from the database
        val result = db.delete(SHINY_HUNT_TABLE, "huntID = ?", arrayOf(huntID.toString()))
        Log.d("DBHelper", "$result row(s) deleted from the database. Closing database connection")

        // close the database connection
        db.close()
    }

    // Method to update a hunt in the database
    fun updateHunt(huntID: Int, formID: Int?, originGameID: Int?, method: String, startDate: String?,
                   counter: Int, phase: Int, isComplete: Boolean, finishDate: String?, currentGameID: Int?) {
        Log.d("DBHelper", "updateHunt() started")
        // Create a writable variable of the database
        val db = this.writableDatabase
        Log.d("DBHelper", "Writable database created")

        // Call addHunt if huntID is 0 (meaning, the user is saving a new hunt)
        if (huntID == 0) {
            Log.d("DBHelper", "huntID is 0. Calling addHunt()")
            addHunt(db, formID, originGameID, method, startDate, counter, phase, isComplete, finishDate, currentGameID)
        }
        // Otherwise, continue with the update hunt process
        else {
            Log.d("DBHelper", "huntID is $huntID. Creating ContentValues for the updated row")
            // Store the counter in a values variable
            val values = ContentValues()

            // Insert the column values
            values.put(HUNT_ID_COL, huntID)
            values.put(FORM_ID_COL, formID)
            values.put(ORIGIN_GAME_ID_COL, originGameID)
            values.put(METHOD_COL, method)
            values.put(START_DATE_COL, startDate)
            values.put(COUNTER_COL, counter)
            values.put(PHASE_COL, phase)
            values.put(IS_COMPLETE_COL, isComplete)
            values.put(FINISH_DATE_COL, finishDate)
            values.put(CURRENT_GAME_ID_COL, currentGameID)

            // Specify the row to update (via the huntID)
            val selection = "$HUNT_ID_COL = ?"
            val selectionArgs = arrayOf(huntID.toString())

            // Update the table
            val result = db.update(SHINY_HUNT_TABLE, values, selection, selectionArgs)
            Log.d("DBHelper", "$result row(s) updated in the database. Closing database connection")

            // Close the database connection
            db.close()
        }
    }

    // Method to retrieve all shiny hunts from the database (by ID or all hunts if no ID is provided)
    fun getHunts(huntID: Int? = null): List<ShinyHunt> {
        Log.d("DBHelper", "getHunts() started")
        // Create a readable variable of the database
        val db = this.readableDatabase
        Log.d("DBHelper", "Readable database created")

        // Instantiate a mutable list for storing the shiny hunts
        val huntList = mutableListOf<ShinyHunt>()
        Log.d("DBHelper", "Mutable Shiny Hunt list created")

        //  Create variables for the query and the arguments
        val query: String
        val args: Array<String>?
        Log.d("DBHelper", "Empty query and args created")

        // Check if a huntID was provided
        if (huntID != null) {
            // ID provided - filter for that ID
            Log.d("DBHelper", "Hunt ID: $huntID")
            query = "SELECT * FROM $SHINY_HUNT_TABLE WHERE huntID = ?"
            args = arrayOf(huntID.toString())
        } else {
            // No ID provided - select all hunts
            Log.d("DBHelper", "No Hunt ID provided")
            query = "SELECT * FROM $SHINY_HUNT_TABLE ORDER BY huntID DESC"
            args = null
        }

        // Read shiny hunts from the database
        val huntData = db.rawQuery(query, args)
        Log.d("DBHelper", "Hunt data received")

        // Move the cursor to the first position
        if (huntData.moveToFirst()) {
            do {
                // Insert the current row into the hunt list
                huntList.add(
                    ShinyHunt(
                        huntData.getInt(0),                                                 // huntID
                        if ((huntData.isNull(1))) null else huntData.getInt(1),  // formID
                        if ((huntData.isNull(2))) null else huntData.getInt(2),  // originGameID
                        huntData.getString(3),                                              // method
                        huntData.getString(4),                                              // startDate
                        huntData.getInt(5),                                                 // counter
                        huntData.getInt(6),                                                 // phase
                        if ((huntData.getInt(7)) == 0 ) false else true,                    // isComplete
                        huntData.getString(8),                                              // finishDate
                        if ((huntData.isNull(9))) null else huntData.getInt(9)   // currentGameID
                    )
                )
            } while (huntData.moveToNext())
            // Move the cursor to the next position
        }

        // Close the cursor and return the hunt list
        huntData.close()
        return huntList
    }

    // Method to retrieve pokemon from the database (by pokemonID or all pokemon if no ID is provided)
    fun getPokemon(pokemonID: Int? = null, formID: Int? = null): List<Pokemon> {
        Log.d("DBHelper", "getPokemon() started")
        val db = this.readableDatabase
        Log.d("DBHelper", "Readable database opened")

        // Mutable map to store Pokémon and their forms
        val pokemonMap = mutableMapOf<Int, Pokemon>()

        // Construct query dynamically based on the parameters
        val query = StringBuilder("""
            SELECT p.$POKEMON_ID_COL, p.$POKEMON_NAME_COL, 
                   f.$FORM_ID_COL, f.$FORM_NAME_COL, f.$FORM_IMAGE_COL, f.$IS_DEFAULT_FORM_COL
            FROM $POKEMON_TABLE p
            LEFT JOIN $POKEMON_FORM_TABLE f ON p.$POKEMON_ID_COL = f.$POKEMON_ID_COL
        """)

        val args = mutableListOf<String>()

        // Apply filtering conditions
        if (pokemonID != null) {
            query.append(" WHERE p.$POKEMON_ID_COL = ?")
            args.add(pokemonID.toString())
        } else if (formID != null) {
            query.append(" WHERE f.$FORM_ID_COL = ?")
            args.add(formID.toString())
        }

        val cursor = db.rawQuery(query.toString(), if (args.isNotEmpty()) args.toTypedArray() else null)
        Log.d("DBHelper", "Query executed")

        // Process the result set
        if (cursor.moveToFirst()) {
            do {
                val pID = cursor.getInt(0)
                val pName = cursor.getString(1)

                // Ensure Pokémon is in the map
                val pokemon = pokemonMap.getOrPut(pID) {
                    Pokemon(pID, pName, mutableListOf())
                }

                // If form data is not null, add it to the Pokémon's forms list
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

        cursor.close()
        Log.d("DBHelper", "Cursor closed and Pokemon list generated")

        return pokemonMap.values.toList()
    }

    // Method to retrieve games from the database (by ID or all games if no ID is provided
    fun getGames(gameID: Int? = null): List<Game> {
        Log.d("DBHelper", "getGames() started")
        // Create a readable variable of the database
        val db = this.readableDatabase
        Log.d("DBHelper", "Readable database created")

        // Instantiate a mutable list for storing the games
        val gameList = mutableListOf<Game>()
        Log.d("DBHelper", "Mutable Game List instantiated")

        //  Create variables for the query and the arguments
        val query: String
        val args: Array<String>?
        Log.d("DBHelper", "Empty query and args created")

        // Check if a gameID was provided
        if (gameID != null) {
            // ID provided - filter for that ID
            Log.d("DBHelper", "Game ID: $gameID")
            query = "SELECT * FROM $GAME_TABLE WHERE gameID = ?"
            args = arrayOf(gameID.toString())
        } else {
            // No ID provided - select all games
            Log.d("DBHelper", "No Game ID. Selecting all games")
            query = "SELECT * FROM $GAME_TABLE"
            args = null
        }

        // Read all games from the database
        val gameData = db.rawQuery(query, args)
        Log.d("DBHelper", "Game data read")

        // Move the cursor to the first position
        if (gameData.moveToFirst()) {
            do {
                // Insert the current row into the game list
                gameList.add(
                    Game(
                        gameData.getInt(0),     // gameID
                        gameData.getString(1),  // gameName
                        gameData.getInt(2),     // gameImage
                        gameData.getInt(3)      // generation
                    )
                )
            } while (gameData.moveToNext())
            // Move the cursor to the next position
        }

        // Close the cursor and return the game list
        gameData.close()
        return gameList
    }

    // Database variables
    companion object{

        // Database name and version
        const val DATABASE_NAME = "SHINY_TRACKER_DB"
        const val DATABASE_VERSION = 3

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
    }
}