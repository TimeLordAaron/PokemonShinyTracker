package com.example.pokemonshinytracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // Method to create the database
    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + HUNT_ID_COL + " INTEGER PRIMARY KEY, " +
                POKEMON_COL + " TEXT," +
                IMAGE_COL + " INTEGER," +
                ORIGIN_GAME_COL + " TEXT," +
                METHOD_COL + " TEXT," +
                START_DATE_COL + " TEXT," +
                COUNTER_COL + " INTEGER," +
                PHASE_COL + " INTEGER," +
                IS_COMPLETE_COL + " INTEGER," +
                COMPLETE_DATE_COL + " TEXT," +
                CURRENT_GAME_COL + " TEXT" + ")")

        db.execSQL(query)
    }

    // Method to upgrade the database when the version changes
    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // Method to add new shiny hunts to the database
    fun addHunt(huntID : Int, pokemon : String, image : Int, originGame : String?, method :String?, startDate : String?,
                counter : Int, phase : Int?, isComplete : Int, completeDate : String?, currentGame : String?){

        // content variable to store the column values
        val values = ContentValues()

        // insert the column values
        values.put(HUNT_ID_COL, huntID)
        values.put(POKEMON_COL, pokemon)
        values.put(IMAGE_COL, image)
        values.put(ORIGIN_GAME_COL, originGame)
        values.put(METHOD_COL, method)
        values.put(START_DATE_COL, startDate)
        values.put(COUNTER_COL, counter)
        values.put(PHASE_COL, phase)
        values.put(IS_COMPLETE_COL, isComplete)
        values.put(COMPLETE_DATE_COL, completeDate)
        values.put(CURRENT_GAME_COL, currentGame)

        // create a writable variable of the database
        val db = this.writableDatabase

        // insert the new hunt into the database
        db.insert(TABLE_NAME, null, values)

        // close the database connection
        db.close()
    }

    // Method to delete a shiny hunt from the database
    fun deleteHunt(huntID : Int) {

        // create a writable variable of the database
        val db = this.writableDatabase

        // delete the hunt from the database
        db.delete(TABLE_NAME, "huntID = ?", arrayOf(huntID.toString()))

        // close the database connection
        db.close()

    }

    // Method to update a hunt in the database (WIP: currently only updates the counter)
    fun updateHunt(huntID: Int, counter: Int) {

        // create a writable variable of the database
        val db = this.writableDatabase

        // store the counter in a values variable
        val values = ContentValues().apply {
            put(COUNTER_COL, counter)
        }

        // specify the row to update (via the huntID)
        val selection = "$HUNT_ID_COL = ?"
        val selectionArgs = arrayOf(huntID.toString())

        // update the table
        db.update(TABLE_NAME, values, selection, selectionArgs)

        // close the database connection
        db.close()
    }


    // Method to retrieve all saved shiny hunts from the database
    fun getHunts(): List<ShinyHunt> {

        // create a readable variable of the database
        val db = this.readableDatabase

        // read all shiny hunts from the database
        val huntsData = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

        // instantiate a mutable list for storing the shiny hunts
        val hunts = mutableListOf<ShinyHunt>()

        // move the cursor to the first position
        if (huntsData.moveToFirst()) {
            do {
                // insert the current db row into the hunts list
                hunts.add(
                    ShinyHunt(
                        huntsData.getInt(0),    // huntID
                        huntsData.getString(1), // name
                        huntsData.getInt(2),    // image
                        huntsData.getInt(6),    // counter
                        if ((huntsData.getInt(8)) == 0 ) false else true    // isComplete
                    )
                )
            } while (huntsData.moveToNext())
            // move the cursor to the next position
        }

        // close the cursor and return the hunts list
        huntsData.close()
        return hunts

    }

    // Database variables
    companion object{

        // database name and version
        private val DATABASE_NAME = "SHINY_TRACKER_DB"
        private val DATABASE_VERSION = 1

        // table name
        val TABLE_NAME = "ShinyHunt"

        // column names
        val HUNT_ID_COL = "huntID"
        val POKEMON_COL = "pokemon"
        val IMAGE_COL = "image"
        val ORIGIN_GAME_COL = "originGame"
        val METHOD_COL = "method"
        val START_DATE_COL = "startDate"
        val COUNTER_COL = "counter"
        val PHASE_COL = "phase"
        val IS_COMPLETE_COL = "isComplete"
        val COMPLETE_DATE_COL = "completeDate"
        val CURRENT_GAME_COL = "currentGame"
    }
}