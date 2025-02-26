package com.example.pokemonshinytracker

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    // Declare a variable for the new hunt button
    lateinit var newHuntBtn: Button

    // Declare a variable for the search button
    lateinit var searchBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate() started")

        // Access the database
        val db = DBHelper(this, null)
        Log.d("MainActivity", "Database opened")

        // Force update the database (for testing purposes)
        //db.forceUpgrade()
        //Log.d("MainActivity","Force updating the database")

        // get list of all pokemon
        val pokemonList = db.getPokemon()

        // get list of all games
        val gameList = db.getGames()

        // get all of the saved shiny hunts
        val hunts = db.getHunts()

        // Access the no hunts message and the shiny hunts recycler view
        val noHuntsMessage = findViewById<TextView>(R.id.no_hunts_message)
        val shinyHuntRecyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)

        // Instantiate adapter for the shiny hunt recycler view
        val shinyHuntListAdapter = ShinyHuntListAdapter(this, hunts, pokemonList, gameList)

        // Handle visibility of the no hunts message and the recycler view
        if (hunts.isEmpty()) {
            noHuntsMessage.visibility = View.VISIBLE
            shinyHuntRecyclerView.visibility = View.GONE
        } else {

            // determine the number of columns based on orientation
            val spanCount =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

            shinyHuntRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
            shinyHuntRecyclerView.adapter = shinyHuntListAdapter

            ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.shiny_hunts_recycler_view)
            ) { v, insets ->
                val innerPadding = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                    // If using EditText, also add
                    // "or WindowInsetsCompat.Type.ime()" to
                    // maintain focus when opening the IME
                )
                v.setPadding(
                    innerPadding.left,
                    innerPadding.top,
                    innerPadding.right,
                    innerPadding.bottom
                )
                insets
            }
        }

        // Handle the New Hunt button
        newHuntBtn = findViewById(R.id.new_hunt_button)

        newHuntBtn.setOnClickListener {
            Log.d("MainActivity", "New Hunt button clicked. Preparing to start a new hunt")

            // Set up an intent (with 0 for the hunt ID)
            val intent = Intent(this, IndividualHunt::class.java).apply {
                putExtra("hunt_id", 0)
            }
            Log.d("MainActivity", "Created intent for new hunt. Switching to Individual Hunt window")
            this.startActivity(intent)
        }

        // Handle the Search button
        searchBtn = findViewById(R.id.search_button)

        searchBtn.setOnClickListener {

        }

    }
}