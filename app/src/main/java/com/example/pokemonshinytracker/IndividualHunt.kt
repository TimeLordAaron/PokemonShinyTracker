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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class IndividualHunt : ComponentActivity() {

    // lateinit UI var declarations
    private lateinit var backBtn: Button                    // back button
    private lateinit var saveBtn: Button                    // save button
    private lateinit var deleteBtn: Button                  // delete button
    private lateinit var detailLayout: ConstraintLayout     // detail layout
    private lateinit var previousFormBtn: ImageButton       // previous form button
    private lateinit var nextFormBtn: ImageButton           // next form button
    private lateinit var selectPokemonBtn: ImageButton      // pokemon selection button
    private lateinit var pokemonRecyclerView: RecyclerView  // pokemon recycler view
    private lateinit var selectedPokemonLabel: TextView     // selected pokemon label (will be hidden for the Individual Hunt page)
    private lateinit var selectPokemonDialogLayout: View    // pokemon selection dialog
    private lateinit var pickStartDateBtn: ImageButton      // start date button
    private lateinit var selectedStartDate: TextView        // start date text
    private lateinit var unselectStartDateBtn: ImageButton  // unselect start date button
    private lateinit var selectOriginGameBtn: ImageButton   // origin game button
    private lateinit var unselectOriginGameBtn: ImageButton // unselect origin game button
    private lateinit var enteredMethod: EditText            // method text field
    private lateinit var gameRecyclerView: RecyclerView     // game recycler view (used for origin game and current game)
    private lateinit var selectGameDialog: View             // game dialog (used for origin game and current game)
    private lateinit var enteredCounter: EditText           // counter text field
    private lateinit var decrementCounterBtn: ImageButton   // decrement counter button
    private lateinit var incrementCounterBtn: ImageButton   // increment counter button
    private lateinit var counterMultiplierBtn: Button       // counter multiplier button
    private lateinit var enteredPhase: EditText             // phase text field
    private lateinit var decrementPhaseBtn: ImageButton     // decrement phase button
    private lateinit var incrementPhaseBtn: ImageButton     // increment phase button
    private lateinit var enteredNotes: EditText             // notes text field
    private lateinit var completionCheckbox: CheckBox       // completion checkbox
    private lateinit var finishDateLabel: TextView          // finish date label
    private lateinit var pickFinishDateBtn: ImageButton     // finish date button
    private lateinit var selectedFinishDate: TextView       // finish date text
    private lateinit var unselectFinishDateBtn: ImageButton // unselect finish date button
    private lateinit var currentGameLabel: TextView         // current game label
    private lateinit var selectCurrentGameBtn: ImageButton  // current game button
    private lateinit var unselectCurrentGameBtn: ImageButton    // unselect current game button

    // lateinit UI declarations: counter multiplier UI
    private lateinit var counterMultiplierText: TextView            // counter multiplier text
    private lateinit var counterMultiplierDecrementBtn: ImageButton // counter multiplier decrement button
    private lateinit var counterMultiplierIncrementBtn: ImageButton // counter multiplier increment button

    // variable declarations for the selected hunt
    private var selectedHuntID = 0
    private lateinit var selectedHuntList: List<ShinyHunt>  // getHunts() function returns the shiny hunt as a list with 1 shiny hunt (or empty if this is a new hunt)
    private var selectedHunt: ShinyHunt? = null             // will be assigned the hunt in selectedHuntList if it's not empty
    private var selectedPokemonID: Int? = null
    private var selectedFormID: Int? = null
    private var selectedOriginGameID: Int? = null
    private var selectedCurrentGameID: Int? = null
    private var selectedDefaultPosition: Int? = null

    private var formImage: Int? = null

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

        // prepare the layout based on device orientation
        prepareLayout()

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
        val mainLayout = findViewById<ConstraintLayout>(R.id.individual_hunt_layout)        // layout of the entire window
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
        unselectStartDateBtn = findViewById(R.id.unselect_start_date_button)                // unselect start date button
        selectOriginGameBtn = findViewById(R.id.origin_game_button)                         // origin game select button
        val originGameIconBorder = findViewById<FrameLayout>(R.id.origin_game_icon_border)  // origin game icon border
        val originGameIcon = findViewById<ImageView>(R.id.origin_game_icon)                 // origin game icon
        unselectOriginGameBtn = findViewById(R.id.unselect_origin_game_button)              // unselect origin game button
        enteredMethod = findViewById(R.id.method)                                           // method edit text
        enteredCounter = findViewById(R.id.counter)                                         // counter edit text
        decrementCounterBtn = findViewById(R.id.decrement_counter_button)                   // counter decrement button
        incrementCounterBtn = findViewById(R.id.increment_counter_button)                   // counter increment button
        counterMultiplierBtn = findViewById(R.id.counter_multiplier_button)                 // counter multiplier button
        enteredPhase = findViewById(R.id.phase)                                             // phase edit text
        decrementPhaseBtn = findViewById(R.id.decrement_phase_button)                       // phase decrement button
        incrementPhaseBtn = findViewById(R.id.increment_phase_button)                       // phase increment button
        enteredNotes = findViewById(R.id.notes_text)                                        // notes edit text
        completionCheckbox = findViewById(R.id.hunt_complete_checkbox)                      // hunt completion checkbox
        finishDateLabel = findViewById(R.id.finish_date_label)                              // finish date label
        pickFinishDateBtn = findViewById(R.id.finish_date_button)                           // finish date button
        selectedFinishDate = findViewById(R.id.finish_date)                                 // finish date text view
        unselectFinishDateBtn = findViewById(R.id.unselect_finish_date_button)              // unselect finish date button
        currentGameLabel = findViewById(R.id.current_game_label)                            // current game label
        selectCurrentGameBtn = findViewById(R.id.current_game_button)                       // current game select button
        val currentGameIconBorder = findViewById<FrameLayout>(R.id.current_game_icon_border)// current game icon border
        val currentGameIcon = findViewById<ImageView>(R.id.current_game_icon)               // current game icon
        unselectCurrentGameBtn = findViewById(R.id.unselect_current_game_button)            // unselect current game button
        Log.d("IndividualHunt", "Accessed all UI elements")

        // set the text of the counter multiplier button
        counterMultiplierBtn.text = String.format("x%s", MyApplication.counterMultiplier)

        // apply filters to the entered counter and phase
        enteredCounter.filters = arrayOf(InputFilterMax(MyApplication.COUNTER_MAX))
        enteredPhase.filters = arrayOf(InputFilterMax(MyApplication.PHASE_MAX))

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
                formImage = pokemon.forms.find { it.formID == selectedHunt!!.formID }!!.formImage
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
                    pokemonImage.load(formImage) {
                        crossfade(true)
                        transformations(
                            ShadowTransformation(
                                shadowRadius = MyApplication.SHADOW_RADIUS,
                                dx = 6f,
                                dy = 6f,
                            )
                        )
                    }
                }

                // set the start date text and unselect button (if not empty)
                if (selectedHunt!!.startDate!!.isNotEmpty()) {
                    selectedStartDate.text = selectedHunt!!.startDate
                    selectedStartDate.visibility = View.VISIBLE
                    unselectStartDateBtn.visibility = View.VISIBLE
                }
                Log.d("IndividualHunt", "Start Date: ${selectedStartDate.text}")

                // set the origin game icon and unselect button (if not null)
                if (selectedHunt!!.originGameID != null) {
                    originGameIcon.setImageResource(gameList[selectedHunt!!.originGameID!!].gameImage)
                    originGameIconBorder.visibility = View.VISIBLE
                    unselectOriginGameBtn.visibility = View.VISIBLE
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

                // update completion checkbox state
                completionCheckbox.isChecked = selectedHunt!!.isComplete
                Log.d("IndividualHunt", "Completion Status: ${completionCheckbox.isChecked}")

                // update the background gradient
                if (selectedHunt!!.isComplete) {
                    mainLayout.setBackgroundResource(R.drawable.ui_background_individual_hunt_complete)
                    finishDateLabel.visibility = View.VISIBLE
                    pickFinishDateBtn.visibility = View.VISIBLE
                    if (selectedHunt!!.finishDate!!.isNotEmpty()) {
                        selectedFinishDate.text = selectedHunt!!.finishDate
                        selectedFinishDate.visibility = View.VISIBLE
                        unselectFinishDateBtn.visibility = View.VISIBLE
                    }
                    currentGameLabel.visibility = View.VISIBLE
                    selectCurrentGameBtn.visibility = View.VISIBLE
                    if (selectedCurrentGameID != null) {
                        currentGameIcon.setImageResource(gameList[selectedHunt!!.currentGameID!!].gameImage)
                        currentGameIconBorder.visibility = View.VISIBLE
                        unselectCurrentGameBtn.visibility = View.VISIBLE
                    }
                    Log.d("IndividualHunt", "Displaying complete hunt layout")
                } else {
                    mainLayout.setBackgroundResource(R.drawable.ui_background_individual_hunt_incomplete)
                    finishDateLabel.visibility = View.GONE
                    pickFinishDateBtn.visibility = View.GONE
                    selectedFinishDate.visibility = View.GONE
                    unselectFinishDateBtn.visibility = View.GONE
                    currentGameLabel.visibility = View.GONE
                    selectCurrentGameBtn.visibility = View.GONE
                    currentGameIconBorder.visibility = View.GONE
                    unselectCurrentGameBtn.visibility = View.GONE
                    Log.d("IndividualHunt", "Displaying incomplete hunt layout")
                }

            } else {
                Log.d("IndividualHunt", "No hunt found for ID: $selectedHuntID. This is a new hunt")
            }
        }

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
            formImage = pokemon.forms.find { it.formID == selectedFormID }!!.formImage
            selectedPokemonName.text = pokemon.pokemonName
            if (formName != null) {
                selectedPokemonForm.text = "($formName)"
            } else {
                selectedPokemonForm.text = ""
            }
            pokemonImage.load(formImage) {
                crossfade(true)
                transformations(
                    ShadowTransformation(
                        shadowRadius = MyApplication.SHADOW_RADIUS,
                        dx = 6f,
                        dy = 6f,
                    )
                )
            }
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
            formImage = pokemon.forms.find { it.formID == selectedFormID }!!.formImage
            selectedPokemonName.text = pokemon.pokemonName
            if (formName != null) {
                selectedPokemonForm.text = "($formName)"
            } else {
                selectedPokemonForm.text = ""
            }
            pokemonImage.load(formImage) {
                crossfade(true)
                transformations(
                    ShadowTransformation(
                        shadowRadius = MyApplication.SHADOW_RADIUS,
                        dx = 6f,
                        dy = 6f,
                    )
                )
            }
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

                val pokemonSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    resources.getInteger(R.integer.pokemon_span_landscape)
                } else {
                    resources.getInteger(R.integer.pokemon_span_portrait)
                }

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

                // filter for the selected pokemon from pokemonList
                val selectedList = selectedPokemonID?.let { id ->
                    pokemonList.find { it.pokemonID == id }?.let { listOf(it) }
                } ?: emptyList()

                val adapter =
                    PokemonSelectionAdapter(PokemonSelectionMode.SINGLE_SELECT, groupedPokemonList, selectedList) { selectedPokemon ->
                        val formName = selectedPokemon.forms.find { it.isDefaultForm }?.formName
                        formImage = selectedPokemon.forms.find { it.isDefaultForm }!!.formImage
                        selectedPokemonName.text = selectedPokemon.pokemonName
                        if (formName != null) {
                            selectedPokemonForm.text = "($formName)"
                        } else {
                            selectedPokemonForm.text = ""
                        }
                        pokemonImage.load(formImage) {
                            crossfade(true)
                            transformations(
                                ShadowTransformation(
                                    shadowRadius = MyApplication.SHADOW_RADIUS,
                                    dx = 6f,
                                    dy = 6f,
                                )
                            )
                        }
                        selectedPokemonID = selectedPokemon.pokemonID
                        selectedFormID = selectedPokemon.forms.find { it.isDefaultForm }!!.formID
                        previousFormBtn.visibility =
                            if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                        nextFormBtn.visibility =
                            if (selectedPokemon.forms.size > 1) View.VISIBLE else View.INVISIBLE
                        selectPokemonDialog.dismiss()
                        subMenuOpened = false
                    }

                pokemonRecyclerView.adapter = adapter

                // calculate the position of the selected pokemon
                val pos = adapter.getPositionOfPokemon(selectedPokemonID)
                if (pos != null) {
                    // if a pokemon is currently selected, scroll to its position in the pokemon list
                    pokemonRecyclerView.post {
                        pokemonRecyclerView.smoothScrollToPosition(pos)
                    }
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
                dh.createDatePickerDialog(this, selectedStartDate.text.toString(), {
                    // use the returned date string
                    selectedStartDate.text = it
                    selectedStartDate.visibility = View.VISIBLE
                    unselectStartDateBtn.visibility = View.VISIBLE  // show the unselect button
                }, {
                    // on close, unset subMenuOpened
                    subMenuOpened = false
                })

            }
        }

        // on click listener for the unselect start date button
        unselectStartDateBtn.setOnClickListener {
            unselectStartDateBtn.visibility = View.GONE
            selectedStartDate.text = ""
            selectedStartDate.visibility = View.GONE
        }

        // on click listener for the origin game selection button
        selectOriginGameBtn.setOnClickListener {
            Log.d("IndividualHunt", "Origin game selection button clicked. Preparing the game recycler view")

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                val selectGameDialogLayout = layoutInflater.inflate(R.layout.game_selection, null)
                gameRecyclerView = selectGameDialogLayout.findViewById(R.id.game_recycler_view)

                val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    resources.getInteger(R.integer.game_span_landscape)
                } else {
                    resources.getInteger(R.integer.game_span_portrait)
                }
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
                        selectedOriginGameID = selectedGame.gameID - 1
                        unselectOriginGameBtn.visibility = View.VISIBLE
                        selectOriginGameDialog.dismiss()
                        subMenuOpened = false
                    }

                selectedOriginGameID?.let { preselectedId ->
                    val targetIndex = groupedGameList.indexOfFirst { item ->
                        item is GameListItem.GameItem && item.game.gameID - 1 == preselectedId
                    }
                    if (targetIndex != -1) {
                        gameRecyclerView.post {
                            // smooth scroll looks nicer
                            gameRecyclerView.smoothScrollToPosition(targetIndex)
                        }
                    }
                }
            }
        }

        // on click listener for the unselect origin game button
        unselectOriginGameBtn.setOnClickListener {
            unselectOriginGameBtn.visibility = View.GONE
            originGameIconBorder.visibility = View.GONE
            selectedOriginGameID = null
        }

        // on click listener for the decrement counter button
        decrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement counter button clicked. Decrementing the counter")
            val counterValue = enteredCounter.text.toString().toIntOrNull()
            // decrement if value is not null
            if (counterValue != null) {
                enteredCounter.setText(String.format((counterValue - MyApplication.counterMultiplier).coerceAtLeast(0).toString()))
            }
        }

        // on click listener for the increment counter button
        incrementCounterBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment counter button clicked. Incrementing the counter")
            val counterValue = enteredCounter.text.toString().toIntOrNull()
            // increment if value is not null
            if (counterValue != null) {
                enteredCounter.setText(String.format((counterValue + MyApplication.counterMultiplier).coerceAtMost(MyApplication.COUNTER_MAX).toString()))
            }
        }

        // on click listener for the counter multiplier button
        counterMultiplierBtn.setOnClickListener {
            // check that a sub menu isn't currently open (to prevent the user from opening multiple dialogs at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                val counterMultiplierLayout = layoutInflater.inflate(R.layout.counter_multiplier_editor, null)

                // access the UI elements
                counterMultiplierText = counterMultiplierLayout.findViewById(R.id.counter_multiplier_text)
                counterMultiplierDecrementBtn = counterMultiplierLayout.findViewById(R.id.counter_multiplier_decrement)
                counterMultiplierIncrementBtn = counterMultiplierLayout.findViewById(R.id.counter_multiplier_increment)

                // set the counter multiplier text
                counterMultiplierText.text = String.format("x%s", MyApplication.counterMultiplier)

                // create a dialog for editing the counter multiplier
                dh.createLayoutDialog(this, "Set the Counter Multiplier", counterMultiplierLayout) {
                    subMenuOpened = false   // on close, unset subMenuOpened
                }

                // on click listener for the decrement button
                counterMultiplierDecrementBtn.setOnClickListener {
                    // decrement the value of the global counter multiplier, to a minimum of 1
                    MyApplication.counterMultiplier = (MyApplication.counterMultiplier - 1).coerceAtLeast(1)

                    // update the UI
                    counterMultiplierText.text = String.format("x%s", MyApplication.counterMultiplier)     // text in dialog
                    counterMultiplierBtn.text = String.format("x%s", MyApplication.counterMultiplier)      // button in main page
                }

                // on click listener for the increment button
                counterMultiplierIncrementBtn.setOnClickListener {
                    // increment the value of the global counter multiplier
                    MyApplication.counterMultiplier = (MyApplication.counterMultiplier + 1).coerceAtMost(MyApplication.COUNTER_MULTIPLIER_MAX)

                    // update the UI
                    counterMultiplierText.text = String.format("x%s", MyApplication.counterMultiplier)     // text in dialog
                    counterMultiplierBtn.text = String.format("x%s", MyApplication.counterMultiplier)      // button in main page
                }
            }
        }

        // on click listener for the decrement phase button
        decrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Decrement phase button clicked. Decrementing the phase")
            val phaseValue = enteredPhase.text.toString().toIntOrNull()
            // decrement if value is not null
            if (phaseValue != null && phaseValue > 0) {
                enteredPhase.setText(String.format((phaseValue - 1).coerceAtLeast(0).toString()))
            }
        }

        // on click listener for the increment phase button
        incrementPhaseBtn.setOnClickListener {
            Log.d("IndividualHunt", "Increment phase button clicked. Incrementing the phase")
            val phaseValue = enteredPhase.text.toString().toIntOrNull()
            // increment if value is not null
            if (phaseValue != null) {
                enteredPhase.setText(String.format((phaseValue + 1).coerceAtMost(MyApplication.PHASE_MAX).toString()))
            }
        }

        // on click listener for the hunt completed checkbox
        completionCheckbox.setOnClickListener {
            Log.d("IndividualHunt", "Hunt completed checkbox clicked. Updating the layout")
            // get the state of the checkbox
            val checkboxState = completionCheckbox.isChecked
            // if checkboxState is false, change background to gray gradient, and make layouts invisible
            if (!checkboxState) {
                mainLayout.setBackgroundResource(R.drawable.ui_background_individual_hunt_incomplete)
                finishDateLabel.visibility = View.GONE
                selectedFinishDate.visibility = View.GONE
                pickFinishDateBtn.visibility = View.GONE
                unselectFinishDateBtn.visibility = View.GONE
                currentGameLabel.visibility = View.GONE
                selectCurrentGameBtn.visibility = View.GONE
                currentGameIconBorder.visibility = View.GONE
                unselectCurrentGameBtn.visibility = View.GONE
            }
            // if checkboxState is true, change background to green gradient, and make layouts visible
            else {
                mainLayout.setBackgroundResource(R.drawable.ui_background_individual_hunt_complete)
                finishDateLabel.visibility = View.VISIBLE
                pickFinishDateBtn.visibility = View.VISIBLE
                if (selectedFinishDate.text.isNotBlank()) {
                    selectedFinishDate.visibility = View.VISIBLE
                    unselectFinishDateBtn.visibility = View.VISIBLE
                }
                currentGameLabel.visibility = View.VISIBLE
                selectCurrentGameBtn.visibility = View.VISIBLE
                if (selectedCurrentGameID != null) {
                    currentGameIconBorder.visibility = View.VISIBLE
                    unselectCurrentGameBtn.visibility = View.VISIBLE
                }
            }
        }

        // on click listener for the finish date selection button
        pickFinishDateBtn.setOnClickListener {
            Log.d("IndividualHunt", "Finish date selection button clicked. Preparing the calendar")

            // check that a sub menu isn't open yet (to prevent user from spamming buttons to open multiple menus at once)
            if (!subMenuOpened) {
                subMenuOpened = true

                // display the date picker
                dh.createDatePickerDialog(this, selectedFinishDate.text.toString(), {
                    // use the returned date string
                    selectedFinishDate.text = it
                    selectedFinishDate.visibility = View.VISIBLE
                    unselectFinishDateBtn.visibility = View.VISIBLE // show the unselect button
                }, {
                    // on close, unset subMenuOpened
                    subMenuOpened = false
                })

            }
        }

        // on click listener for the unselect finish date button
        unselectFinishDateBtn.setOnClickListener {
            unselectFinishDateBtn.visibility = View.GONE
            selectedFinishDate.text = ""
            selectedFinishDate.visibility = View.GONE
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
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        resources.getInteger(R.integer.game_span_landscape)
                    } else {
                        resources.getInteger(R.integer.game_span_portrait)
                    }
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
                        selectedCurrentGameID = selectedGame.gameID - 1
                        unselectCurrentGameBtn.visibility = View.VISIBLE
                        selectCurrentGameDialog.dismiss()
                        subMenuOpened = false
                    }

                selectedCurrentGameID?.let { preselectedId ->
                    val targetIndex = groupedGameList.indexOfFirst { item ->
                        item is GameListItem.GameItem && item.game.gameID - 1 == preselectedId
                    }
                    if (targetIndex != -1) {
                        gameRecyclerView.post {
                            // smooth scroll looks nicer
                            gameRecyclerView.smoothScrollToPosition(targetIndex)
                        }
                    }
                }
            }
        }

        // on click listener for the unselect current game button
        unselectCurrentGameBtn.setOnClickListener {
            unselectCurrentGameBtn.visibility = View.GONE
            currentGameIconBorder.visibility = View.GONE
            selectedCurrentGameID = null
        }

        Log.d("IndividualHunt", "onCreate() completed")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("IndividualHunt", "onConfigurationChanged() started")
        super.onConfigurationChanged(newConfig)

        // update the layout
        prepareLayout()

        // determine the number of columns based on orientation
        val pokemonSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resources.getInteger(R.integer.pokemon_span_landscape)
        } else {
            resources.getInteger(R.integer.pokemon_span_portrait)
        }
        val gameSpanCount = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resources.getInteger(R.integer.game_span_landscape)
        } else {
            resources.getInteger(R.integer.game_span_portrait)
        }

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

    // Helper function to prepare the layout based on device orientation
    private fun prepareLayout() {
        // hunt details
        val huntDetailsConstraintLayout = findViewById<ConstraintLayout>(R.id.individual_hunt_details)
        val huntDetailsConstraintSet = ConstraintSet()
        huntDetailsConstraintSet.clone(huntDetailsConstraintLayout)

        // handle portrait orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // constrain the pokemon section
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.BOTTOM, R.id.additional_details_section, ConstraintSet.TOP)

            // constrain the additional details section
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.TOP, R.id.pokemon_section, ConstraintSet.BOTTOM)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            // let both sections take up the maximum available width
            huntDetailsConstraintSet.constrainPercentWidth(R.id.pokemon_section, 1f)
            huntDetailsConstraintSet.constrainPercentWidth(R.id.additional_details_section, 1f)

            // let both sections take up roughly half the maximum available height
            huntDetailsConstraintSet.constrainPercentHeight(R.id.pokemon_section, 0.48f)
            huntDetailsConstraintSet.constrainPercentHeight(R.id.additional_details_section, 0.48f)

        // handle landscape orientation
        } else {
            // constrain the pokemon section
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            huntDetailsConstraintSet.connect(R.id.pokemon_section, ConstraintSet.END, R.id.additional_details_section, ConstraintSet.START)

            // constrain the additional details section
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.START, R.id.pokemon_section, ConstraintSet.END)
            huntDetailsConstraintSet.connect(R.id.additional_details_section, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

            // let both sections take up the maximum available height
            huntDetailsConstraintSet.constrainPercentHeight(R.id.pokemon_section, 1f)
            huntDetailsConstraintSet.constrainPercentHeight(R.id.additional_details_section, 1f)

            // let the additional details section take a bit more width than the pokemon section
            huntDetailsConstraintSet.constrainPercentWidth(R.id.pokemon_section, 0.38f)
            huntDetailsConstraintSet.constrainPercentWidth(R.id.additional_details_section, 0.58f)

        }

        huntDetailsConstraintSet.applyTo(huntDetailsConstraintLayout)
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