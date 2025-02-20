package com.example.pokemonshinytracker

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // access the database
        val db = DBHelper(this, null)

        db.addHunt(6,"Articuno",R.drawable.shiny_articuno,"Ultra Sun","Soft Resets","",400,0,0,"","")
        db.addHunt(7,"Landorus",R.drawable.shiny_landorus,"Ultra Moon","Soft Resets","",400,0,0,"","")

        // get all of the saved shiny hunts
        val hunts = db.getHunts()

        // instantiate a custom adapter variable
        val customAdapter = CustomAdapter(this, hunts)

        // access the shiny hunts recycler view
        val recyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)

        // determine the number of columns based on orientation
        val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

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
                innerPadding.bottom)
            insets
        }

    }
}

class CustomAdapter(private val context : Context, private val dataSet: List<ShinyHunt>) :
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
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.shiny_hunt_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val hunt = dataSet[position]

        viewHolder.pokemonName.text = hunt.name
        viewHolder.pokemonImage.setImageResource(hunt.imageRes)
        viewHolder.pokemonCounter.text = hunt.counter.toString()
        viewHolder.background.setBackgroundResource( if (hunt.isComplete) R.drawable.complete_hunt_view else R.drawable.incomplete_hunt_view )

        // Click listener for each hunt item's increment button
        viewHolder.incrementButton.setOnClickListener {
            // increment the counter
            hunt.counter++
            viewHolder.pokemonCounter.text = hunt.counter.toString()

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.counter)
        }

        // Click listener for each hunt item's decrement button
        viewHolder.decrementButton.setOnClickListener {
            // decrement the counter (if greater than 0)
            if (hunt.counter > 0) {
                hunt.counter--
                viewHolder.pokemonCounter.text = hunt.counter.toString()
            }

            // update the database
            val db = DBHelper(context, null)
            db.updateHunt(hunt.huntID, hunt.counter)
        }

        // Click listener for each hunt item's layout
        viewHolder.background.setOnClickListener {
            // switch to the detailed view of the shiny hunt
            val intent = Intent(context, IndividualHunt::class.java).apply {
                putExtra("hunt_id", hunt.huntID)
                putExtra("pokemon_name", hunt.name)
                putExtra("pokemon_counter", hunt.counter)
                putExtra("hunt_complete", hunt.isComplete)
            }
            context.startActivity(intent)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}