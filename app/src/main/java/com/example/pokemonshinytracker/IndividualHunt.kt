package com.example.pokemonshinytracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.ComponentActivity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
    private lateinit var selectedPokemonLabel: TextView     // selected pokemon label (will be hidden for the Individual Hunt page)
    private lateinit var selectPokemonDialogLayout: View    // pokemon selection dialog
    private lateinit var pickStartDateBtn: Button           // start date button
    private lateinit var selectedStartDate: TextView        // start date text
    private lateinit var selectOriginGameBtn: Button        // origin game button
    private lateinit var enteredMethod: EditText            // method text field
    private lateinit var enteredCounter: EditText           // counter text field
    private lateinit var gameRecyclerView: RecyclerView     // game recycler view (used for origin game and current game)
    private lateinit var selectGameDialog: View             // game dialog (used for origin game and current game)
    private lateinit var decrementCounterBtn: Button        // decrement counter button
    private lateinit var incrementCounterBtn: Button        // increment counter button
    private lateinit var enteredPhase: EditText             // phase text field
    private lateinit var decrementPhaseBtn: Button          // decrement phase button
    private lateinit var incrementPhaseBtn: Button          // increment phase button
    private lateinit var enteredNotes: EditText             // notes text field
    private lateinit var completionCheckbox: CheckBox       // completion checkbox
    private lateinit var pickFinishDateBtn: Button          // finish date button
    private lateinit var selectedFinishDate: TextView       // finish date text
    private lateinit var selectCurrentGameBtn: Button       // current game button

    // variable declarations for the selected hunt
    private var selectedHuntID = 0
    private lateinit var selectedHuntList: List<ShinyHunt>  // getHunts() function returns the shiny hunt as a list with 1 shiny hunt (or empty if this is a new hunt)
    private var selectedHunt: ShinyHunt? = null             // will be assigned the hunt in selectedHuntList if it's not empty
    private var selectedPokemonID: Int? = null
    private var selectedFormID: Int? = null
    private var selectedOriginGameID: Int? = null
    private var selectedCurrentGameID: Int? = null
    private var selectedDefaultPosition: Int? = null

    // variable to track if a sub menu is currently open
    private var subMenuOpened = false

    // database helper
    private val db = DBHelper(this, null)

    // dialog handler
    private val dh = DialogHandler()

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("IndividualHunt", "onCreate() started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_hunt)

        // hide the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // temporarily show the bars when swiping
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // set up detection for the user clicking the back button on their navigation bar
        onBackPressedDispatcher.addCallback(this) {
            handleBackNavigation()  // display confirmation dialog if there are unsaved changes
        }

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
        enteredMethod = findViewById(R.id.method)                                    // method edit text
        enteredCounter = findViewById(R.id.counter)                                  // counter edit text
        decrementCounterBtn = findViewById(R.id.decrement_counter_button)                   // counter decrement button
        incrementCounterBtn = findViewById(R.id.increment_counter_button)                   // counter increment button
        enteredPhase = findViewById(R.id.phase)                                      // phase edit text
        decrementPhaseBtn = findViewById(R.id.decrement_phase_button)                       // phase decrement button
        incrementPhaseBtn = findViewById(R.id.increment_phase_button)                       // phase increment button
        enteredNotes = findViewById(R.id.notes_text)                                 // notes edit text
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
            selectedHuntList = db.getHunts(selectedHuntID)
            Log.d("IndividualHunt", "Hunt for ID $selectedHuntID: $selectedHuntList")

            // check if a hunt was received from the database (should be empty if huntID was 0)
            if (selectedHuntList.isNotEmpty()) {
                selectedHunt = selectedHuntList[0]  // extracting the hunt from the returned list
                Log.d("IndividualHunt", "Received Hunt: $selectedHunt")
                selectedFormID = selectedHunt!!.formID
                Log.d("IndividualHunt", "Form ID: $selectedFormID")
                val pokemon = pokemonList.find { p -> p.forms.any { it.formID == selectedFormID } }!!
                val formName = pokemon.forms.find { it.formID == selectedHunt!!.formID }!!.formName
                val formImage = pokemon.forms.find { it.formID == selectedHunt!!.formID }!!.formImage
                Log.d("IndividualHunt", "Received Pokemon: $pokemon")
                if (selectedFormID != null) {
                    selectedPokemonID = pokemon.pokemonID
                    if (pokemon.forms.size > 1) {
                        previousFormBtn.visibility = View.VISIBLE
                        nextFormBtn.visibility = View.VISIBLE
                    }
                }
                Log.d("IndividualHunt", "Pokemon ID: $selectedPokemonID")
                selectedOriginGameID = selectedHunt!!.originGameID
                Log.d("IndividualHunt", "Origin Game ID: $selectedOriginGameID")
                selectedCurrentGameID = selectedHunt!!.currentGameID
                Log.d("IndividualHunt", "Current Game ID: $selectedCurrentGameID")
                selectedDefaultPosition = selectedHunt!!.defaultPosition
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
                if (selectedHunt!!.startDate != null) {
                    selectedStartDate.text = selectedHunt!!.startDate
                }
                Log.d("IndividualHunt", "Start Date: ${selectedStartDate.text}")

                // update the origin game icon and name
                if (selectedHunt!!.originGameID != null) {
                    originGameIcon.setImageResource(gameList[selectedHunt!!.originGameID!!].gameImage)
                    originGameIconBorder.visibility = View.VISIBLE
                    originGameName.text = gameList[selectedHunt!!.originGameID!!].gameName
                }

                // update the method text
                enteredMethod.setText(selectedHunt!!.method)
                Log.d("IndividualHunt", "Method: ${enteredMethod.text}")

                // update counter
                enteredCounter.setText(selectedHunt!!.counter.toString())
                Log.d("IndividualHunt", "Counter: ${enteredCounter.text}")

                // update phase
                enteredPhase.setText(selectedHunt!!.phase.toString())
                Log.d("IndividualHunt", "Phase: ${enteredPhase.text}")

                // update notes
                enteredNotes.setText(selectedHunt!!.notes)
                Log.d("IndividualHunt", "Notes: ${enteredNotes.text}")

                // update the finish date text (if not null)
                if (selectedHunt!!.finishDate != null) {
                    selectedFinishDate.text = selectedHunt!!.finishDate
                }
                Log.d("IndividualHunt", "Finish Date: ${selectedFinishDate.text}")

                // update the current game icon and name
                if (selectedHunt!!.currentGameID != null) {
                    currentGameIcon.setImageResource(gameList[selectedHunt!!.currentGameID!!].gameImage)
                    currentGameIconBorder.visibility = View.VISIBLE
                    currentGameName.text = gameList[selectedHunt!!.currentGameID!!].gameName
                }

                // update completion checkbox state
                completionCheckbox.isChecked = selectedHunt!!.isComplete
                Log.d("IndividualHunt", "Completion Status: ${completionCheckbox.isChecked}")

                // update the background gradient
                if (selectedHunt!!.isComplete) {
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
        selectPokemonDialogLayout = layoutInflater.inflate(R.layout.pokemon_selection, null)
        pokemonRecyclerView = selectPokemonDialogLayout.findViewById(R.id.pokemon_recycler_view)
        selectGameDialog = layoutInflater.inflate(R.layout.game_selection, null)
        gameRecyclerView = selectGameDialog.findViewById(R.id.game_recycler_view)

        // set up a recycled view pool for the pokemon recycler view so it doesn't have to reinflate all the views each time the dialog is opened
        val pokemonViewPool = RecyclerView.RecycledViewPool()
        pokemonRecyclerView.setRecycledViewPool(pokemonViewPool)

        // on click listener for the back button
        backBtn.setOnClickListener {
            Log.d("IndividualHunt", "Back button clicked")
            handleBackNavigation()  // display confirmation dialog if there are unsaved changes
        }

        // on click listener for the save button
        saveBtn.setOnClickListener {
            Log.d("IndividualHunt", "Save button clicked. Saving hunt to the database")

            // check that a pokemon is selected
            if (selectedPokemonID == null || selectedFormID == null) {
                // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                if (!subMenuOpened) {
                    subMenuOpened = true

                    // create the error dialog
                    dh.createErrorDialog(this, "Pokémon Not Selected", "Please select a Pokémon before saving!") {
                        subMenuOpened = false   // on close, unset subMenuOpened
                    }

                }
            } else {

                // call updateHunt with the values selected by the user
                db.updateHunt(
                    selectedHuntID,
                    selectedFormID,
                    selectedOriginGameID,
                    enteredMethod.text.toString(),
                    selectedStartDate.text.toString(),
                    enteredCounter.text.toString().toInt(),
                    enteredPhase.text.toString().toInt(),
                    enteredNotes.text.toString(),
                    completionCheckbox.isChecked,
                    if (completionCheckbox.isChecked) selectedFinishDate.text.toString() else "",
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
        }

        // on click listener for the delete button
        deleteBtn.setOnClickListener {
            Log.d("IndividualHunt", "Delete button clicked. Showing confirmation dialog")

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                // create a confirmation dialog
                dh.createConfirmationDialog(
                    this,
                    "Confirm Deletion",
                    "Are you sure you want to delete this shiny hunt? This action cannot be undone.",
                    {
                        // logic for Yes button
                        // call deleteHunt with the selectedHuntID as the parameter
                        db.deleteHunt(selectedHuntID)

                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish() // close IndividualHunt activity
                    },
                    {
                        subMenuOpened = false   // on close, unset subMenuOpened
                    })
            }
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

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                // hide the "Selected Pokemon" label
                selectedPokemonLabel = selectPokemonDialogLayout.findViewById(R.id.selected_pokemon_label)
                selectedPokemonLabel.visibility = View.GONE

                val pokemonSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.POKEMON_SPAN_LANDSCAPE else MyApplication.POKEMON_SPAN_PORTRAIT

                val layoutManager = GridLayoutManager(this, pokemonSpanCount)

                // prepare dataset with headers
                val groupedPokemonList = preparePokemonListWithHeaders(pokemonList)

                // custom span size logic to make headers span full width
                layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (groupedPokemonList[position]) {
                            is PokemonListItem.HeaderItem -> pokemonSpanCount  // header takes full row
                            is PokemonListItem.PokemonItem -> 1         // Pokemon takes 1 column
                        }
                    }
                }

                // apply the layout manager to the Pokemon recycler view
                pokemonRecyclerView.layoutManager = layoutManager

                // detach the dialog layout from any previous parent before reattaching
                (selectPokemonDialogLayout.parent as? ViewGroup)?.removeView(selectPokemonDialogLayout)

                // create the Pokemon selection dialog
                val selectPokemonDialog = dh.createLayoutDialog(this, "Select a Pokémon", selectPokemonDialogLayout) {
                    subMenuOpened = false   // on close, unset subMenuOpened
                }

                pokemonRecyclerView.adapter =
                    PokemonSelectionAdapter(PokemonSelectionMode.SINGLE_SELECT, groupedPokemonList, emptyList()) { selectedPokemon ->
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
                        previousFormBtn.visibility =
                            if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                        nextFormBtn.visibility =
                            if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                        selectPokemonDialog.dismiss()
                        subMenuOpened = false
                    }

                // access the search bar in the Pokemon selection dialog
                val searchBar = selectPokemonDialogLayout.findViewById<EditText>(R.id.search_pokemon)
                searchBar.setText("")   // clear the search bar in case the user previously typed something

                // filter Pokemon as the user types
                searchBar.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val filteredList = groupedPokemonList
                            .filterIsInstance<PokemonListItem.PokemonItem>()
                            .filter {
                                it.pokemon.pokemonName.contains(
                                    s.toString(),
                                    ignoreCase = true
                                )
                            }

                        // keep headers and merge them back into the list
                        val updatedList =
                            preparePokemonListWithHeaders(filteredList.map { it.pokemon })

                        // update adapter
                        (pokemonRecyclerView.adapter as PokemonSelectionAdapter).updateList(
                            updatedList
                        )

                        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return when (updatedList[position]) {
                                    is PokemonListItem.HeaderItem -> pokemonSpanCount
                                    is PokemonListItem.PokemonItem -> 1
                                }
                            }
                        }


                    }
                })
            }
        }

        // on click listener for the start date selection button
        pickStartDateBtn.setOnClickListener {
            Log.d("IndividualHunt", "Start date selection button clicked. Preparing the calendar")

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                // display the date picker
                dh.createDatePickerDialog(this, {
                    // use the returned date string
                    selectedStartDate.text = it
                }, {
                    // on close, unset subMenuOpened
                    subMenuOpened = false
                })

            }
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

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                val selectGameDialogLayout = layoutInflater.inflate(R.layout.game_selection, null)
                gameRecyclerView = selectGameDialogLayout.findViewById(R.id.game_recycler_view)

                val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT
                gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

                // prepare dataset with headers
                val groupedGameList = prepareGameListWithHeaders(gameList)

                // custom span size logic to make headers span full width
                (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                    object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (groupedGameList[position]) {
                                is GameListItem.HeaderItem -> spanCount  // header takes full row
                                is GameListItem.GameItem -> 1            // game takes 1 column
                            }
                        }
                    }

                // create the origin game selection dialog
                val selectOriginGameDialog = dh.createLayoutDialog(this, "Select the Origin Game", selectGameDialogLayout) {
                    subMenuOpened = false   // on close, unset subMenuOpened
                }

                gameRecyclerView.adapter =
                    GameSelectionAdapter(GameSelectionMode.ORIGIN_SINGLE_SELECT, groupedGameList, listOf(selectedOriginGameID)) { selectedGame ->
                        originGameIcon.setImageResource(selectedGame.gameImage)
                        originGameIconBorder.visibility = View.VISIBLE
                        originGameName.text = selectedGame.gameName
                        selectedOriginGameID = selectedGame.gameID - 1
                        selectOriginGameDialog.dismiss()
                        subMenuOpened = false
                    }
            }
        }

        // on click listener for the decrement counter button
        decrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement counter button clicked. Decrementing the counter")
            val counterValue = enteredCounter.text.toString().toIntOrNull()
            // decrement if value is greater than 0 and not null
            if (counterValue != null && counterValue > 0) {
                enteredCounter.setText(String.format((counterValue - 1).toString()))
            }
        }

        // on click listener for the increment counter button
        incrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment counter button clicked. Incrementing the counter")
            val counterValue = enteredCounter.text.toString().toIntOrNull()
            // increment if value is not null
            if (counterValue != null) {
                enteredCounter.setText(String.format((counterValue + 1).toString()))
            }
        }

        // on click listener for the decrement phase button
        decrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement phase button clicked. Decrementing the phase")
            val phaseValue = enteredPhase.text.toString().toIntOrNull()
            // decrement if value is greater than 0 and not null
            if (phaseValue != null && phaseValue > 0) {
                enteredPhase.setText(String.format((phaseValue - 1).toString()))
            }
        }

        // on click listener for the increment phase button
        incrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment phase button clicked. Incrementing the phase")
            val phaseValue = enteredPhase.text.toString().toIntOrNull()
            // increment if value is not null
            if (phaseValue != null) {
                enteredPhase.setText(String.format((phaseValue + 1).toString()))
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

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                // display the date picker
                dh.createDatePickerDialog(this, {
                    // use the returned date string
                    selectedFinishDate.text = it
                }, {
                    // on close, unset subMenuOpened
                    subMenuOpened = false
                })

            }
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

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                val selectGameDialogLayout = layoutInflater.inflate(R.layout.game_selection, null)
                gameRecyclerView = selectGameDialogLayout.findViewById(R.id.game_recycler_view)

                val spanCount =
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT
                gameRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

                // prepare dataset with headers
                val groupedGameList = prepareGameListWithHeaders(gameList)

                // custom span size logic to make headers span full width
                (gameRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                    object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return when (groupedGameList[position]) {
                                is GameListItem.HeaderItem -> spanCount  // header takes full row
                                is GameListItem.GameItem -> 1            // game takes 1 column
                            }
                        }
                    }

                // create the current game selection dialog
                val selectCurrentGameDialog = dh.createLayoutDialog(this, "Select the Current Game", selectGameDialogLayout) {
                    subMenuOpened = false   // on close, unset subMenuOpened
                }

                gameRecyclerView.adapter =
                    GameSelectionAdapter(GameSelectionMode.CURRENT_SINGLE_SELECT, groupedGameList, listOf(selectedCurrentGameID)) { selectedGame ->
                        currentGameIcon.setImageResource(selectedGame.gameImage)
                        currentGameIconBorder.visibility = View.VISIBLE
                        currentGameName.text = selectedGame.gameName
                        selectedCurrentGameID = selectedGame.gameID - 1
                        selectCurrentGameDialog.dismiss()
                        subMenuOpened = false
                    }
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
        val pokemonSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.POKEMON_SPAN_LANDSCAPE else MyApplication.POKEMON_SPAN_PORTRAIT
        val gameSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT

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

    // Helper function for detecting unsaved changes
    private fun detectUnsavedChanges(): Boolean {
        Log.d("IndividualHunt", "detectUnsavedChanges() started")

        // extract all of the entered/selected fields
        val formID = selectedFormID
        val startDate = selectedStartDate.text
        val originGameID = selectedOriginGameID
        val methodText = enteredMethod.text.toString()
        val counterText = enteredCounter.text.toString()
        val phaseText = enteredPhase.text.toString()
        val notesText = enteredNotes.text.toString()
        val isComplete = completionCheckbox.isChecked
        val finishDate = selectedFinishDate.text
        val currentGameID = selectedCurrentGameID

        // if editing a pre-existing hunt, return true if any of the fields are different from the original unedited hunt
        selectedHunt?.let { hunt ->
            return formID != hunt.formID ||
                    startDate != hunt.startDate ||
                    originGameID != hunt.originGameID ||
                    methodText != hunt.method ||
                    counterText != hunt.counter.toString() ||
                    phaseText != hunt.phase.toString() ||
                    notesText != hunt.notes ||
                    isComplete != hunt.isComplete ||
                    finishDate != hunt.finishDate ||
                    currentGameID != hunt.currentGameID
        }

        // if creating a new hunt, return true if any of the fields are not their default value
        return formID != null ||
                startDate.isNotEmpty() ||
                originGameID != null ||
                methodText.isNotEmpty() ||
                counterText != "0" ||
                phaseText != "0" ||
                notesText.isNotEmpty() ||
                isComplete ||
                finishDate.isNotEmpty() ||
                currentGameID != null
    }

    // Function to display a confirmation dialog if the user has unsaved changes while trying to navigate back to the MainActivity window
    private fun handleBackNavigation() {
        Log.d("IndividualHunt", "Back navigation triggered")

        // check that a sub menu isn't currently open (so the user can't accidentally close the page by hitting the back button
        if (!subMenuOpened) {
            subMenuOpened = true

            // if unsaved changes are detected, open a confirmation dialog
            if (detectUnsavedChanges()) {
                dh.createConfirmationDialog(
                    this,
                    "Unsaved Changes Detected",
                    "You have unsaved changes. Are you sure you want to return to the main menu without saving?", {
                        // logic for Yes button
                        finish() // close IndividualHunt activity
                    }, {
                        subMenuOpened = false   // on close, unset subMenuOpened
                    })
            }
            // if there are no detected changes, return to the MainActivity window
            else { finish() }
        }
    }
}