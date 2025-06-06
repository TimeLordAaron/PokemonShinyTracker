package com.example.pokemonshinytracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class MainActivity : ComponentActivity() {

    // lateinit UI declarations: main UI
    private lateinit var newHuntBtn: Button                     // new hunt button
    private lateinit var filterBtn: Button                      // filter button
    private lateinit var mainClearFiltersBtn: Button            // clear filters button (main page)
    private lateinit var expandAllCheckbox: CheckBox            // expand all checkbox
    private lateinit var shinyHuntRecyclerView: RecyclerView    // shiny hunt recycler view
    private lateinit var sortBtn: Button                        // floating sort button
    private lateinit var sortMenu: FrameLayout                  // sort menu
    private lateinit var sortMenuLayout: LinearLayout           // sort menu linear layout
    private lateinit var sortBackBtn: Button                    // sort menu back button
    private lateinit var sortDefaultBtn: Button                 // sort by default button
    private lateinit var sortStartDateBtn: Button               // sort by start date button
    private lateinit var sortFinishDateBtn: Button              // sort by finish date button
    private lateinit var sortNameBtn: Button                    // sort by name button
    private lateinit var sortGenerationBtn: Button              // sort by generation button

    // lateinit UI declarations: filter selection UI
    private lateinit var filterClearFiltersBtn: Button          // clear filters button (filter selection dialog)
    private lateinit var editPokemonBtn: Button                 // edit pokemon button
    private lateinit var pokemonTxt: TextView                   // selected pokemon text
    private lateinit var selectedPokemonRecyclerView: RecyclerView  // selected pokemon recycler view
    private lateinit var pokemonListRecyclerView: RecyclerView  // pokemon list recycler view
    private lateinit var editOriginGamesBtn: Button             // edit origin games button
    private lateinit var originGamesTxt: TextView               // selected origin games text
    private lateinit var completionStatusRadioGrp: RadioGroup   // completion status radio group
    private lateinit var inProgressRadioBtn: RadioButton        // in progress radio button (for completion status)
    private lateinit var completedRadioBtn: RadioButton         // completed radio button (for completion status)
    private lateinit var bothRadioBtn: RadioButton              // both radio button (for completion status)
    private lateinit var currentGamesLayout: LinearLayout       // current games layout
    private lateinit var editCurrentGamesBtn: Button            // edit current games button
    private lateinit var currentGamesTxt: TextView              // selected current games text
    private lateinit var method: EditText                       // method text field
    private lateinit var startDateFromBtn: Button               // start date from button
    private lateinit var startDateToBtn: Button                 // start date to button
    private lateinit var finishDateLayout: LinearLayout         // finish date range layout
    private lateinit var finishDateFromBtn: Button              // finish date from button
    private lateinit var finishDateToBtn: Button                // finish date to button
    private lateinit var counterLo: EditText                    // counter (low bound) text field
    private lateinit var counterHi: EditText                    // counter (high bound) text field
    private lateinit var phaseLo: EditText                      // phase (low bound) text field
    private lateinit var phaseHi: EditText                      // phase (high bound) text field
    private lateinit var confirmFiltersBtn: Button              // confirm filters button

    // initialize variables for tracking the current sort method
    // 0: Default, 1: Start Date, 2: Finish Date, 3: Name, 4: Generation
    private var currentSortMethod = SortMethod.DEFAULT
    private var currentSortMethodIndex = 0
    private var currentSortOrders =
        arrayOf(SortOrder.DESC, SortOrder.DESC, SortOrder.DESC, SortOrder.ASC, SortOrder.ASC)

    // initialize variables for tracking the selected filters
    private var selectedPokemon = mutableListOf<Pokemon>()           // dataset for the selected pokemon RecyclerView
    private var selectedPokemonForms = mutableSetOf<Int>()           // formIDs of every selected Pokemon Form
    private var selectedOriginGames = mutableListOf<Int>()           // gameIDs of every selected Origin Game
    private var selectedCompletionStatus: CompletionStatus? = null
    private var selectedCurrentGames = mutableListOf<Int>()           // gameIDs of every selected Current Game
    private var enteredMethod = ""
    private var selectedStartDateFrom = ""
    private var selectedStartDateTo = ""
    private var selectedFinishDateFrom = ""
    private var selectedFinishDateTo = ""
    private var enteredCounterLo = ""
    private var enteredCounterHi = ""
    private var enteredPhaseLo = ""
    private var enteredPhaseHi = ""

    // initialize variables for tracking the confirmed filters
    private var confirmedPokemonFormsFilter = mutableSetOf<Int>()
    private var confirmedOriginGamesFilter = mutableListOf<Int>()
    private var confirmedCompletionStatusFilter: CompletionStatus? = null
    private var confirmedCurrentGamesFilter = mutableListOf<Int>()
    private var confirmedMethodFilter = ""
    private var confirmedStartDateFromFilter = ""
    private var confirmedStartDateToFilter = ""
    private var confirmedFinishDateFromFilter = ""
    private var confirmedFinishDateToFilter = ""
    private var confirmedCounterLoFilter = ""
    private var confirmedCounterHiFilter = ""
    private var confirmedPhaseLoFilter = ""
    private var confirmedPhaseHiFilter = ""

    // access the database
    val db = DBHelper(this, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate() started")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        var hunts = db.getHunts()                                                 // gets all hunts
        //val hunts = db.getHunts(formIDs = listOf(720, 721, 722, 723, 724, 725))   // filters for Rotom
        //val hunts = db.getHunts(originGameIDs = listOf(12, 13, 14))               // filters for hunts in D/P/PL
        //val hunts = db.getHunts(currentGameIDs = listOf(27, 28))                  // filters for shinies transferred to US/UM
        //val hunts = db.getHunts(startedFrom = "2021-05-01", startedTo = "2021-07-01")   // filters for hunts started from May-July 2020 ( doesn't work yet >:( )
        //val hunts = db.getHunts(counterLo = 0, counterHi = 100)     // filters for hunts completed in 100 encounters or less
        //val hunts = db.getHunts(completionStatus = CompletionStatus.IN_PROGRESS, sortMethod = SortMethod.NAME, sortOrder = SortOrder.DESC)  // filters for in progress hunts, sorts by name in reverse alphabetical order
        Log.d("MainActivity", "Retrieved ${hunts.size} shiny hunts from database")

        // access the UI elements
        val noHuntsMessage = findViewById<TextView>(R.id.no_hunts_message)                      // message for when user has no saved hunts
        shinyHuntRecyclerView = findViewById(R.id.shiny_hunts_recycler_view)                    // recycler view that displays the user's saved hunts
        newHuntBtn = findViewById(R.id.new_hunt_button)                                         // new hunt button
        filterBtn = findViewById(R.id.filter_button)                                            // filter button
        mainClearFiltersBtn = findViewById(R.id.clear_filters_button)                           // clear filters button (main page)
        expandAllCheckbox = findViewById(R.id.expand_all_checkbox)                              // expand all checkbox
        sortBtn = findViewById(R.id.sort_button)                                                // floating sort button
        sortMenu = findViewById(R.id.sort_menu)                                                 // sort menu
        sortMenuLayout = findViewById(R.id.sort_menu_layout)                                    // sort menu linear layout
        sortBackBtn = findViewById(R.id.sort_back_button)                                       // sort menu back button
        sortDefaultBtn = findViewById(R.id.sort_default_button)                                 // sort by default button
        sortStartDateBtn = findViewById(R.id.sort_start_date_button)                            // sort by start date button
        sortFinishDateBtn = findViewById(R.id.sort_finish_date_button)                          // sort by finish date button
        sortNameBtn = findViewById(R.id.sort_name_button)                                       // sort by name button
        sortGenerationBtn = findViewById(R.id.sort_generation_button)                           // sort by generation button

        // instantiate an adapter for the shiny hunt recycler view
        val shinyHuntListAdapter = ShinyHuntListAdapter(this, pokemonList, gameList).apply {
            onScrollToPosition = { position ->
                // when swapping shiny hunts, scroll to the position of the shiny hunt that initiated the swap
                shinyHuntRecyclerView.scrollToPosition(position)
            }
            onExpandStateChanged = { allExpanded ->
                // check the expand all checkbox if all shiny hunts are currently expanded
                expandAllCheckbox.isChecked = allExpanded
            }
        }

        // set the margins of the floating sort button and sort menu
        val sortBtnParams = sortBtn.layoutParams as FrameLayout.LayoutParams
        val sortMenuParams = sortMenuLayout.layoutParams as FrameLayout.LayoutParams

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // set margins for landscape
            sortBtnParams.setMargins(0, 0, 150, 50)
            sortMenuParams.setMargins(0, 0, 150, 0)
        } else {
            // set margins for portrait
            sortBtnParams.setMargins(0, 0, 20, 150)
            sortMenuParams.setMargins(0, 0, 0, 0)
        }

        // apply margins to the sort button and sort menu
        sortBtn.layoutParams = sortBtnParams
        sortMenuLayout.layoutParams = sortMenuParams

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
            shinyHuntListAdapter.submitList(hunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

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

        /* Helper functions for setting UI in the filter selection dialog */
        // selected pokemon
        fun setPokemon() {
            if (selectedPokemonForms.size == 1) {
                pokemonTxt.text = "1 Pokemon form selected."
            } else {
                pokemonTxt.text = "${selectedPokemonForms.size} Pokemon forms selected."
            }
        }
        // selected origin games
        fun setOriginGames() {
            if (selectedOriginGames.size == 1) {
                originGamesTxt.text = "1 game selected."
            } else {
                originGamesTxt.text = "${selectedOriginGames.size} games selected."
            }
        }
        // selected completion status
        fun setCompletionStatus() {
            when (selectedCompletionStatus) {
                CompletionStatus.IN_PROGRESS -> completionStatusRadioGrp.check(R.id.in_progress_radio_button)
                CompletionStatus.COMPLETE -> completionStatusRadioGrp.check(R.id.completed_radio_button)
                CompletionStatus.BOTH -> completionStatusRadioGrp.check(R.id.both_radio_button)
                else -> {}  // do nothing
            }
        }
        // selected current games
        fun setCurrentGames() {
            if (selectedCompletionStatus == null || selectedCompletionStatus == CompletionStatus.IN_PROGRESS) {
                currentGamesLayout.visibility = View.GONE
            } else {
                currentGamesLayout.visibility = View.VISIBLE
                if (selectedCurrentGames.size == 1) {
                    currentGamesTxt.text = "1 game selected."
                } else {
                    currentGamesTxt.text = "${selectedCurrentGames.size} games selected."
                }
            }
        }
        // entered method
        fun setMethod() { method.setText(enteredMethod) }
        // start date from
        fun setStartDateFrom() { startDateFromBtn.text = selectedStartDateFrom }
        // start date to
        fun setStartDateTo() { startDateToBtn.text = selectedStartDateTo }
        // finish date layout
        fun setFinishDateLayout() {
            if (selectedCompletionStatus == null || selectedCompletionStatus == CompletionStatus.IN_PROGRESS) {
                finishDateLayout.visibility = View.GONE
            } else {
                finishDateLayout.visibility = View.VISIBLE
            }
        }
        // finish date from
        fun setFinishDateFrom() { finishDateFromBtn.text = selectedFinishDateFrom }
        // finish date to
        fun setFinishDateTo() { finishDateToBtn.text = selectedFinishDateTo }
        // counter low
        fun setCounterLo() { counterLo.setText(enteredCounterLo) }
        // counter high
        fun setCounterHi() { counterHi.setText(enteredCounterHi) }
        // phase low
        fun setPhaseLo() { phaseLo.setText(enteredPhaseLo) }
        // phase high
        fun setPhaseHi() { phaseHi.setText(enteredPhaseHi) }

        // on click listener for the filter button
        filterBtn.setOnClickListener {
            Log.d("MainActivity", "Filter button clicked. Opening filters dialog")

            val filterDialog = layoutInflater.inflate(R.layout.filter_selection, null)

            // access all the UI elements
            filterClearFiltersBtn = filterDialog.findViewById(R.id.clear_filters_button)    // clear filters button (filter selection dialog)
            editPokemonBtn = filterDialog.findViewById(R.id.edit_pokemon_button)            // edit pokemon button
            pokemonTxt = filterDialog.findViewById(R.id.pokemon_text)                       // selected pokemon text
            editOriginGamesBtn = filterDialog.findViewById(R.id.edit_origin_games_button)   // edit origin games button
            originGamesTxt = filterDialog.findViewById(R.id.origin_games_text)              // selected origin games text
            completionStatusRadioGrp = filterDialog.findViewById(R.id.completion_statuses_radio_group)  // completion status radio group
            inProgressRadioBtn = filterDialog.findViewById(R.id.in_progress_radio_button)   // in progress radio button (for completion status)
            completedRadioBtn = filterDialog.findViewById(R.id.completed_radio_button)      // completed radio button (for completion status)
            bothRadioBtn = filterDialog.findViewById(R.id.both_radio_button)                // both radio button (for completion status)
            currentGamesLayout = filterDialog.findViewById(R.id.current_games_layout)       // current games layout
            editCurrentGamesBtn = filterDialog.findViewById(R.id.edit_current_games_button) // edit current games button
            currentGamesTxt = filterDialog.findViewById(R.id.current_games_text)            // selected current games text
            method = filterDialog.findViewById(R.id.method)                                 // method text field
            startDateFromBtn = filterDialog.findViewById(R.id.start_date_from_button)       // start date from button
            startDateToBtn = filterDialog.findViewById(R.id.start_date_to_button)           // start date to button
            finishDateLayout = filterDialog.findViewById(R.id.finish_date_layout)           // finish date range layout
            finishDateFromBtn = filterDialog.findViewById(R.id.finish_date_from_button)     // finish date from button
            finishDateToBtn = filterDialog.findViewById(R.id.finish_date_to_button)         // finish date to button
            counterLo = filterDialog.findViewById(R.id.counter_lo)                          // counter (low bound) text field
            counterHi = filterDialog.findViewById(R.id.counter_hi)                          // counter (high bound) text field
            phaseLo = filterDialog.findViewById(R.id.phase_lo)                              // phase (low bound) text field
            phaseHi = filterDialog.findViewById(R.id.phase_hi)                              // phase (high bound) text field
            confirmFiltersBtn = filterDialog.findViewById(R.id.confirm_filters_button)      // confirm filters button

            // initialize the UI based on the currently selected filters
            setPokemon()
            setOriginGames()
            setCompletionStatus()
            setCurrentGames()
            setMethod()
            setStartDateFrom()
            setStartDateTo()
            setFinishDateLayout()
            setFinishDateFrom()
            setFinishDateTo()
            setCounterLo()
            setCounterHi()
            setPhaseLo()
            setPhaseHi()

            // display the dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Filters")
                .setView(filterDialog)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()

            // on click listener for the clear filters button (filter selection dialog)
            filterClearFiltersBtn.setOnClickListener {
                // TODO: Implement clear filters button logic
                Log.d("MainActivity", "Clear Filters button clicked in the filter selection dialog")
            }

            // on click listener for the edit pokemon button
            editPokemonBtn.setOnClickListener {
                // TODO: Implement edit pokemon button logic
                Log.d("MainActivity", "Edit Pokemon button clicked in the filter selection dialog")

                // inflate the pokemon selection dialog layout
                val selectPokemonDialog = layoutInflater.inflate(R.layout.pokemon_selection, null)

                // access the recycler views
                selectedPokemonRecyclerView = selectPokemonDialog.findViewById(R.id.selected_pokemon_recycler_view)
                pokemonListRecyclerView = selectPokemonDialog.findViewById(R.id.pokemon_recycler_view)

                // show the selected Pokemon section
                val selectedPokemonLayout = selectPokemonDialog.findViewById<LinearLayout>(R.id.selected_pokemon_layout)
                selectedPokemonLayout.visibility = View.VISIBLE

                // access the search bar
                val searchBar = selectPokemonDialog.findViewById<EditText>(R.id.search_pokemon)

                // set the span counts based on device orientation
                selectedPokemonRecyclerView.layoutManager = LinearLayoutManager(this)
                val spanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 5
                pokemonListRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

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

                pokemonListRecyclerView.layoutManager = layoutManager

                // create and show the dialog
                AlertDialog.Builder(this)
                    .setTitle("Select Pokémon")
                    .setView(selectPokemonDialog)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .show()

                lateinit var selectedPokemonAdapter: SelectedPokemonAdapter
                lateinit var pokemonListAdapter: PokemonSelectionAdapter

                // create the adapter for the selected pokemon recycler view
                selectedPokemonAdapter = SelectedPokemonAdapter(
                    selectedPokemon,
                    selectedPokemonForms,
                    onPokemonUnselected = { removedPokemon ->
                        selectedPokemon.removeAll { it.pokemonID == removedPokemon.pokemonID }
                        removedPokemon.forms.forEach { selectedPokemonForms.remove(it.formID) }
                        selectedPokemonAdapter.updateList(selectedPokemon)
                        pokemonListAdapter.updateSelectedPokemon(selectedPokemon)
                        pokemonListAdapter.notifyDataSetChanged()
                        setPokemon()
                    },
                    onSelectionChanged = {
                        setPokemon()
                    }
                )
                selectedPokemonRecyclerView.adapter = selectedPokemonAdapter

                // create the adapter for the main pokemon selection list
                pokemonListAdapter = PokemonSelectionAdapter(1, groupedPokemonList, selectedPokemon) { returnedPokemon ->
                    if (selectedPokemon.contains(returnedPokemon)) {
                        selectedPokemon.remove(returnedPokemon)
                        for (form in returnedPokemon.forms) {
                            selectedPokemonForms.remove(form.formID)
                        }
                    } else {
                        selectedPokemon.add(returnedPokemon)
                        selectedPokemonForms.add(returnedPokemon.forms.sortedBy { it.formID }[0].formID)
                    }
                    selectedPokemonAdapter.updateList(selectedPokemon)
                    setPokemon()
                }
                pokemonListRecyclerView.adapter = pokemonListAdapter


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
                        (pokemonListRecyclerView.adapter as PokemonSelectionAdapter).updateList(updatedList)

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

            // on click listener for the edit origin games button
            editOriginGamesBtn.setOnClickListener {
                // TODO: Implement edit origin games button logic
                Log.d("MainActivity", "Edit Origin Games button clicked in the filter selection dialog")
            }

            // on click listener for the in progress radio button
            inProgressRadioBtn.setOnClickListener {
                Log.d("MainActivity", "In Progress radio button clicked in the filter selection dialog")
                selectedCompletionStatus = CompletionStatus.IN_PROGRESS
                setCurrentGames()
                setFinishDateLayout()
            }

            // on click listener for the completed radio button
            completedRadioBtn.setOnClickListener {
                Log.d("MainActivity", "Completed radio button clicked in the filter selection dialog")
                selectedCompletionStatus = CompletionStatus.COMPLETE
                setCurrentGames()
                setFinishDateLayout()
            }

            // on click listener for the both radio button
            bothRadioBtn.setOnClickListener {
                Log.d("MainActivity", "Both radio button clicked in the filter selection dialog")
                selectedCompletionStatus = CompletionStatus.BOTH
                setCurrentGames()
                setFinishDateLayout()
            }

            // on click listener for the edit current games button
            editCurrentGamesBtn.setOnClickListener {
                // TODO: Implement edit current games button logic
                Log.d("MainActivity", "Edit Current Games button clicked in the filter selection dialog")
            }

            // text change listener for the method
            method.doAfterTextChanged { enteredMethod = method.text.toString() }

            // on click listener for the start date from button
            startDateFromBtn.setOnClickListener {
                Log.d("MainActivity", "Start Date From button clicked in the filter selection dialog")

                // create a calendar instance
                val c = Calendar.getInstance()

                // get the day, month, and year from the calendar
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // create a dialog for the date picker
                Log.d("MainActivity", "Creating date picker dialog")
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // format and set the text for the start date
                        selectedStartDateFrom =
                            (buildString {
                                append(selectedYear.toString())
                                append("-")
                                append((selectedMonth + 1).toString())
                                append("-")
                                append(selectedDay.toString())
                            })
                        startDateFromBtn.text = selectedStartDateFrom
                        Log.d("MainActivity", "Selected Start Date From: ${startDateFromBtn.text}")
                    },
                    // pass the year, month, and day for the selected date
                    year,
                    month,
                    day
                )

                // display the date picker dialog
                datePickerDialog.show()
            }

            // on click listener for the start date to button
            startDateToBtn.setOnClickListener {
                Log.d("MainActivity", "Start Date To button clicked in the filter selection dialog")

                // create a calendar instance
                val c = Calendar.getInstance()

                // get the day, month, and year from the calendar
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // create a dialog for the date picker
                Log.d("MainActivity", "Creating date picker dialog")
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // format and set the text for the start date
                        selectedStartDateTo =
                            (buildString {
                                append(selectedYear.toString())
                                append("-")
                                append((selectedMonth + 1).toString())
                                append("-")
                                append(selectedDay.toString())
                            })
                        startDateToBtn.text = selectedStartDateTo
                        Log.d("MainActivity", "Selected Start Date To: ${startDateToBtn.text}")
                    },
                    // pass the year, month, and day for the selected date
                    year,
                    month,
                    day
                )

                // display the date picker dialog
                datePickerDialog.show()
            }

            // on click listener for the finish date from button
            finishDateFromBtn.setOnClickListener {
                Log.d("MainActivity", "Finish Date From button clicked in the filter selection dialog")

                // create a calendar instance
                val c = Calendar.getInstance()

                // get the day, month, and year from the calendar
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // create a dialog for the date picker
                Log.d("MainActivity", "Creating date picker dialog")
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // format and set the text for the finish date
                        selectedFinishDateFrom =
                            (buildString {
                                append(selectedYear.toString())
                                append("-")
                                append((selectedMonth + 1).toString())
                                append("-")
                                append(selectedDay.toString())
                            })
                        finishDateFromBtn.text = selectedFinishDateFrom
                        Log.d("MainActivity", "Selected Finish Date From: ${finishDateFromBtn.text}")
                    },
                    // pass the year, month, and day for the selected date
                    year,
                    month,
                    day
                )

                // display the date picker dialog
                datePickerDialog.show()
            }

            // on click listener for the finish date to button
            finishDateToBtn.setOnClickListener {
                Log.d("MainActivity", "Finish Date To button clicked in the filter selection dialog")

                // create a calendar instance
                val c = Calendar.getInstance()

                // get the day, month, and year from the calendar
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // create a dialog for the date picker
                Log.d("MainActivity", "Creating date picker dialog")
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // format and set the text for the finish date
                        selectedFinishDateTo =
                            (buildString {
                                append(selectedYear.toString())
                                append("-")
                                append((selectedMonth + 1).toString())
                                append("-")
                                append(selectedDay.toString())
                            })
                        finishDateToBtn.text = selectedFinishDateTo
                        Log.d("MainActivity", "Selected Finish Date To: ${finishDateToBtn.text}")
                    },
                    // pass the year, month, and day for the selected date
                    year,
                    month,
                    day
                )

                // display the date picker dialog
                datePickerDialog.show()
            }

            // text change listeners for the counter and phase ranges
            counterLo.doAfterTextChanged { enteredCounterLo = counterLo.text.toString() }
            counterHi.doAfterTextChanged { enteredCounterHi = counterHi.text.toString() }
            phaseLo.doAfterTextChanged { enteredPhaseLo = phaseLo.text.toString() }
            phaseHi.doAfterTextChanged { enteredPhaseHi = phaseHi.text.toString() }

            // on click listener for the confirm filters button
            confirmFiltersBtn.setOnClickListener {
                // TODO: Implement the confirm filters button logic
                Log.d("MainActivity", "Confirm Filters button clicked in the filter selection dialog")

                // check if a completion status was selected
                if (selectedCompletionStatus == null) {
                    AlertDialog.Builder(this)
                        .setTitle("Empty Field Detected")
                        .setMessage("Please select a completion status!")
                        .setPositiveButton("Okay") { errorDialog, _ ->
                            // close the error dialog
                            errorDialog.dismiss()
                        }
                        .show()
                }
                // otherwise, apply the filters
                else {
                    dialog.dismiss()

                    // set all of the confirmed filters
                    confirmedPokemonFormsFilter = selectedPokemonForms
                    confirmedOriginGamesFilter = selectedOriginGames
                    confirmedCompletionStatusFilter = selectedCompletionStatus
                    confirmedCurrentGamesFilter = selectedCurrentGames
                    confirmedMethodFilter = enteredMethod
                    confirmedStartDateFromFilter = selectedStartDateFrom
                    confirmedStartDateToFilter = selectedStartDateTo
                    confirmedFinishDateFromFilter = selectedFinishDateFrom
                    confirmedFinishDateToFilter = selectedFinishDateTo
                    confirmedCounterLoFilter = enteredCounterLo
                    confirmedCounterHiFilter = enteredCounterHi
                    confirmedPhaseLoFilter = enteredPhaseLo
                    confirmedPhaseHiFilter = enteredPhaseHi

                    Log.d("MainActivity", "sortMethod: $currentSortMethodIndex")
                    Log.d("MainActivity", "sortOrder: ${currentSortOrders[0]}")
                    Log.d("MainActivity", "formIDs: $confirmedPokemonFormsFilter")
                    Log.d("MainActivity", "originGameIDs: $confirmedOriginGamesFilter")
                    Log.d("MainActivity", "currentGameIDs: $confirmedCurrentGamesFilter")
                    Log.d("MainActivity", "method: $confirmedMethodFilter")
                    Log.d("MainActivity", "startedFrom: $confirmedStartDateFromFilter")
                    Log.d("MainActivity", "startedTo: $confirmedStartDateToFilter")
                    Log.d("MainActivity", "finishedFrom: $confirmedFinishDateFromFilter")
                    Log.d("MainActivity", "finishedTo: $confirmedFinishDateToFilter")
                    Log.d("MainActivity", "counterLo: $confirmedCounterLoFilter")
                    Log.d("MainActivity", "counterHi: $confirmedCounterHiFilter")
                    Log.d("MainActivity", "phaseLo: $confirmedPhaseLoFilter")
                    Log.d("MainActivity", "phaseHi: $confirmedPhaseHiFilter")
                    Log.d("MainActivity", "completionStatus: $confirmedCompletionStatusFilter")

                    // get the new shiny hunt data set
                    val filteredHunts = getFilteredAndSortedHunts()

                    // update the recycler view
                    shinyHuntListAdapter.submitList(filteredHunts) {
                        shinyHuntRecyclerView.scrollToPosition(0)
                    }
                }
            }

        }

        // on click listener for the clear filters button (main page)
        mainClearFiltersBtn.setOnClickListener {
            // TODO: Implement clear filters button logic
        }

        // on click listener for the expand all checkbox
        expandAllCheckbox.setOnClickListener {
            if (expandAllCheckbox.isChecked) {
                shinyHuntListAdapter.expandAll()
            } else {
                shinyHuntListAdapter.collapseAll()
            }
        }

        // Helper function to open the Sort Menu
        fun openSortMenu() {
            Log.d("MainActivity", "openSortMenu() started")

            // hide the floating sort button
            sortBtn.visibility = View.GONE

            // show the sort menu
            sortMenu.visibility = View.VISIBLE

            Log.d("MainActivity", "openSortMenu() completed")
        }

        // Helper function to close the Sort Menu
        fun closeSortMenu() {
            Log.d("MainActivity", "closeSortMenu() started")

            // hide the sort menu
            sortMenu.visibility = View.GONE

            // show the floating sort button
            sortBtn.visibility = View.VISIBLE

            Log.d("MainActivity", "closeSortMenu() completed")
        }

        // on click listener for the floating sort button
        sortBtn.setOnClickListener {
            Log.d("MainActivity", "Floating sort button clicked")

            // Initialize the text and background for each of the sort buttons (append an up or down arrow to the current sort method)
            if (currentSortMethodIndex == 0) {  // default
                sortDefaultBtn.setBackgroundResource(R.drawable.ui_container_transparent)
                sortDefaultBtn.text = if (currentSortOrders[0] == SortOrder.DESC) "Default ↓" else "Default ↑"
            } else {
                sortDefaultBtn.setBackgroundColor(0x00000000)
                sortDefaultBtn.text = "Default"
            }

            if (currentSortMethodIndex == 1) {  // by start date
                sortStartDateBtn.setBackgroundResource(R.drawable.ui_container_transparent)
                sortStartDateBtn.text = if (currentSortOrders[1] == SortOrder.DESC) "Start Date ↓" else "Start Date ↑"
            } else {
                sortStartDateBtn.setBackgroundColor(0x00000000)
                sortStartDateBtn.text = "Start Date"
            }

            if (currentSortMethodIndex == 2) {  // by finish date
                sortFinishDateBtn.setBackgroundResource(R.drawable.ui_container_transparent)
                sortFinishDateBtn.text = if (currentSortOrders[2] == SortOrder.DESC) "Finish Date ↓" else "Finish Date ↑"
            } else {
                sortFinishDateBtn.setBackgroundColor(0x00000000)
                sortFinishDateBtn.text = "Finish Date"
            }

            if (currentSortMethodIndex == 3) {  // by name
                sortNameBtn.setBackgroundResource(R.drawable.ui_container_transparent)
                sortNameBtn.text = if (currentSortOrders[3] == SortOrder.DESC) "Name ↓" else "Name ↑"
            } else {
                sortNameBtn.setBackgroundColor(0x00000000)
                sortNameBtn.text = "Name"
            }

            if (currentSortMethodIndex == 4) {  // by generation
                sortGenerationBtn.setBackgroundResource(R.drawable.ui_container_transparent)
                sortGenerationBtn.text = if (currentSortOrders[4] == SortOrder.DESC) "Generation ↓" else "Generation ↑"
            } else {
                sortGenerationBtn.setBackgroundColor(0x00000000)
                sortGenerationBtn.text = "Generation"
            }

            // open the sort menu
            openSortMenu()

        }

        // on click listener for the sort menu back button
        sortBackBtn.setOnClickListener {
            Log.d("MainActivity", "Sort menu back button clicked. Closing the sort menu")

            // simply close the sort menu
            closeSortMenu()
        }

        // on click listener for the sort by default button
        sortDefaultBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by default button clicked")

            // invert the sort order if current sort method is DEFAULT (0)
            if (currentSortMethod == SortMethod.DEFAULT) {
                currentSortOrders[0] = if (currentSortOrders[0] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method to DEFAULT (0)
                currentSortMethod = SortMethod.DEFAULT
                currentSortMethodIndex = 0
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[0] == SortOrder.DESC) "DF ↓" else "DF ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = getFilteredAndSortedHunts()

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.DEFAULT, currentSortOrders[0])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        // on click listener for the sort by start date button
        sortStartDateBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by start date button clicked")

            // invert the sort order if current sort method is by DATE_STARTED (1)
            if (currentSortMethodIndex == 1) {
                currentSortOrders[1] = if (currentSortOrders[1] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method to DATE_STARTED (1)
                currentSortMethod = SortMethod.DATE_STARTED
                currentSortMethodIndex = 1
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[1] == SortOrder.DESC) "SD ↓" else "SD ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = getFilteredAndSortedHunts()

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.DATE_STARTED, currentSortOrders[1])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        // on click listener for the sort by finish date button
        sortFinishDateBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by finish date button clicked")

            // invert the sort order if current sort method is by DATE_FINISHED (2)
            if (currentSortMethodIndex == 2) {
                currentSortOrders[2] = if (currentSortOrders[2] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method to DATE_FINISHED (2)
                currentSortMethod = SortMethod.DATE_FINISHED
                currentSortMethodIndex = 2
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[2] == SortOrder.DESC) "FD ↓" else "FD ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = getFilteredAndSortedHunts()

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.DATE_FINISHED, currentSortOrders[2])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        // on click listener for the sort by name button
        sortNameBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by name button clicked")

            // invert the sort order if current sort method is by NAME (3)
            if (currentSortMethodIndex == 3) {
                currentSortOrders[3] = if (currentSortOrders[3] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method to NAME (3)
                currentSortMethod = SortMethod.NAME
                currentSortMethodIndex = 3
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[3] == SortOrder.DESC) "NA ↓" else "NA ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = getFilteredAndSortedHunts()

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.NAME, currentSortOrders[3])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        // on click listener for the sort by generation button
        sortGenerationBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by generation button clicked")

            // invert the sort order if current sort method is by GENERATION (4)
            if (currentSortMethodIndex == 4) {
                currentSortOrders[4] = if (currentSortOrders[4] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method to GENERATION (4)
                currentSortMethod = SortMethod.GENERATION
                currentSortMethodIndex = 4
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[4] == SortOrder.DESC) "GE ↓" else "GE ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = getFilteredAndSortedHunts()

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.GENERATION, currentSortOrders[4])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        Log.d("MainActivity", "onCreate() completed")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("MainActivity", "onConfigurationChanged() started")
        super.onConfigurationChanged(newConfig)

        // update the margins of the floating sort button
        val sortBtnParams = sortBtn.layoutParams as FrameLayout.LayoutParams
        val sortMenuParams = sortMenuLayout.layoutParams as FrameLayout.LayoutParams

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // set margins for landscape
            sortBtnParams.setMargins(0, 0, 150, 50)
            sortMenuParams.setMargins(0, 0, 150, 0)
        } else {
            // set margins for portrait
            sortBtnParams.setMargins(0, 0, 20, 150)
            sortMenuParams.setMargins(0, 0, 0, 0)
        }

        sortBtn.layoutParams = sortBtnParams
        sortMenuLayout.layoutParams = sortMenuParams

        val layoutManager = shinyHuntRecyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutManager.spanCount = 2
                Log.d("MainActivity", "Orientation is LANDSCAPE, spanCount set to 2")
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager.spanCount = 1
                Log.d("MainActivity", "Orientation is PORTRAIT, spanCount set to 1")
            }
        }

        Log.d("MainActivity", "onConfigurationChanged() completed")
    }

    // Helper function for filtering/sorting the shiny hunts
    private fun getFilteredAndSortedHunts(): List<ShinyHunt> {
        Log.d("MainActivity", "getFilteredAndSortedHunts() started")

        // get shiny hunts from the database using the applied filters and sorting method
        val hunts = db.getHunts(
            sortMethod = currentSortMethod,
            sortOrder = currentSortOrders[currentSortMethodIndex],
            formIDs = confirmedPokemonFormsFilter.toList(),
            originGameIDs = confirmedOriginGamesFilter.toList(),
            currentGameIDs = if (confirmedCompletionStatusFilter == CompletionStatus.IN_PROGRESS) emptyList() else confirmedCurrentGamesFilter.toList(),
            method = confirmedMethodFilter.ifBlank { null },
            startedFrom = confirmedStartDateFromFilter.ifBlank { null },
            startedTo = confirmedStartDateToFilter.ifBlank { null },
            finishedFrom = if (confirmedFinishDateFromFilter.isBlank() || confirmedCompletionStatusFilter == CompletionStatus.IN_PROGRESS) null else confirmedFinishDateFromFilter,
            finishedTo = if (confirmedFinishDateToFilter.isBlank() || confirmedCompletionStatusFilter == CompletionStatus.IN_PROGRESS) null else confirmedFinishDateToFilter,
            counterLo = if (confirmedCounterLoFilter.isDigitsOnly() && confirmedCounterLoFilter.isNotEmpty()) confirmedCounterLoFilter.toInt() else null,
            counterHi = if (confirmedCounterHiFilter.isDigitsOnly() && confirmedCounterHiFilter.isNotEmpty()) confirmedCounterHiFilter.toInt() else null,
            phaseLo = if (confirmedPhaseLoFilter.isDigitsOnly() && confirmedPhaseLoFilter.isNotEmpty()) confirmedPhaseLoFilter.toInt() else null,
            phaseHi = if (confirmedPhaseHiFilter.isDigitsOnly() && confirmedPhaseHiFilter.isNotEmpty()) confirmedPhaseHiFilter.toInt() else null,
            completionStatus = if (confirmedCompletionStatusFilter == null) CompletionStatus.BOTH else confirmedCompletionStatusFilter!!
        )

        Log.d("MainActivity", "getFilteredAndSortedHunts() completed. Returning the list of shiny hunts")
        return hunts
    }

}