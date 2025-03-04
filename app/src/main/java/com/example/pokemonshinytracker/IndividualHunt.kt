package com.example.pokemonshinytracker

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Spinner
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

    // Declare variables for the back and save buttons
    lateinit var backBtn: Button
    lateinit var saveBtn: Button

    lateinit var detailLayout: LinearLayout

    // Declare variables for the pokemon selection dialog
    lateinit var selectPokemonBtn: Button
    lateinit var pokemonRecyclerView: RecyclerView
    lateinit var selectPokemonDialog: View

    // Declare variables for the start date button
    lateinit var pickStartDateBtn: Button
    lateinit var selectedStartDate: TextView

    // Declare variables for the origin game selection dialog
    lateinit var selectOriginGameBtn: Button
    lateinit var gameRecyclerView: RecyclerView
    lateinit var selectGameDialog: View

    // Declare variables for the counter buttons
    lateinit var decrementCounterBtn: Button
    lateinit var incrementCounterBtn: Button

    // Declare variables for the phase buttons
    lateinit var decrementPhaseBtn: Button
    lateinit var incrementPhaseBtn: Button

    // Declare variable for the hunt complete checkbox
    lateinit var completionCheckbox: CheckBox

    // Declare variables for the finish date button
    lateinit var pickFinishDateBtn: Button
    lateinit var selectedFinishDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_hunt)
        Log.d("IndividualHunt", "onCreate() started")

        // Declare a selected hunt variable (in case a pre-existing hunt was selected)
        var selectedHuntID = 0
        var selectedHunt: List<ShinyHunt> = emptyList()
        var selectedPokemonID = 0
        var selectedOriginGameID: Int? = 0

        // Access the database
        val db = DBHelper(this, null)
        Log.d("IndividualHunt", "Database opened")

        // Get list of Pokemon
        val pokemonList = db.getPokemon()
        Log.d("IndividualHunt", "Pokemon List: $pokemonList")

        // Extract the list of Pokemon names
        val pokemonNames = pokemonList.map { it.pokemonName }.toMutableList()
        Log.d("IndividualHunt", "Pokemon names extracted")

        // Replace "Default" in pokemonNames with "Select a Pokemon:"
        val defaultPokemonIndex = pokemonNames.indexOf("Default")
        if (defaultPokemonIndex != -1) {
            pokemonNames[defaultPokemonIndex] = "Select a Pokemon:"
            Log.d("IndividualHunt", "Default Pokemon Option: ${pokemonNames[defaultPokemonIndex]}")
        }

        // Get list of Games
        val gameList = db.getGames()
        Log.d("IndividualHunt", "Games List: $gameList")

        // Extract the list of Game names
        val gameNames = gameList.map { it.gameName }.toMutableList()
        Log.d("IndividualHunt", "Game names extracted")

        // Replace "Default" in gameNames with "Select a Game:"
        val defaultGameIndex = gameNames.indexOf("Default")
        if (defaultGameIndex != -1) {
            gameNames[defaultGameIndex] = "Select a Game:"
            Log.d("IndividualHunt", "Default Game Option: ${pokemonNames[defaultGameIndex]}")
        }

        // Access all the UI elements
        val mainLayout = findViewById<LinearLayout>(R.id.background)                // main layout (i.e. the background gradient)
        backBtn = findViewById(R.id.backButton)                                     // back button
        saveBtn = findViewById(R.id.saveButton)                                     // save button
        detailLayout = findViewById(R.id.individual_hunt_layout)                    // detail layout
        val selectedPokemonName = findViewById<TextView>(R.id.selected_pokemon_name)    // selected pokemon name
        val pokemonImage = findViewById<ImageView>(R.id.pokemonImage)               // pokemon image
        selectPokemonBtn = findViewById(R.id.pokemon_selection_button)              // pokemon select button
        pickStartDateBtn = findViewById(R.id.startDatePicker)                       // start date button
        selectedStartDate = findViewById(R.id.startDate)                            // start date text view
        val originGameIconBorder = findViewById<FrameLayout>(R.id.originGameIconBorder) // origin game icon border
        val originGameIcon = findViewById<ImageView>(R.id.originGameIcon)           // origin game icon
        selectOriginGameBtn = findViewById(R.id.originGameButton)                   // origin game select button
        val method = findViewById<EditText>(R.id.method)                            // method edit text
        val counter = findViewById<EditText>(R.id.counter)                          // counter edit text
        decrementCounterBtn = findViewById(R.id.decrementCounter)                   // counter decrement button
        incrementCounterBtn = findViewById(R.id.incrementCounter)                   // counter increment button
        val phase = findViewById<EditText>(R.id.phase)                              // phase edit text
        decrementPhaseBtn = findViewById(R.id.decrementPhase)                       // phase decrement button
        incrementPhaseBtn = findViewById(R.id.incrementPhase)                       // phase increment button
        completionCheckbox = findViewById(R.id.huntCompleteCheckbox)                // hunt completion checkbox
        val finishDateLayout = findViewById<LinearLayout>(R.id.finishDateLayout)    // finish date layout
        pickFinishDateBtn = findViewById(R.id.finishDatePicker)                     // finish date button
        selectedFinishDate = findViewById(R.id.finishDate)                          // finish date text view
        val currentGameLayout = findViewById<LinearLayout>(R.id.currentGameLayout)  // current game layout
        val currentGameSpinner = findViewById<Spinner>(R.id.currentGameSpinner)     // current game spinner (i.e. selector)
        Log.d("IndividualHunt", "Accessed all UI elements")

        // Retrieve data from the main window
        intent?.let {
            // Retrieve the selected hunt ID
            selectedHuntID = it.getIntExtra("hunt_id", 0)
            Log.d("IndividualHunt", "Received Hunt ID: $selectedHuntID")

            // Get the hunt from the database
            selectedHunt = db.getHunts(selectedHuntID)
            Log.d("IndividualHunt", "Hunt for ID $selectedHuntID: $selectedHunt")

            // Check if a hunt was received from the database (should be empty if huntID was 0)
            if (selectedHunt.isNotEmpty()) {
                val hunt = selectedHunt[0]  // Safe access
                Log.d("IndividualHunt", "Received Hunt: $hunt")
                selectedPokemonID = hunt.pokemonID
                selectedOriginGameID = hunt.originGameID

                // Update the pokemon name and image
                selectedPokemonName.text = pokemonList[hunt.pokemonID].pokemonName
                pokemonImage.setImageResource(pokemonList[hunt.pokemonID].pokemonImage)

                // Update the start date text (if not null)
                if (hunt.startDate != null) {
                    selectedStartDate.text = hunt.startDate
                }
                Log.d("IndividualHunt", "Start Date: ${selectedStartDate.text}")

                // Update the origin game icon
                originGameIcon.setImageResource(gameList[hunt.originGameID!!].gameImage)
                originGameIconBorder.visibility = View.VISIBLE

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

                // Update completion checkbox state
                completionCheckbox.isChecked = hunt.isComplete
                Log.d("IndividualHunt", "Completion Status: ${completionCheckbox.isChecked}")

                if (hunt.isComplete) {
                    mainLayout.setBackgroundResource(R.drawable.complete_hunt_gradient)
                    finishDateLayout.visibility = View.VISIBLE
                    currentGameLayout.visibility = View.VISIBLE
                    Log.d("IndividualHunt", "Displaying complete hunt layout")
                } else {
                    mainLayout.setBackgroundResource(R.drawable.incomplete_hunt_gradient)
                    finishDateLayout.visibility = View.INVISIBLE
                    currentGameLayout.visibility = View.INVISIBLE
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

        // Back button handling
        backBtn.setOnClickListener {
            Log.d("IndividualHunt", "Back button clicked. Returning to MainActivity window")

            // Return to the MainActivity window
            finish()
        }

        // Save button handling
        saveBtn.setOnClickListener {
            Log.d("IndividualHunt", "Save button clicked. Saving hunt to the database")

            // TODO: Implement the save hunt logic
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
                selectedFinishDate.text.toString(),
                currentGameSpinner.selectedItemPosition)

            // Return to MainActivity
            Log.d("IndividualActivity", "Returning to Main window")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close IndividualHunt activity
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
                { view, year, monthOfYear, dayOfMonth ->
                    // Format and set the text for the start date
                    selectedStartDate.text =
                        (buildString {
                            append(dayOfMonth.toString())
                            append("/")
                            append((monthOfYear + 1))
                            append("/")
                            append(year)
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
                .setTitle("Select an Origin Game")
                .setView(selectGameDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            gameRecyclerView.adapter = GameSelectionAdapter(this, groupedGameList) { selectedGame ->
                originGameIcon.setImageResource(selectedGame.gameImage)
                originGameIconBorder.visibility = View.VISIBLE
                selectedOriginGameID = selectedGame.gameID - 1
                dialog.dismiss()
            }
        }

        // Handle the decrement counter button
        decrementCounterBtn.setOnClickListener {
            var counterValue = counter.text.toString().toIntOrNull()
            // Decrement if value is greater than 0 and not null
            if (counterValue != null && counterValue > 0) {
                counter.setText(String.format((counterValue - 1).toString()))
            }
        }

        // Handle the increment counter button
        incrementCounterBtn.setOnClickListener {
            var counterValue = counter.text.toString().toIntOrNull()
            // Increment if value is not null
            if (counterValue != null) {
                counter.setText(String.format((counterValue + 1).toString()))
            }
        }

        // Handle the decrement phase button
        decrementPhaseBtn.setOnClickListener {
            var phaseValue = phase.text.toString().toIntOrNull()
            // Decrement if value is greater than 0 and not null
            if (phaseValue != null && phaseValue > 0) {
                phase.setText(String.format((phaseValue - 1).toString()))
            }
        }

        // Handle the increment phase button
        incrementPhaseBtn.setOnClickListener {
            var phaseValue = phase.text.toString().toIntOrNull()
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
                mainLayout.setBackgroundResource(R.drawable.incomplete_hunt_gradient)
                finishDateLayout.visibility = View.INVISIBLE
                currentGameLayout.visibility = View.INVISIBLE
            }
            // If checkboxState is true, change background to green gradient, and make layouts visible
            else {
                mainLayout.setBackgroundResource(R.drawable.complete_hunt_gradient)
                finishDateLayout.visibility = View.VISIBLE
                currentGameLayout.visibility = View.VISIBLE
            }
        }

        // Handle selection of the start date
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
                { view, year, monthOfYear, dayOfMonth ->
                    // on below line we are setting
                    // date to our text view.
                    selectedFinishDate.text =
                        (buildString {
                            append(dayOfMonth.toString())
                            append("/")
                            append((monthOfYear + 1))
                            append("/")
                            append(year)
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

        // Populate the Current Game spinner and handle selection
        if (currentGameSpinner != null) {
            Log.d("IndividualHunt", "Beginning Current Game Spinner population process")

            // Create an adapter for the Current Game spinner
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gameNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            currentGameSpinner.adapter = adapter
            Log.d("IndividualHunt", "Current Game Spinner adapter setup properly")

            // Set spinner selection if a current game is selected
            if (selectedHunt.isNotEmpty() && selectedHunt[0].currentGameID != null) {
                // Get the correct index (+1 index offset)
                val huntCurrentGameID = selectedHunt[0].currentGameID
                val selectedIndex = gameList.indexOfFirst { it.gameID == huntCurrentGameID } + 1

                if (selectedIndex != -1) {
                    Log.d(
                        "IndividualHunt",
                        "Setting spinner to index: $selectedIndex for Game ID: $huntCurrentGameID"
                    )
                    currentGameSpinner.setSelection(selectedIndex)
                } else {
                    Log.e("IndividualHunt", "Game ID not found in list: $huntCurrentGameID")
                }
            }

            // TODO: Handle Current Game selection
            currentGameSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
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
        val spanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5

        // Reinitialize GridLayoutManager with updated span count
        val gridLayoutManager = GridLayoutManager(this, spanCount)

        // Reset spanSizeLookup to ensure headers take up the full row
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((pokemonRecyclerView.adapter as? PokemonSelectionAdapter)?.getItemViewType(position)) {
                    PokemonSelectionAdapter.VIEW_TYPE_HEADER -> spanCount // Header spans full row
                    PokemonSelectionAdapter.VIEW_TYPE_POKEMON -> 1        // Pokémon takes 1 column
                    else -> 1 // Default fallback
                }
            }
        }

        // Apply the updated layout manager
        pokemonRecyclerView.layoutManager = gridLayoutManager
    }

}