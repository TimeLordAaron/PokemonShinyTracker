package com.example.pokemonshinytracker

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
        val recyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)

        // Instantiate a custom adapter for the recycler view
        val customAdapter = CustomAdapter(this, hunts, pokemonList, gameList)

        // Handle visibility of the no hunts message and the recycler view
        if (hunts.isEmpty()) {
            noHuntsMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {

            // determine the number of columns based on orientation
            val spanCount =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

            recyclerView.layoutManager = GridLayoutManager(this, spanCount)
            recyclerView.adapter = customAdapter

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
            val testDialog = Dialog(this)
            testDialog.setContentView(R.layout.individual_hunt)
            testDialog.show()
        }

    }
}

class CustomAdapter(private val context: Context, private val huntSet: List<ShinyHunt>, private val pokemonSet: List<Pokemon>, private val gameSet: List<Game>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout
        val pokemonName: TextView
        val originGameIconBorder: FrameLayout
        val originGameIcon: ImageView
        val currentGameIconBorder: FrameLayout
        val currentGameIcon: ImageView
        val pokemonImage: ImageView
        val counterValue: TextView
        val counterIncrementBtn: Button
        val counterDecrementBtn: Button

        init {
            // Define click listener for the ViewHolder's View
            background = view.findViewById(R.id.background)
            pokemonName = view.findViewById(R.id.pokemonName)
            originGameIconBorder = view.findViewById(R.id.originGameIconBorder)
            originGameIcon = view.findViewById(R.id.originGameIcon)
            currentGameIconBorder = view.findViewById(R.id.currentGameIconBorder)
            currentGameIcon = view.findViewById(R.id.currentGameIcon)
            pokemonImage = view.findViewById(R.id.pokemonImage)
            counterValue = view.findViewById(R.id.counterValue)
            counterIncrementBtn = view.findViewById(R.id.counterIncrement)
            counterDecrementBtn = view.findViewById(R.id.counterDecrement)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("MainActivity", "Creating shiny hunt View Holder")

        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.shiny_hunt_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Extract the shiny hunt from the huntSet
        val hunt = huntSet[position]

        Log.d("MainActivity", "Binding View Holder for shiny hunt: $hunt")
        viewHolder.background.setBackgroundResource( if (hunt.isComplete) R.drawable.complete_hunt_view else R.drawable.incomplete_hunt_view )
        viewHolder.pokemonName.text = pokemonSet[hunt.pokemonID].pokemonName
        if (hunt.originGameID != 0) {
            viewHolder.originGameIcon.setImageResource(gameSet[hunt.originGameID!!].gameImage)
            viewHolder.originGameIconBorder.visibility = View.VISIBLE
        } else {
            viewHolder.originGameIconBorder.visibility = View.GONE
        }
        if (hunt.currentGameID != 0) {
            viewHolder.currentGameIcon.setImageResource(gameSet[hunt.currentGameID!!].gameImage)
            viewHolder.currentGameIconBorder.visibility = View.VISIBLE
        } else {
            viewHolder.currentGameIconBorder.visibility = View.GONE
        }
        viewHolder.pokemonImage.setImageResource(pokemonSet[hunt.pokemonID].pokemonImage)
        viewHolder.counterValue.text = hunt.counter.toString()

        // Click listener for each hunt item's counter increment button
        viewHolder.counterIncrementBtn.setOnClickListener {
            Log.d("MainActivity", "Increment counter button clicked for shiny hunt: $hunt")
            // increment the counter
            hunt.counter++
            viewHolder.counterValue.text = hunt.counter.toString()

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.pokemonID, hunt.originGameID, hunt.method, hunt.startDate,
            hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // Click listener for each hunt item's counter decrement button
        viewHolder.counterDecrementBtn.setOnClickListener {
            Log.d("MainActivity", "Decrement counter button clicked for shiny hunt: $hunt")
            // decrement the counter (if greater than 0)
            if (hunt.counter > 0) {
                hunt.counter--
                viewHolder.counterValue.text = hunt.counter.toString()
            }

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.pokemonID, hunt.originGameID, hunt.method, hunt.startDate,
                hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // Click listener for each hunt item's layout
        viewHolder.background.setOnClickListener {
            Log.d("MainActivity", "Clicked shiny hunt: $hunt")
            // switch to the detailed view of the shiny hunt
            val intent = Intent(context, IndividualHunt::class.java).apply {
                putExtra("hunt_id", hunt.huntID)
            }
            Log.d("MainActivity", "Created intent for selected hunt. Switching to Individual Hunt window")
            context.startActivity(intent)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = huntSet.size

}