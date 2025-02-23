package com.example.pokemonshinytracker

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        // get all of the saved shiny hunts
        val hunts = db.getHunts()

        // Access the no hunts message and the shint hunts recycler view
        val noHuntsMessage = findViewById<TextView>(R.id.no_hunts_message)
        val recyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)

        // Instantiate a custom adapter for the recycler view
        val customAdapter = CustomAdapter(this, hunts, pokemonList)

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

    }
}

class CustomAdapter(private val context: Context, private val huntSet: List<ShinyHunt>, private val pokemonSet: List<Pokemon>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pokemonName: TextView
        val pokemonImage: ImageView
        val pokemonCounter: TextView
        val background: LinearLayout
        val incrementButton: Button
        val decrementButton: Button

        init {
            // Define click listener for the ViewHolder's View
            pokemonName = view.findViewById(R.id.pokemonName)
            pokemonImage = view.findViewById(R.id.pokemonImage)
            pokemonCounter = view.findViewById(R.id.pokemonCounter)
            background = view.findViewById(R.id.background)
            incrementButton = view.findViewById(R.id.incrementButton)
            decrementButton = view.findViewById(R.id.decrementButton)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("MainActivity", "Creating View Holder for shiny hunts")

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
        viewHolder.pokemonName.text = pokemonSet[hunt.pokemonID].pokemonName
        viewHolder.pokemonImage.setImageResource(pokemonSet[hunt.pokemonID].pokemonImage)
        viewHolder.pokemonCounter.text = hunt.counter.toString()
        viewHolder.background.setBackgroundResource( if (hunt.isComplete) R.drawable.complete_hunt_view else R.drawable.incomplete_hunt_view )

        // Click listener for each hunt item's increment button
        viewHolder.incrementButton.setOnClickListener {
            Log.d("MainActivity", "Increment button clicked for shiny hunt: $hunt")
            // increment the counter
            hunt.counter++
            viewHolder.pokemonCounter.text = hunt.counter.toString()

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.pokemonID, hunt.originGameID, hunt.method, hunt.startDate,
            hunt.counter, hunt.phase, hunt.isComplete, hunt.finishDate, hunt.currentGameID)
        }

        // Click listener for each hunt item's decrement button
        viewHolder.decrementButton.setOnClickListener {
            Log.d("MainActivity", "Decrement button clicked for shiny hunt: $hunt")
            // decrement the counter (if greater than 0)
            if (hunt.counter > 0) {
                hunt.counter--
                viewHolder.pokemonCounter.text = hunt.counter.toString()
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