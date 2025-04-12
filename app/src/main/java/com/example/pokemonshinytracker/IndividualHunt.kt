package com.example.pokemonshinytracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class IndividualHunt : ComponentActivity() {

    // lateinit UI var declarations
    private lateinit var backBtn: Button                    // back button
    private lateinit var saveBtn: Button                    // save button
    private lateinit var deleteBtn: Button                  // delete button
    private lateinit var detailLayout: LinearLayout         // detail layout
    private lateinit var previousFormBtn: Button            // previous form button
    private lateinit var nextFormBtn: Button                // next form button
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

        // variable declarations for the selected hunt
        var selectedHuntID = 0
        var selectedHunt: List<ShinyHunt>
        var selectedPokemonID: Int? = null
        var selectedFormID: Int? = null
        var selectedOriginGameID: Int? = null
        var selectedCurrentGameID: Int? = null
        var selectedDefaultPosition: Int? = null

        // access the database
        val db = DBHelper(this, null)
        Log.d("IndividualHunt", "Database opened")

        // retrieve relevant data from database
        val pokemonList = db.getPokemon()   // list of all pokemon
        if (pokemonList.isEmpty()) {
            Log.d("IndividualHunt", "Failed to retrieve Pokemon from the database")
        }
        val gameList = db.getGames()        // list of all games
        if (gameList.isEmpty()) {
            Log.d("IndividualHunt", "Failed to retrieve games from database")
        }

        // find all the UI views
        val mainLayout = findViewById<LinearLayout>(R.id.individual_hunt_background)        // background of the entire window
        backBtn = findViewById(R.id.back_button)                                            // back button
        saveBtn = findViewById(R.id.save_button)                                            // save button
        deleteBtn = findViewById(R.id.delete_button)                                        // delete button
        detailLayout = findViewById(R.id.individual_hunt_details)                           // detail layout
        previousFormBtn = findViewById(R.id.previous_form_button)                           // previous form button
        nextFormBtn = findViewById(R.id.next_form_button)                                   // next form button
        val selectedPokemonName = findViewById<TextView>(R.id.selected_pokemon_name)        // selected pokemon name
        val selectedPokemonForm = findViewById<TextView>(R.id.selected_pokemon_form)        // selected pokemon form
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

        // retrieve data from the main window
        intent?.let { it ->
            selectedHuntID = it.getIntExtra("hunt_id", 0)           // hunt ID of the retrieved hunt (or 0 if new hunt)
            Log.d("IndividualHunt", "Received Hunt ID: $selectedHuntID")

            // retrieve the hunt from the database using the hunt ID
            selectedHunt = db.getHunts(selectedHuntID)
            Log.d("IndividualHunt", "Hunt for ID $selectedHuntID: $selectedHunt")

            // check if a hunt was received from the database (should be empty if huntID was 0)
            if (selectedHunt.isNotEmpty()) {
                val hunt = selectedHunt[0]  // extracting the hunt from the returned list
                Log.d("IndividualHunt", "Received Hunt: $hunt")
                selectedFormID = hunt.formID
                Log.d("IndividualHunt", "Form ID: $selectedFormID")
                val pokemon = pokemonList.find { p -> p.forms.any { it.formID == selectedFormID } }!!
                val formName = pokemon.forms.find { it.formID == hunt.formID }!!.formName
                val formImage = pokemon.forms.find { it.formID == hunt.formID }!!.formImage
                Log.d("IndividualHunt", "Received Pokemon: $pokemon")
                if (selectedFormID != null) {
                    selectedPokemonID = pokemon.pokemonID
                    if (pokemon.forms.size > 1) {
                        previousFormBtn.visibility = View.VISIBLE
                        nextFormBtn.visibility = View.VISIBLE
                    }
                }
                Log.d("IndividualHunt", "Pokemon ID: $selectedPokemonID")
                selectedOriginGameID = hunt.originGameID
                Log.d("IndividualHunt", "Origin Game ID: $selectedOriginGameID")
                selectedCurrentGameID = hunt.currentGameID
                Log.d("IndividualHunt", "Current Game ID: $selectedCurrentGameID")
                selectedDefaultPosition = hunt.defaultPosition
                Log.d("IndividualHunt", "Default Position: $selectedDefaultPosition")

                // enable the delete button
                deleteBtn.visibility = View.VISIBLE

                // update the Pokemon name, form, and image
                if (selectedFormID != null) {
                    selectedPokemonName.text = pokemon.pokemonName
                    if (formName != null) {
                        selectedPokemonForm.text = "($formName)"
                    } else {
                        selectedPokemonForm.text = ""
                    }
                    pokemonImage.setImageResource(formImage)
                }

                // update the start date text (if not null)
                if (hunt.startDate != null) {
                    selectedStartDate.text = hunt.startDate
                }
                Log.d("IndividualHunt", "Start Date: ${selectedStartDate.text}")

                // update the origin game icon and name
                if (hunt.originGameID != null) {
                    originGameIcon.setImageResource(gameList[hunt.originGameID!!].gameImage)
                    originGameIconBorder.visibility = View.VISIBLE
                    originGameName.text = gameList[hunt.originGameID!!].gameName
                }

                // update the method text
                method.setText(hunt.method)
                Log.d("IndividualHunt", "Method: ${method.text}")

                // update counter
                counter.setText(hunt.counter.toString())
                Log.d("IndividualHunt", "Counter: ${counter.text}")

                // update phase
                phase.setText(hunt.phase.toString())
                Log.d("IndividualHunt", "Phase: ${phase.text}")

                // update the finish date text (if not null)
                if (hunt.finishDate != null) {
                    selectedFinishDate.text = hunt.finishDate
                }
                Log.d("IndividualHunt", "Finish Date: ${selectedFinishDate.text}")

                // update the current game icon and name
                if (hunt.currentGameID != null) {
                    currentGameIcon.setImageResource(gameList[hunt.currentGameID!!].gameImage)
                    currentGameIconBorder.visibility = View.VISIBLE
                    currentGameName.text = gameList[hunt.currentGameID!!].gameName
                }

                // update completion checkbox state
                completionCheckbox.isChecked = hunt.isComplete
                Log.d("IndividualHunt", "Completion Status: ${completionCheckbox.isChecked}")

                // update the background gradient
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
                Log.d("IndividualHunt", "No hunt found for ID: $selectedHuntID. This is a new hunt")
            }
        }

        // handle the UI layout based on device orientation
        detailLayout.orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

        // prepare the recycler views
        selectPokemonDialog = layoutInflater.inflate(R.layout.pokemon_selection, null)
        pokemonRecyclerView = selectPokemonDialog.findViewById(R.id.pokemon_recycler_view)
        selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
        gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

        // on click listener for the back button
        backBtn.setOnClickListener {
            Log.d("IndividualHunt", "Back button clicked. Returning to MainActivity window")

            // return to the MainActivity window
            finish()
        }

        // on click listener for the save button
        saveBtn.setOnClickListener {
            Log.d("IndividualHunt", "Save button clicked. Saving hunt to the database")

            // call updateHunt with the values selected by the user
            db.updateHunt(
                selectedHuntID,
                selectedFormID,
                selectedOriginGameID,
                method.text.toString(),
                selectedStartDate.text.toString(),
                counter.text.toString().toInt(),
                phase.text.toString().toInt(),
                completionCheckbox.isChecked,
                if (completionCheckbox.isChecked) selectedFinishDate.text.toString() else null,
                if (completionCheckbox.isChecked) selectedCurrentGameID else null,
                selectedDefaultPosition
            )

            // return to MainActivity
            Log.d("IndividualActivity", "Returning to Main window")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // close IndividualHunt activity
        }

        // on click listener for the delete button
        deleteBtn.setOnClickListener {
            Log.d("IndividualHunt", "Delete button clicked. Showing confirmation dialog")

            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this shiny hunt? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    // user confirmed deletion
                    Log.d("IndividualHunt", "User confirmed deletion. Deleting hunt from the database")

                    // call deleteHunt with the selectedHuntID as the parameter
                    db.deleteHunt(selectedHuntID)

                    // return to MainActivity
                    Log.d("IndividualActivity", "Returning to Main window")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // close IndividualHunt activity
                }
                .setNegativeButton("No") { dialog, _ ->
                    // user canceled deletion
                    Log.d("IndividualHunt", "User canceled deletion")
                    dialog.dismiss()
                }
                .show()
        }

        // on click listener for the previous form button '◀'
        previousFormBtn.setOnClickListener {
            Log.d("IndividualHunt", "Previous form button clicked. Retrieving the previous form of the Pokemon")
            val pokemon = pokemonList.find { it.pokemonID == selectedPokemonID }!!
            val sortedForms = pokemon.forms.sortedBy { it.formID }
            selectedFormID = if (selectedFormID!! != sortedForms.first().formID) {
                selectedFormID!! - 1
            } else {
                sortedForms.last().formID
            }
            val formName = pokemon.forms.find { it.formID == selectedFormID }!!.formName
            val formImage = pokemon.forms.find { it.formID == selectedFormID }!!.formImage
            selectedPokemonName.text = pokemon.pokemonName
            if (formName != null) {
                selectedPokemonForm.text = "($formName)"
            } else {
                selectedPokemonForm.text = ""
            }
            pokemonImage.setImageResource(formImage)
        }

        // on click listener for the next form button '▶'
        nextFormBtn.setOnClickListener {
            Log.d("IndividualHunt", "Next form button clicked. Retrieving the next form of the Pokemon")
            val pokemon = pokemonList.find { it.pokemonID == selectedPokemonID }!!
            val sortedForms = pokemon.forms.sortedBy { it.formID }
            if (selectedFormID!! != sortedForms.last().formID) {
                selectedFormID = selectedFormID!! + 1
            } else {
                selectedFormID = sortedForms.first().formID
            }
            val formName = pokemon.forms.find { it.formID == selectedFormID }!!.formName
            val formImage = pokemon.forms.find { it.formID == selectedFormID }!!.formImage
            selectedPokemonName.text = pokemon.pokemonName
            if (formName != null) {
                selectedPokemonForm.text = "($formName)"
            } else {
                selectedPokemonForm.text = ""
            }
            pokemonImage.setImageResource(formImage)
        }

        // on click listener for the pokemon selection button
        selectPokemonBtn.setOnClickListener {
            Log.d("IndividualHunt", "Pokemon selection button clicked. Preparing the Pokemon recycler view")
            val selectPokemonDialog = layoutInflater.inflate(R.layout.pokemon_selection, null)
            pokemonRecyclerView = selectPokemonDialog.findViewById(R.id.pokemon_recycler_view)

            val searchBar = selectPokemonDialog.findViewById<EditText>(R.id.search_pokemon)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5
            pokemonRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            val layoutManager = GridLayoutManager(this, spanCount)

            // prepare dataset with headers
            val groupedPokemonList = preparePokemonListWithHeaders(pokemonList)

            // custom span size logic to make headers span full width
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedPokemonList[position]) {
                        is PokemonListItem.HeaderItem -> spanCount  // header takes full row
                        is PokemonListItem.PokemonItem -> 1         // Pokemon takes 1 column
                    }
                }
            }

            pokemonRecyclerView.layoutManager = layoutManager

            // create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select a Pokémon")
                .setView(selectPokemonDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            pokemonRecyclerView.adapter = PokemonSelectionAdapter(groupedPokemonList) { selectedPokemon ->
                val formName = selectedPokemon.forms.find { it.isDefaultForm }?.formName
                val formImage = selectedPokemon.forms.find { it.isDefaultForm }!!.formImage
                selectedPokemonName.text = selectedPokemon.pokemonName
                if (formName != null) {
                    selectedPokemonForm.text = "($formName)"
                } else {
                    selectedPokemonForm.text = ""
                }
                pokemonImage.setImageResource(formImage)
                selectedPokemonID = selectedPokemon.pokemonID
                selectedFormID = selectedPokemon.forms.find { it.isDefaultForm }!!.formID
                previousFormBtn.visibility = if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                nextFormBtn.visibility = if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                dialog.dismiss()
            }

            // filter Pokémon as the user types
            searchBar.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val filteredList = groupedPokemonList
                        .filterIsInstance<PokemonListItem.PokemonItem>()
                        .filter { it.pokemon.pokemonName.contains(s.toString(), ignoreCase = true) }

                    // keep headers and merge them back into the list
                    val updatedList = preparePokemonListWithHeaders(filteredList.map { it.pokemon })

                    // update adapter
                    (pokemonRecyclerView.adapter as PokemonSelectionAdapter).updateList(updatedList)

                    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (updatedList[position]) {
                                is PokemonListItem.HeaderItem -> spanCount
                                is PokemonListItem.PokemonItem -> 1
                            }
                        }
                    }


                }
            })
        }

        // on click listener for the start date selection button
        pickStartDateBtn.setOnClickListener {
            Log.d("IndividualHunt", "Start date selection button clicked. Preparing the calendar")

            // create a calendar instance
            val c = Calendar.getInstance()

            // get the day, month, and year from the calendar
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // create a dialog for the date picker
            Log.d("IndividualHunt", "Creating date picker dialog")
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // format and set the text for the start date
                    selectedStartDate.text =
                        (buildString {
                            append(selectedDay.toString())
                            append("/")
                            append((selectedMonth + 1))
                            append("/")
                            append(selectedYear)
                        })
                },
                // pass the year, month, and day for the selected date
                year,
                month,
                day
            )
            Log.d("IndividualHunt", "Selected Start Date: ${selectedStartDate.text}")

            // display the date picker dialog
            datePickerDialog.show()
        }

        // on click listener for the origin game icon (i.e. unselecting the game)
        originGameIcon.setOnClickListener {
            Log.d("IndividualHunt", "Origin game icon clicked. Unselecting the origin game")
            originGameIconBorder.visibility = View.INVISIBLE
            originGameName.text = ""
            selectedOriginGameID = null
        }

        // on click listener for the origin game selection button
        selectOriginGameBtn.setOnClickListener {
            Log.d("IndividualHunt", "Origin game selection button clicked. Preparing the game recycler view")
            val selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
            gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
            gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            // prepare dataset with headers
            val groupedGameList = prepareGameListWithHeaders(gameList)

            // custom span size logic to make headers span full width
            (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedGameList[position]) {
                        is GameListItem.HeaderItem -> spanCount  // header takes full row
                        is GameListItem.GameItem -> 1            // game takes 1 column
                    }
                }
            }

            // create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select the Origin Game")
                .setView(selectGameDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            gameRecyclerView.adapter = GameSelectionAdapter(0, groupedGameList) { selectedGame ->
                originGameIcon.setImageResource(selectedGame.gameImage)
                originGameIconBorder.visibility = View.VISIBLE
                originGameName.text = selectedGame.gameName
                selectedOriginGameID = selectedGame.gameID - 1
                dialog.dismiss()
            }
        }

        // on click listener for the decrement counter button
        decrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement counter button clicked. Decrementing the counter")
            val counterValue = counter.text.toString().toIntOrNull()
            // decrement if value is greater than 0 and not null
            if (counterValue != null && counterValue > 0) {
                counter.setText(String.format((counterValue - 1).toString()))
            }
        }

        // on click listener for the increment counter button
        incrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment counter button clicked. Incrementing the counter")
            val counterValue = counter.text.toString().toIntOrNull()
            // increment if value is not null
            if (counterValue != null) {
                counter.setText(String.format((counterValue + 1).toString()))
            }
        }

        // on click listener for the decrement phase button
        decrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement phase button clicked. Decrementing the phase")
            val phaseValue = phase.text.toString().toIntOrNull()
            // decrement if value is greater than 0 and not null
            if (phaseValue != null && phaseValue > 0) {
                phase.setText(String.format((phaseValue - 1).toString()))
            }
        }

        // on click listener for the increment phase button
        incrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment phase button clicked. Incrementing the phase")
            val phaseValue = phase.text.toString().toIntOrNull()
            // increment if value is not null
            if (phaseValue != null) {
                phase.setText(String.format((phaseValue + 1).toString()))
            }
        }

        // on click listener for the hunt completed checkbox
        completionCheckbox.setOnClickListener {
            Log.d("IndividualHunt", "Hunt completed checkbox clicked. Updating the layout")
            // get the state of the checkbox
            val checkboxState = completionCheckbox.isChecked
            // if checkboxState is false, change background to gray gradient, and make layouts invisible
            if (!checkboxState) {
                mainLayout.setBackgroundResource(R.drawable.ui_gradient_incomplete_hunt)
                finishDateLayout.visibility = View.GONE
                currentGameLayout.visibility = View.GONE
            }
            // if checkboxState is true, change background to green gradient, and make layouts visible
            else {
                mainLayout.setBackgroundResource(R.drawable.ui_gradient_complete_hunt)
                finishDateLayout.visibility = View.VISIBLE
                currentGameLayout.visibility = View.VISIBLE
            }
        }

        // on click listener for the finish date selection button
        pickFinishDateBtn.setOnClickListener {
            Log.d("IndividualHunt", "Finish date selection button clicked. Preparing the calendar")

            // create a calendar instance
            val c = Calendar.getInstance()

            // get the day, month, and year from the calendar
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // create a dialog for the date picker
            Log.d("IndividualHunt", "Creating date picker dialog")
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // format and set the text for the finish date
                    selectedFinishDate.text =
                        (buildString {
                            append(selectedDay.toString())
                            append("/")
                            append((selectedMonth + 1))
                            append("/")
                            append(selectedYear)
                        })
                },
                // pass the year, month, and day for the selected date
                year,
                month,
                day
            )
            Log.d("IndividualHunt", "Selected Finish Date: ${selectedFinishDate.text}")

            // display the date picker dialog
            datePickerDialog.show()
        }

        // on click listener for the current game icon (i.e. unselecting the current game)
        currentGameIcon.setOnClickListener {
            Log.d("IndividualHunt", "Current game icon clicked. Unselecting the current game")
            currentGameIconBorder.visibility = View.INVISIBLE
            currentGameName.text = ""
            selectedCurrentGameID = null
        }

        // on click listener for the current game selection button
        selectCurrentGameBtn.setOnClickListener {
            Log.d("IndividualHunt", "Current game selection button clicked. Preparing the game recycler view")
            val selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
            gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

            val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3
            gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

            // prepare dataset with headers
            val groupedGameList = prepareGameListWithHeaders(gameList)

            // custom span size logic to make headers span full width
            (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (groupedGameList[position]) {
                        is GameListItem.HeaderItem -> spanCount  // header takes full row
                        is GameListItem.GameItem -> 1            // game takes 1 column
                    }
                }
            }

            // create and show the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Select the Current Game")
                .setView(selectGameDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            gameRecyclerView.adapter = GameSelectionAdapter(1, groupedGameList) { selectedGame ->
                currentGameIcon.setImageResource(selectedGame.gameImage)
                currentGameIconBorder.visibility = View.VISIBLE
                currentGameName.text = selectedGame.gameName
                selectedCurrentGameID = selectedGame.gameID - 1
                dialog.dismiss()
            }
        }

        Log.d("IndividualHunt", "onCreate() completed")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("IndividualHunt", "onConfigurationChanged() started")
        super.onConfigurationChanged(newConfig)

        // update layout orientation dynamically
        detailLayout.orientation =
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                LinearLayout.HORIZONTAL
            else
                LinearLayout.VERTICAL


        // determine the number of columns based on orientation
        val pokemonSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5
        val gameSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 5 else 3

        // reinitialize GridLayoutManagers with updated span counts
        val pokemonGridLayoutManager = GridLayoutManager(this, pokemonSpanCount)
        val gameGridLayoutManager = GridLayoutManager(this, gameSpanCount)

        // reset spanSizeLookup to ensure headers take up the full row
        pokemonGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((pokemonRecyclerView.adapter as? PokemonSelectionAdapter)?.getItemViewType(position)) {
                    PokemonSelectionAdapter.VIEW_TYPE_HEADER -> pokemonSpanCount // header spans full row
                    PokemonSelectionAdapter.VIEW_TYPE_POKEMON -> 1               // Pokemon takes 1 column
                    else -> 1 // default fallback
                }
            }
        }

        gameGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((gameRecyclerView.adapter as? GameSelectionAdapter)?.getItemViewType(position)) {
                    GameSelectionAdapter.VIEW_TYPE_HEADER -> gameSpanCount // header spans full row
                    GameSelectionAdapter.VIEW_TYPE_GAME -> 1               // game takes 1 column
                    else -> 1 // default fallback
                }
            }
        }

        // apply the updated layout managers
        pokemonRecyclerView.layoutManager = pokemonGridLayoutManager
        gameRecyclerView.layoutManager = gameGridLayoutManager

        Log.d("IndividualHunt", "onConfigurationChanged() completed")
    }

}