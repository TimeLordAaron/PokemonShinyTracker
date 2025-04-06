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

    // lateinit UI declarations
    private lateinit var newHuntBtn: Button     // new hunt button
    private lateinit var searchBtn: Button      // search button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate() started")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // access the database
        val db = DBHelper(this, null)
        Log.d("MainActivity", "Database opened")

        // force update the database (for testing purposes)
        //db.forceUpgrade()
        //Log.d("MainActivity","Force updating the database")

        // retrieve relevant data from database
        val pokemonList = db.getPokemon()   // list of all pokemon
        if (pokemonList.isEmpty()) {
            Log.d("MainActivity", "Failed to retrieve pokemon from database")
        }
        val gameList = db.getGames()        // list of all games
        if (gameList.isEmpty()) {
            Log.d("MainActivity", "Failed to retrieve games from database")
        }
        val hunts = db.getHunts()           // list of all saved hunts
        Log.d("MainActivity", "Retrieved ${hunts.size} shiny hunts from database")

        // access the UI elements
        val noHuntsMessage = findViewById<TextView>(R.id.no_hunts_message)                      // message for when user has no saved hunts
        val shinyHuntRecyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)  // recycler view that displays the user's saved hunts
        newHuntBtn = findViewById(R.id.new_hunt_button)                                         // new hunt button
        searchBtn = findViewById(R.id.search_button)                                            // search button

        // instantiate an adapter for the shiny hunt recycler view
        val shinyHuntListAdapter = ShinyHuntListAdapter(this, hunts, pokemonList, gameList)

        // handle visibility of the no hunts message and the recycler view
        if (hunts.isEmpty()) {
            noHuntsMessage.visibility = View.VISIBLE
            shinyHuntRecyclerView.visibility = View.GONE
        } else {
            // determine the number of columns based on orientation
            val spanCount =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

            // apply the layout and adapter to the shiny hunt recycler view
            shinyHuntRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
            shinyHuntRecyclerView.adapter = shinyHuntListAdapter

            ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.shiny_hunts_recycler_view)
            ) { v, insets ->
                val innerPadding = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
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

        // on click listener for the new hunt button
        newHuntBtn.setOnClickListener {
            Log.d("MainActivity", "New Hunt button clicked. Preparing to create a new shiny hunt")

            // set up an intent (using 0 as a placeholder for the hunt ID)
            val intent = Intent(this, IndividualHunt::class.java).apply {
                putExtra("hunt_id", 0)
            }
            Log.d("MainActivity", "Created intent for a new hunt. Switching to IndividualHunt")

            // switch to IndividualHunt
            this.startActivity(intent)
        }

        // on click listener for the search button
        searchBtn.setOnClickListener {

        }

        Log.d("MainActivity", "onCreate() completed")
    }
}