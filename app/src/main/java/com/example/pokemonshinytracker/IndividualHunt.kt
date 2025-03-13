package com.example.pokemonshinytracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class IndividualHunt : ComponentActivity() {

    // Lateinit UI declarations
    private lateinit var backBtn: Button                    // back button
    private lateinit var saveBtn: Button                    // save button
    private lateinit var deleteBtn: Button                  // delete button
    private lateinit var detailLayout: LinearLayout         // detail layout
    private lateinit var selectPokemonBtn: Button           // pokemon selection button
    private lateinit var pokemonRecyclerView: RecyclerView  // pokemon recycler view
    private lateinit var selectPokemonDialog: View          // pokemon selection dialog
    private lateinit var pickStartDateBtn: Button           // start date button
    private lateinit var selectedStartDate: TextView        // start date text
    private lateinit var selectOriginGameBtn: Button        // origin game button
    private lateinit var gameRecyclerView: RecyclerView     // game recycler view (used for origin game and current game)
    private lateinit var selectGameDialog: View             // game dialog (used for origin game and current game)
    private lateinit var decrementCounterBtn: Button        // decrement counter button
    private lateinit var incrementCounterBtn: Button        // increment counter button
    private lateinit var decrementPhaseBtn: Button          // decrement phase button
    private lateinit var incrementPhaseBtn: Button          // increment phase button
    private lateinit var completionCheckbox: CheckBox       // completion checkbox
    private lateinit var pickFinishDateBtn: Button          // finish date button
    private lateinit var selectedFinishDate: TextView       // finish date text
    private lateinit var selectCurrentGameBtn: Button       // current game button

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("IndividualHunt", "onCreate() started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_hunt)

        // Declare variables for the selected hunt (in case the user opened a saved hunt)
        var selectedHuntID = 0
        var selectedHunt: List<ShinyHunt>
        var selectedPokemonID = 0
        var selectedOriginGameID: Int? = null
        var selectedCurrentGameID: Int? = null

        // Access the database
        val db = DBHelper(this, null)
        Log.d("IndividualHunt", "Database opened")

        // Retrieve relevant data from database
        val pokemonList = db.getPokemon()   // list of all pokemon
        if (pokemonList.isEmpty()) {
            Log.d("IndividualHunt", "Failed to retrieve pokemon from database")
        }
        val gameList = db.getGames()        // list of all games
        if (gameList.isEmpty()) {
            Log.d("IndividualHunt", "Failed to retrieve games from database")
        }

        // Access all the UI elements
        val mainLayout = findViewById<LinearLayout>(R.id.individual_hunt_background)        // background of the entire window
        backBtn = findViewById(R.id.back_button)                                            // back button
        saveBtn = findViewById(R.id.save_button)                                            // save button
        deleteBtn = findViewById(R.id.delete_button)                                        // delete button
        detailLayout = findViewById(R.id.individual_hunt_details)                           // detail layout
        val selectedPokemonName = findViewById<TextView>(R.id.selected_pokemon_name)        // selected pokemon name
        val pokemonImage = findViewById<ImageView>(R.id.pokemon_image)                      // pokemon image
        selectPokemonBtn = findViewById(R.id.pokemon_selection_button)                      // pokemon select button
        selectedStartDate = findViewById(R.id.start_date)                                   // start date text view
        pickStartDateBtn = findViewById(R.id.start_date_button)                             // start date button
        val originGameIconBorder = findViewById<FrameLayout>(R.id.origin_game_icon_border)  // origin game icon border
        val originGameIcon = findViewById<ImageView>(R.id.origin_game_icon)                 // origin game icon
        val originGameName = findViewById<TextView>(R.id.origin_game_name)                  // origin game name
        selectOriginGameBtn = findViewById(R.id.origin_game_button)                         // origin game select button
        val method = findViewById<EditText>(R.id.method)                                    // method edit text
        val counter = findViewById<EditText>(R.id.counter)                                  // counter edit text
        decrementCounterBtn = findViewById(R.id.decrement_counter_button)                   // counter decrement button
        incrementCounterBtn = findViewById(R.id.increment_counter_button)                   // counter increment button
        val phase = findViewById<EditText>(R.id.phase)                                      // phase edit text
        decrementPhaseBtn = findViewById(R.id.decrement_phase_button)                       // phase decrement button
        incrementPhaseBtn = findViewById(R.id.increment_phase_button)                       // phase increment button
        completionCheckbox = findViewById(R.id.hunt_complete_checkbox)                      // hunt completion checkbox
        val finishDateLayout = findViewById<LinearLayout>(R.id.finish_date_layout)          // finish date layout
        pickFinishDateBtn = findViewById(R.id.finish_date_button)                           // finish date button
        selectedFinishDate = findViewById(R.id.finish_date)                                 // finish date text view
        val currentGameLayout = findViewById<LinearLayout>(R.id.current_game_layout)        // current game layout
        val currentGameIconBorder = findViewById<FrameLayout>(R.id.current_game_icon_border)// current game icon border
        val currentGameIcon = findViewById<ImageView>(R.id.current_game_icon)               // current game icon
        val currentGameName = findViewById<TextView>(R.id.current_game_name)                // current game name
        selectCurrentGameBtn = findViewById(R.id.current_game_button)                       // current game select button
        Log.d("IndividualHunt", "Accessed all UI elements")

        // Retrieve data from the main window
        intent?.let {
            selectedHuntID = it.getIntExtra("hunt_id", 0)           // hunt ID of the retrieved hunt (or 0 if new hunt)
            Log.d("IndividualHunt", "Received Hunt ID: $selectedHuntID")

            // Retrieve the hunt from the database using the hunt ID
            selectedHunt = db.getHunts(selectedHuntID)
            Log.d("IndividualHunt", "Hunt for ID $selectedHuntID: $selectedHunt")

            // Check if a hunt was received from the database (should be empty if huntID was 0)
            if (selectedHunt.isNotEmpty()) {
                val hunt = selectedHunt[0]  // Safe access
                Log.d("IndividualHunt", "Received Hunt: $hunt")
                selectedPokemonID = hunt.pokemonID
                selectedOriginGameID = hunt.originGameID
                selectedCurrentGameID = hunt.currentGameID

                // Enable the delete button
                deleteBtn.visibility = View.VISIBLE

                // Update the pokemon name and image
                selectedPokemonName.text = pokemonList[hunt.pokemonID].pokemonName
                pokemonImage.setImageResource(pokemonList[hunt.pokemonID].pokemonImage)

                // Update the start date text (if not null)
                if (hunt.startDate != null) {
                    selectedStartDate.text = hunt.startDate
                }
                Log.d("IndividualHunt", "Start Date: ${selectedStartDate.text}")

                // Update the origin game icon and name
                if (hunt.originGameID != null) {
                    originGameIcon.setImageResource(gameList[hunt.originGameID!!].gameImage)
                    originGameIconBorder.visibility = View.VISIBLE
                    originGameName.text = gameList[hunt.originGameID!!].gameName
                }

                // Update the method text
                method.setText(hunt.method)
                Log.d("IndividualHunt", "Method: ${method.text}")

                // Update counter
                counter.setText(hunt.counter.toString())
                Log.d("IndividualHunt", "Counter: ${counter.text}")

                // Update phase
                phase.setText(hunt.phase.toString())
                Log.d("IndividualHunt", "Phase: ${phase.text}")

                // Update the finish date text (if not null)
                if (hunt.finishDate != null) {
                    selectedFinishDate.text = hunt.finishDate
                }
                Log.d("IndividualHunt", "Finish Date: ${selectedFinishDate.text}")

                // Update the current game icon and name
                if (hunt.currentGameID != null) {
                    currentGameIcon.setImageResource(gameList[hunt.currentGameID!!].gameImage)
                    currentGameIconBorder.visibility = View.VISIBLE
                    currentGameName.text = gameList[hunt.currentGameID!!].gameName
                }

                // Update completion checkbox state
                completionCheckbox.isChecked = hunt.isComplete
                Log.d("IndividualHunt", "Completion Status: ${completionCheckbox.isChecked}")

                if (hunt.isComplete) {
                    mainLayout.setBackgroundResource(R.drawable.ui_gradient_complete_hunt)
                    finishDateLayout.visibility = View.VISIBLE
                    currentGameLayout.visibility = View.VISIBLE
                    Log.d("IndividualHunt", "Displaying complete hunt layout")
                } else {
                    mainLayout.setBackgroundResource(R.drawable.ui_gradient_incomplete_hunt)
                    finishDateLayout.visibility = View.GONE
                    currentGameLayout.visibility = View.GONE
                    Log.d("IndividualHunt", "Displaying incomplete hunt layout")
                }

            } else {
                Log.e("IndividualHunt", "No hunt found for ID: $selectedHuntID")
            }
        }

        // Handle the UI layout based on orientation
        detailLayout.orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

        selectPokemonDialog = layoutInflater.inflate(R.layout.pokemon_selection, null)
        pokemonRecyclerView = selectPokemonDialog.findViewById(R.id.pokemon_recycler_view)
        selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
        gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

        // Back button handling
        backBtn.setOnClickListener {
            Log.d("IndividualHunt", "Back button clicked. Returning to MainActivity window")

            // Return to the MainActivity window
            finish()
        }

        // Save button handling
        saveBtn.setOnClickListener {
            Log.d("IndividualHunt", "Save button clicked. Saving hunt to the database")

            // Call updateHunt with the values selected by the user
            db.updateHunt(
                selectedHuntID,
                selectedPokemonID,
                selectedOriginGameID,
                method.text.toString(),
                selectedStartDate.text.toString(),
                counter.text.toString().toInt(),
                phase.text.toString().toInt(),
                completionCheckbox.isChecked,
                if (completionCheckbox.isChecked) selectedFinishDate.text.toString() else null,
                if (completionCheckbox.isChecked) selectedCurrentGameID else null
            )

            // Return to MainActivity
            Log.d("IndividualActivity", "Returning to Main window")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close IndividualHunt activity
        }

        // Delete button handling
        deleteBtn.setOnClickListener {
            Log.d("IndividualHunt", "Delete button clicked. Showing confirmation dialog")

            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this shiny hunt? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    // User confirmed deletion
                    Log.d("IndividualHunt", "User confirmed deletion. Deleting hunt from the database")

                    // Call deleteHunt with the selectedHuntID as the parameter
                    db.deleteHunt(selectedHuntID)

                    // Return to MainActivity
                    Log.d("IndividualActivity", "Returning to Main window")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // Close IndividualHunt activity
                }
                .setNegativeButton("No") { dialog, _ ->
                    // User canceled deletion
                    Log.d("IndividualHunt", "User canceled deletion")
                    dialog.dismiss()
                }
                .show()
        }


        // Handle the pokemon select button
        selectPokemonBtn.setOnClickListener {
            val selectPokemonDialog = layoutInflater.inflate(R.layout.pokemon_selection, null)
            pokemonRecyclerView = selectPokemonDialog.findViewById(R.id.pokemon_recycler_view)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5
            pokemonRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            // Prepare dataset with headers
            val groupedPokemonList = preparePokemonListWithHeaders(pokemonList)

            // Custom span size logic to make headers span full width
            (pokemonRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedPokemonList[position]) {
                        is PokemonListItem.HeaderItem -> spanCount  // Header takes full row
                        is PokemonListItem.PokemonItem -> 1        // Pokémon takes 1 column
                    }
                }
            }

            // Create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select a Pokémon")
                .setView(selectPokemonDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            pokemonRecyclerView.adapter = PokemonSelectionAdapter(this, groupedPokemonList) { selectedPokemon ->
                pokemonImage.setImageResource(selectedPokemon.pokemonImage)
                selectedPokemonName.text = selectedPokemon.pokemonName
                selectedPokemonID = selectedPokemon.pokemonID - 1
                dialog.dismiss()
            }
        }

        // Handle selection of the start date
        pickStartDateBtn.setOnClickListener {
            Log.d("IndividualHunt", "Handling selection of the start date")

            // Create a calendar instance
            val c = Calendar.getInstance()
            Log.d("IndividualHunt", "Calendar instance created")

            // Get the day, month, and year from the calendar
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a dialog for the date picker
            Log.d("IndividualHunt", "Creating date picker dialog")
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format and set the text for the start date
                    selectedStartDate.text =
                        (buildString {
                            append(selectedDay.toString())
                            append("/")
                            append((selectedMonth + 1))
                            append("/")
                            append(selectedYear)
                        })
                },
                // Pass the year, month, and day for the selected date
                year,
                month,
                day
            )
            Log.d("IndividualHunt", "Selected Start Date: ${selectedStartDate.text}")

            // Display the date picker dialog
            datePickerDialog.show()
        }

        // Handle clicking the origin game icon (i.e. de-select the origin game)
        originGameIcon.setOnClickListener {
            originGameIconBorder.visibility = View.INVISIBLE
            originGameName.text = ""
            selectedOriginGameID = null
        }

        // Handle the origin game select button
        selectOriginGameBtn.setOnClickListener {
            val selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
            gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
            gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            // Prepare dataset with headers
            val groupedGameList = prepareGameListWithHeaders(gameList)

            // Custom span size logic to make headers span full width
            (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedGameList[position]) {
                        is GameListItem.HeaderItem -> spanCount  // Header takes full row
                        is GameListItem.GameItem -> 1        // Game takes 1 column
                    }
                }
            }

            // Create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select the Origin Game")
                .setView(selectGameDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            gameRecyclerView.adapter = GameSelectionAdapter(this, 0, groupedGameList) { selectedGame ->
                originGameIcon.setImageResource(selectedGame.gameImage)
                originGameIconBorder.visibility = View.VISIBLE
                originGameName.text = selectedGame.gameName
                selectedOriginGameID = selectedGame.gameID - 1
                dialog.dismiss()
            }
        }

        // Handle the decrement counter button
        decrementCounterBtn.setOnClickListener {
            val counterValue = counter.text.toString().toIntOrNull()
            // Decrement if value is greater than 0 and not null
            if (counterValue != null && counterValue > 0) {
                counter.setText(String.format((counterValue - 1).toString()))
            }
        }

        // Handle the increment counter button
        incrementCounterBtn.setOnClickListener {
            val counterValue = counter.text.toString().toIntOrNull()
            // Increment if value is not null
            if (counterValue != null) {
                counter.setText(String.format((counterValue + 1).toString()))
            }
        }

        // Handle the decrement phase button
        decrementPhaseBtn.setOnClickListener {
            val phaseValue = phase.text.toString().toIntOrNull()
            // Decrement if value is greater than 0 and not null
            if (phaseValue != null && phaseValue > 0) {
                phase.setText(String.format((phaseValue - 1).toString()))
            }
        }

        // Handle the increment phase button
        incrementPhaseBtn.setOnClickListener {
            val phaseValue = phase.text.toString().toIntOrNull()
            // Increment if value is not null
            if (phaseValue != null) {
                phase.setText(String.format((phaseValue + 1).toString()))
            }
        }

        // Handle the checkbox
        completionCheckbox.setOnClickListener {
            // Get the state of the checkbox
            val checkboxState = completionCheckbox.isChecked
            // If checkboxState is false, change background to gray gradient, and make layouts invisible
            if (!checkboxState) {
                mainLayout.setBackgroundResource(R.drawable.ui_gradient_incomplete_hunt)
                finishDateLayout.visibility = View.GONE
                currentGameLayout.visibility = View.GONE
            }
            // If checkboxState is true, change background to green gradient, and make layouts visible
            else {
                mainLayout.setBackgroundResource(R.drawable.ui_gradient_complete_hunt)
                finishDateLayout.visibility = View.VISIBLE
                currentGameLayout.visibility = View.VISIBLE
            }
        }

        // Handle selection of the finish date
        pickFinishDateBtn.setOnClickListener {
            // on below line we are getting
            // the instance of our calendar.
            val c = Calendar.getInstance()

            // on below line we are getting
            // our day, month and year.
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // on below line we are creating a
            // variable for date picker dialog.
            val datePickerDialog = DatePickerDialog(
                // on below line we are passing context.
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // on below line we are setting
                    // date to our text view.
                    selectedFinishDate.text =
                        (buildString {
                            append(selectedDay.toString())
                            append("/")
                            append((selectedMonth + 1))
                            append("/")
                            append(selectedYear)
                        })
                },
                // on below line we are passing year, month
                // and day for the selected date in our date picker.
                year,
                month,
                day
            )
            // at last we are calling show
            // to display our date picker dialog.
            datePickerDialog.show()
        }

        // Handle clicking the current game icon (i.e. de-select the current game)
        currentGameIcon.setOnClickListener {
            currentGameIconBorder.visibility = View.INVISIBLE
            currentGameName.text = ""
            selectedCurrentGameID = null
        }

        // Handle the current game select button
        selectCurrentGameBtn.setOnClickListener {
            val selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
            gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
            gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            // Prepare dataset with headers
            val groupedGameList = prepareGameListWithHeaders(gameList)

            // Custom span size logic to make headers span full width
            (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedGameList[position]) {
                        is GameListItem.HeaderItem -> spanCount  // Header takes full row
                        is GameListItem.GameItem -> 1        // Game takes 1 column
                    }
                }
            }

            // Create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select the Current Game")
                .setView(selectGameDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            gameRecyclerView.adapter = GameSelectionAdapter(this, 1, groupedGameList) { selectedGame ->
                currentGameIcon.setImageResource(selectedGame.gameImage)
                currentGameIconBorder.visibility = View.VISIBLE
                currentGameName.text = selectedGame.gameName
                selectedCurrentGameID = selectedGame.gameID - 1
                dialog.dismiss()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Update layout orientation dynamically
        detailLayout.orientation =
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                LinearLayout.HORIZONTAL
            else
                LinearLayout.VERTICAL


        // Determine the number of columns based on orientation
        val pokemonSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5
        val gameSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3

        // Reinitialize GridLayoutManagers with updated span counts
        val pokemonGridLayoutManager = GridLayoutManager(this, pokemonSpanCount)
        val gameGridLayoutManager = GridLayoutManager(this, gameSpanCount)

        // Reset spanSizeLookup to ensure headers take up the full row
        pokemonGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((pokemonRecyclerView.adapter as? PokemonSelectionAdapter)?.getItemViewType(position)) {
                    PokemonSelectionAdapter.VIEW_TYPE_HEADER -> pokemonSpanCount // Header spans full row
                    PokemonSelectionAdapter.VIEW_TYPE_POKEMON -> 1        // Pokémon takes 1 column
                    else -> 1 // Default fallback
                }
            }
        }

        gameGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((gameRecyclerView.adapter as? GameSelectionAdapter)?.getItemViewType(position)) {
                    GameSelectionAdapter.VIEW_TYPE_HEADER -> gameSpanCount // Header spans full row
                    GameSelectionAdapter.VIEW_TYPE_GAME -> 1        // Game takes 1 column
                    else -> 1 // Default fallback
                }
            }
        }

        // Apply the updated layout managers
        pokemonRecyclerView.layoutManager = pokemonGridLayoutManager
        gameRecyclerView.layoutManager = gameGridLayoutManager
    }

}