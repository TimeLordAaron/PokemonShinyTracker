package com.example.pokemonshinytracker

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
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

class MainActivity : ComponentActivity(), AdapterView.OnItemSelectedListener {

    // lateinit UI declarations: main UI
    private lateinit var newHuntBtn: ImageButton                // new hunt button
    private lateinit var sortBtn: ImageButton                   // sort button
    private lateinit var filterBtn: ImageButton                 // filter button
    private lateinit var counterMultiplierBtn: Button        // counter multiplier button
    private lateinit var expandAllCheckbox: CheckBox            // expand all checkbox
    private lateinit var shinyHuntRecyclerView: RecyclerView    // shiny hunt recycler view

    // lateinit UI declarations: sort selection UI
    private lateinit var sortMethodSpinner: Spinner             // sort method spinner
    private lateinit var sortOrdersRadioGrp: RadioGroup         // sort orders radio group
    private lateinit var ascendingOrderRadioBtn: RadioButton    // ascending order radio button
    private lateinit var descendingOrderRadioBtn: RadioButton   // descending order radio button
    private lateinit var confirmSortBtn: Button                 // confirm sort button

    // lateinit UI declarations: filter selection UI
    private lateinit var filterClearFiltersBtn: Button          // clear filters button (filter selection dialog)
    private lateinit var editPokemonBtn: Button                 // edit pokemon button
    private lateinit var pokemonTxt: TextView                   // selected pokemon text
    private lateinit var selectedPokemonRecyclerView: RecyclerView  // selected pokemon recycler view
    private lateinit var pokemonListRecyclerView: RecyclerView  // pokemon list recycler view
    private lateinit var editOriginGamesBtn: Button             // edit origin games button
    private lateinit var originGamesTxt: TextView               // selected origin games text
    private lateinit var gamesRecyclerView: RecyclerView        // game recycler view (used for origin and current game)
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

    // lateinit UI declarations: counter multiplier UI
    private lateinit var counterMultiplierText: TextView        // counter multiplier text
    private lateinit var counterMultiplierDecrementBtn: ImageButton // counter multiplier decrement button
    private lateinit var counterMultiplierIncrementBtn: ImageButton // counter multiplier increment button

    // initialize variables for tracking the current sort method
    // 0: Default, 1: Start Date, 2: Finish Date, 3: Name, 4: Generation
    private var currentSortMethod = SortMethod.DEFAULT
    private var currentSortMethodIndex = 0
    private var currentSortOrder = SortOrder.DESC

    // initialize variables for tracking the selected filters
    private var selectedPokemon = mutableListOf<Pokemon>()           // dataset for the selected pokemon RecyclerView
    private var selectedPokemonForms = mutableSetOf<Int>()           // formIDs of every selected Pokemon Form
    private var selectedOriginGames = mutableListOf<Int>()           // gameIDs of every selected Origin Game
    private var selectedCompletionStatus: CompletionStatus? = null
    private var selectedCurrentGames = mutableListOf<Int>()          // gameIDs of every selected Current Game
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

    // variables to track if a sub menu is currently open
    private var sortMenuOpened = false
    private var filterMenuOpened = false
    private var subMenuOpened = false
    private var individualHuntOpening = false

    // variables for inflating the selection dialogs in the filter menu
    private lateinit var selectPokemonDialogLayout: View
    private lateinit var selectGamesDialogLayout: View

    // database helper
    private val db = DBHelper(this, null)

    // dialog handler
    private val dh = DialogHandler()

    // application instance
    private val ma = (application as MyApplication)

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
        val hunts = db.getHunts()           // retrieve all saved shiny hunts from the database
        Log.d("MainActivity", "Retrieved ${hunts.size} shiny hunts from database")

        // access the main UI elements
        val noHuntsMessage = findViewById<TextView>(R.id.no_hunts_message)                      // message for when user has no saved hunts
        shinyHuntRecyclerView = findViewById(R.id.shiny_hunts_recycler_view)                    // recycler view that displays the user's saved hunts
        newHuntBtn = findViewById(R.id.new_hunt_button)                                         // new hunt button
        filterBtn = findViewById(R.id.filter_button)                                            // filter button
        counterMultiplierBtn = findViewById(R.id.counter_multiplier_button)                     // counter multiplier button
        expandAllCheckbox = findViewById(R.id.expand_all_checkbox)                              // expand all checkbox
        sortBtn = findViewById(R.id.sort_button)                                                // floating sort button

        // set the text of the counter multiplier button
        counterMultiplierBtn.text = String.format("x%s", ma.counterMultiplier)

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

        // set up the callback functions of the adapter
        shinyHuntListAdapter.onScrollToPosition = { position ->
            // when swapping shiny hunts, scroll to the position of the shiny hunt that initiated the swap
            shinyHuntRecyclerView.scrollToPosition(position)
        }

        shinyHuntListAdapter.onExpandStateChanged = { allExpanded ->
            // check the expand all checkbox if all shiny hunts are currently expanded
            expandAllCheckbox.isChecked = allExpanded
        }

        shinyHuntListAdapter.onEditHuntRequested = { huntToEdit ->
            // check that a sub menu isn't currently open (to prevent the user from opening multiple dialogs at once)
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                individualHuntOpening = true

                // switch to the detailed view of the shiny hunt
                val intent = Intent(this, IndividualHunt::class.java).apply {
                    putExtra("hunt_id", huntToEdit.huntID)
                }

                Log.d("ShinyHuntListAdapter", "Created intent for selected hunt. Switching to Individual Hunt window")
                this.startActivity(intent)
            }
        }

        shinyHuntListAdapter.onDeleteHuntRequested = { huntToDelete ->
            // check that a sub menu isn't currently open (to prevent the user from opening multiple dialogs at once)
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                subMenuOpened = true

                val pokemon = pokemonList.find { p -> p.forms.any { it.formID == huntToDelete.formID } }
                val formName = pokemon?.pokemonName ?: "this Pokémon"

                val deleteDialog = AlertDialog.Builder(this)
                    .setTitle("Delete Shiny Hunt")
                    .setMessage("Are you sure you want to delete the shiny hunt for $formName?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.deleteHunt(huntToDelete.huntID)  // delete the hunt from the database

                        val updatedList = shinyHuntListAdapter.currentList.toMutableList().apply {
                            remove(huntToDelete)    // delete the hunt visually
                        }
                        shinyHuntListAdapter.submitList(updatedList)

                        if (updatedList.isEmpty()) {
                            noHuntsMessage.visibility = View.VISIBLE
                            shinyHuntRecyclerView.visibility = View.GONE
                        }
                        subMenuOpened = false
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        subMenuOpened = false
                    }
                    .create()

                // listener for when the dialog is dismissed (includes CANCEL and outside taps)
                deleteDialog.setOnCancelListener { subMenuOpened = false }

                // listener for when user presses back or taps outside
                deleteDialog.setOnDismissListener { subMenuOpened = false }

                // display the deletion dialog
                deleteDialog.show()
            }
        }

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

        /* Filter Menu: Selection Dialogs */
        // inflate layouts of the pokemon and game selection dialogs (for the filter menu)
        selectPokemonDialogLayout = layoutInflater.inflate(R.layout.pokemon_selection, null)
        selectGamesDialogLayout = layoutInflater.inflate(R.layout.game_selection, null)

        // access the recycler views
        selectedPokemonRecyclerView = selectPokemonDialogLayout.findViewById(R.id.selected_pokemon_recycler_view)
        pokemonListRecyclerView = selectPokemonDialogLayout.findViewById(R.id.pokemon_recycler_view)
        gamesRecyclerView = selectGamesDialogLayout.findViewById(R.id.game_recycler_view)

        // set up a recycled view pool for the pokemon recycler view so it doesn't have to reinflate all the views each time the dialog is opened
        val pokemonViewPool = RecyclerView.RecycledViewPool()
        pokemonListRecyclerView.setRecycledViewPool(pokemonViewPool)

        // show the selected Pokemon section
        val selectedPokemonLayout = selectPokemonDialogLayout.findViewById<LinearLayout>(R.id.selected_pokemon_layout)
        selectedPokemonLayout.visibility = View.VISIBLE

        // on click listener for the new hunt button
        newHuntBtn.setOnClickListener {
            Log.d("MainActivity", "New Hunt button clicked. Preparing to create a new shiny hunt")

            // don't navigate to the IndividualHunt page if a sub menu is opened
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                individualHuntOpening = true

                // set up an intent (using 0 as a placeholder for the hunt ID)
                val intent = Intent(this, IndividualHunt::class.java).apply {
                    putExtra("hunt_id", 0)
                }
                Log.d("MainActivity", "Created intent for a new hunt. Switching to IndividualHunt")

                // switch to IndividualHunt
                this.startActivity(intent)
            }
        }

        // on click listener for the sort button
        sortBtn.setOnClickListener {
            Log.d("MainActivity", "Sort button clicked")

            // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                sortMenuOpened = true

                val sortDialogLayout = layoutInflater.inflate(R.layout.sort_selection, null)

                // access all the UI elements
                sortMethodSpinner =
                    sortDialogLayout.findViewById(R.id.sort_method_spinner)                 // sort method spinner
                sortOrdersRadioGrp =
                    sortDialogLayout.findViewById(R.id.sort_orders_radio_group)            // sort orders radio group
                ascendingOrderRadioBtn =
                    sortDialogLayout.findViewById(R.id.ascending_radio_button)         // ascending radio button (for sort order)
                descendingOrderRadioBtn =
                    sortDialogLayout.findViewById(R.id.descending_radio_button)       // descending radio button (for sort order)
                confirmSortBtn =
                    sortDialogLayout.findViewById(R.id.confirm_sort_button)                    // confirm sort button

                // create an adapter for the sort method spinner
                ArrayAdapter.createFromResource(
                    this,
                    R.array.sort_methods_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    // set the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
                    // apply the adapter to the spinner
                    sortMethodSpinner.adapter = adapter
                }

                // apply the on item selected listener (which handles the logic for item selection in the spinner)
                sortMethodSpinner.onItemSelectedListener = this

                // set the current selection in the spinner with the current method
                sortMethodSpinner.setSelection(currentSortMethodIndex)

                // set the current selection in the sort orders radio button group
                sortOrdersRadioGrp.check(
                    if (currentSortOrder == SortOrder.ASC) R.id.ascending_radio_button else R.id.descending_radio_button
                )

                // on click listener for the ascending radio button
                ascendingOrderRadioBtn.setOnClickListener { currentSortOrder = SortOrder.ASC }

                // on click listener for the descending radio button
                descendingOrderRadioBtn.setOnClickListener { currentSortOrder = SortOrder.DESC }

                // create the sort menu dialog
                val sortDialog = dh.createLayoutDialog(this, "Sort", sortDialogLayout) {
                    sortMenuOpened = false  // on close, unset sortMenuOpened
                }

                // on click listener for the confirm sort button
                confirmSortBtn.setOnClickListener {
                    Log.d("MainActivity", "Confirm Sort button clicked")
                    Log.d("MainActivity", "Sort Method: $currentSortMethod")
                    Log.d("MainActivity", "Sort Order: $currentSortOrder")

                    // get the new shiny hunt data set
                    val sortedHunts = getFilteredAndSortedHunts()

                    // update the sort method and order in the adapter
                    shinyHuntListAdapter.updateSortMethod(currentSortMethod, currentSortOrder)

                    // update the recycler view
                    shinyHuntListAdapter.submitList(sortedHunts) {
                        shinyHuntRecyclerView.scrollToPosition(0)
                    }

                    // close the dialog
                    sortDialog.dismiss()
                    sortMenuOpened = false
                }
            }
        }

        /* Helper functions for setting UI in the filter selection dialog */
        // selected pokemon
        fun setPokemon() {
            if (selectedPokemonForms.size == 1) {
                pokemonTxt.text = "1 Pokémon form selected."
            } else {
                pokemonTxt.text = "${selectedPokemonForms.size} Pokémon forms selected."
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

            // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                filterMenuOpened = true

                val filterDialogLayout = layoutInflater.inflate(R.layout.filter_selection, null)

                // access all the UI elements
                filterClearFiltersBtn = filterDialogLayout.findViewById(R.id.clear_filters_button)      // clear filters button (filter selection dialog)
                editPokemonBtn = filterDialogLayout.findViewById(R.id.edit_pokemon_button)              // edit pokemon button
                pokemonTxt = filterDialogLayout.findViewById(R.id.pokemon_text)                         // selected pokemon text
                editOriginGamesBtn = filterDialogLayout.findViewById(R.id.edit_origin_games_button)     // edit origin games button
                originGamesTxt = filterDialogLayout.findViewById(R.id.origin_games_text)                // selected origin games text
                completionStatusRadioGrp = filterDialogLayout.findViewById(R.id.completion_statuses_radio_group)  // completion status radio group
                inProgressRadioBtn = filterDialogLayout.findViewById(R.id.in_progress_radio_button)     // in progress radio button (for completion status)
                completedRadioBtn = filterDialogLayout.findViewById(R.id.completed_radio_button)        // completed radio button (for completion status)
                bothRadioBtn = filterDialogLayout.findViewById(R.id.both_radio_button)                  // both radio button (for completion status)
                currentGamesLayout = filterDialogLayout.findViewById(R.id.current_games_layout)         // current games layout
                editCurrentGamesBtn = filterDialogLayout.findViewById(R.id.edit_current_games_button)   // edit current games button
                currentGamesTxt = filterDialogLayout.findViewById(R.id.current_games_text)              // selected current games text
                method = filterDialogLayout.findViewById(R.id.method)                                   // method text field
                startDateFromBtn = filterDialogLayout.findViewById(R.id.start_date_from_button)         // start date from button
                startDateToBtn = filterDialogLayout.findViewById(R.id.start_date_to_button)             // start date to button
                finishDateLayout = filterDialogLayout.findViewById(R.id.finish_date_layout)             // finish date range layout
                finishDateFromBtn = filterDialogLayout.findViewById(R.id.finish_date_from_button)       // finish date from button
                finishDateToBtn = filterDialogLayout.findViewById(R.id.finish_date_to_button)           // finish date to button
                counterLo = filterDialogLayout.findViewById(R.id.counter_lo)                            // counter (low bound) text field
                counterHi = filterDialogLayout.findViewById(R.id.counter_hi)                            // counter (high bound) text field
                phaseLo = filterDialogLayout.findViewById(R.id.phase_lo)                                // phase (low bound) text field
                phaseHi = filterDialogLayout.findViewById(R.id.phase_hi)                                // phase (high bound) text field
                confirmFiltersBtn = filterDialogLayout.findViewById(R.id.confirm_filters_button)        // confirm filters button

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

                // create the filter menu dialog
                val filterDialog = dh.createLayoutDialog(this, "Filters", filterDialogLayout) {
                    filterMenuOpened = false    // on close, unset filterMenuOpened
                }

                // on click listener for the clear filters button (filter selection dialog)
                filterClearFiltersBtn.setOnClickListener {
                    Log.d("MainActivity", "Clear Filters button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // create a confirmation dialog
                        dh.createConfirmationDialog(this, "Clear All Filters?", "Are you sure you want to clear all currently applied filters?", {
                            // logic for Yes button
                            // reset all the filters (selected and confirmed)
                            selectedPokemon.clear()
                            selectedPokemonForms.clear()
                            confirmedPokemonFormsFilter.clear()
                            selectedOriginGames.clear()
                            confirmedOriginGamesFilter.clear()
                            selectedCompletionStatus = null
                            confirmedCompletionStatusFilter = null
                            selectedCurrentGames.clear()
                            confirmedCurrentGamesFilter.clear()
                            enteredMethod = ""
                            confirmedMethodFilter = ""
                            selectedStartDateFrom = ""
                            confirmedStartDateFromFilter = ""
                            selectedStartDateTo = ""
                            confirmedStartDateToFilter = ""
                            selectedFinishDateFrom = ""
                            confirmedFinishDateFromFilter = ""
                            selectedFinishDateTo = ""
                            confirmedFinishDateToFilter = ""
                            enteredCounterLo = ""
                            confirmedCounterLoFilter = ""
                            enteredCounterHi = ""
                            confirmedCounterHiFilter = ""
                            enteredPhaseLo = ""
                            confirmedPhaseLoFilter = ""
                            enteredPhaseHi = ""
                            confirmedPhaseHiFilter = ""

                            // get the unfiltered hunts
                            val unfilteredHunts = getFilteredAndSortedHunts()

                            // allow move buttons to be enabled
                            shinyHuntListAdapter.setMoveButtonsEnabled(true)

                            // automatically collapse all shiny hunts
                            expandAllCheckbox.isChecked = false
                            shinyHuntListAdapter.collapseAll()

                            // update the recycler view
                            shinyHuntListAdapter.submitList(unfilteredHunts) {
                                shinyHuntRecyclerView.scrollToPosition(0)
                            }

                            subMenuOpened = false

                            // close the filter dialog
                            filterDialog.dismiss()
                            filterMenuOpened = false
                        }, {
                            subMenuOpened = false   // on close, unset subMenuOpened
                        })
                    }
                }

                // on click listener for the edit pokemon button
                editPokemonBtn.setOnClickListener {
                    Log.d("MainActivity", "Edit Pokemon button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // set the span counts based on device orientation
                        selectedPokemonRecyclerView.layoutManager = LinearLayoutManager(this)
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
                        pokemonListRecyclerView.layoutManager = layoutManager

                        // detach the dialog layout from any previous parent before reattaching
                        (selectPokemonDialogLayout.parent as? ViewGroup)?.removeView(selectPokemonDialogLayout)

                        // create the pokemon selection dialog
                        dh.createLayoutDialog(this, "Select Pokémon", selectPokemonDialogLayout) {
                            subMenuOpened = false   // on close, unset subMenuOpened
                        }

                        // create the adapter for the selected Pokemon recycler view
                        selectedPokemonRecyclerView.adapter = SelectedPokemonAdapter(
                            selectedPokemon,
                            selectedPokemonForms,
                            onPokemonUnselected = { removedPokemon ->
                                selectedPokemon.removeAll { it.pokemonID == removedPokemon.pokemonID }
                                removedPokemon.forms.forEach { selectedPokemonForms.remove(it.formID) }
                                (selectedPokemonRecyclerView.adapter as SelectedPokemonAdapter).updateList(selectedPokemon)
                                (pokemonListRecyclerView.adapter as PokemonSelectionAdapter).updateSelectedPokemon(selectedPokemon)
                                (pokemonListRecyclerView.adapter as PokemonSelectionAdapter).notifyDataSetChanged()
                                setPokemon()
                            },
                            onSelectionChanged = {
                                setPokemon()
                            }
                        )

                        // create the adapter for the main Pokemon selection list
                        pokemonListRecyclerView.adapter = PokemonSelectionAdapter(
                            1,
                            groupedPokemonList,
                            selectedPokemon
                        ) { returnedPokemon ->
                            if (selectedPokemon.contains(returnedPokemon)) {
                                selectedPokemon.remove(returnedPokemon)
                                for (form in returnedPokemon.forms) {
                                    selectedPokemonForms.remove(form.formID)
                                }
                            } else {
                                selectedPokemon.add(returnedPokemon)
                                selectedPokemonForms.add(returnedPokemon.forms.sortedBy { it.formID }[0].formID)
                            }
                            (selectedPokemonRecyclerView.adapter as SelectedPokemonAdapter).updateList(selectedPokemon)
                            setPokemon()
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
                                (pokemonListRecyclerView.adapter as PokemonSelectionAdapter).updateList(
                                    updatedList
                                )

                                layoutManager.spanSizeLookup =
                                    object : GridLayoutManager.SpanSizeLookup() {
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

                // on click listener for the edit origin games button
                editOriginGamesBtn.setOnClickListener {
                    Log.d("MainActivity", "Edit Origin Games button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // get and apply the span count based on orientation
                        val gameSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT
                        gamesRecyclerView.layoutManager = GridLayoutManager(this, gameSpanCount)

                        // prepare dataset with headers
                        val groupedGameList = prepareGameListWithHeaders(gameList)

                        // custom span size logic to make headers span full width
                        (gamesRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                            object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (groupedGameList[position]) {
                                        is GameListItem.HeaderItem -> gameSpanCount  // header takes full row
                                        is GameListItem.GameItem -> 1            // game takes 1 column
                                    }
                                }
                            }

                        // detach the dialog layout from any previous parent before reattaching
                        (selectGamesDialogLayout.parent as? ViewGroup)?.removeView(selectGamesDialogLayout)

                        // create the origin games dialog
                        dh.createLayoutDialog(this, "Select the Origin Games", selectGamesDialogLayout) {
                            subMenuOpened = false   // on close, unset subMenuOpened
                        }

                        gamesRecyclerView.adapter =
                            GameSelectionAdapter(
                                2,
                                groupedGameList,
                                selectedOriginGames
                            ) { selectedGame ->
                                if (selectedOriginGames.contains(selectedGame.gameID - 1)) {    // gameIDs are one off
                                    selectedOriginGames.remove(selectedGame.gameID - 1)
                                } else {
                                    selectedOriginGames.add(selectedGame.gameID - 1)
                                }
                                setOriginGames()
                            }
                    }
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
                    Log.d("MainActivity", "Edit Current Games button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // get and apply the span count based on orientation
                        val gameSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT
                        gamesRecyclerView.layoutManager = GridLayoutManager(this, gameSpanCount)

                        // prepare dataset with headers
                        val groupedGameList = prepareGameListWithHeaders(gameList)

                        // custom span size logic to make headers span full width
                        (gamesRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                            object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (groupedGameList[position]) {
                                        is GameListItem.HeaderItem -> gameSpanCount  // header takes full row
                                        is GameListItem.GameItem -> 1            // game takes 1 column
                                    }
                                }
                            }

                        // detach the dialog layout from any previous parent before reattaching
                        (selectGamesDialogLayout.parent as? ViewGroup)?.removeView(selectGamesDialogLayout)

                        // create the current games dialog
                        dh.createLayoutDialog(this, "Select the Current Games", selectGamesDialogLayout) {
                            subMenuOpened = false   // on close, unset subMenuOpened
                        }

                        gamesRecyclerView.adapter =
                            GameSelectionAdapter(
                                3,
                                groupedGameList,
                                selectedCurrentGames
                            ) { selectedGame ->
                                if (selectedCurrentGames.contains(selectedGame.gameID - 1)) {    // gameIDs are one off
                                    selectedCurrentGames.remove(selectedGame.gameID - 1)
                                } else {
                                    selectedCurrentGames.add(selectedGame.gameID - 1)
                                }
                                setCurrentGames()
                            }
                    }
                }

                // text change listener for the method
                method.doAfterTextChanged { enteredMethod = method.text.toString() }

                // on click listener for the start date from button
                startDateFromBtn.setOnClickListener {
                    Log.d("MainActivity", "Start Date From button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // display the date picker
                        dh.createDatePickerDialog(this, {
                            // use the returned date string
                            selectedStartDateFrom = it
                            startDateFromBtn.text = it
                        }, {
                            // on close, unset subMenuOpened
                            subMenuOpened = false
                        })
                    }
                }

                // on click listener for the start date to button
                startDateToBtn.setOnClickListener {
                    Log.d("MainActivity", "Start Date To button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // display the date picker
                        dh.createDatePickerDialog(this, {
                            // use the returned date string
                            selectedStartDateTo = it
                            startDateToBtn.text = it
                        }, {
                            // on close, unset subMenuOpened
                            subMenuOpened = false
                        })
                    }
                }

                // on click listener for the finish date from button
                finishDateFromBtn.setOnClickListener {
                    Log.d("MainActivity", "Finish Date From button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // display the date picker
                        dh.createDatePickerDialog(this, {
                            // use the returned date string
                            selectedFinishDateFrom = it
                            finishDateFromBtn.text = it
                        }, {
                            // on close, unset subMenuOpened
                            subMenuOpened = false
                        })
                    }
                }

                // on click listener for the finish date to button
                finishDateToBtn.setOnClickListener {
                    Log.d("MainActivity", "Finish Date To button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // display the date picker
                        dh.createDatePickerDialog(this, {
                            // use the returned date string
                            selectedFinishDateTo = it
                            finishDateToBtn.text = it
                        }, {
                            // on close, unset subMenuOpened
                            subMenuOpened = false
                        })
                    }
                }

                // text change listeners for the counter and phase ranges
                counterLo.doAfterTextChanged { enteredCounterLo = counterLo.text.toString() }
                counterHi.doAfterTextChanged { enteredCounterHi = counterHi.text.toString() }
                phaseLo.doAfterTextChanged { enteredPhaseLo = phaseLo.text.toString() }
                phaseHi.doAfterTextChanged { enteredPhaseHi = phaseHi.text.toString() }

                // on click listener for the confirm filters button
                confirmFiltersBtn.setOnClickListener {
                    Log.d("MainActivity", "Confirm Filters button clicked in the filter selection dialog")

                    // check if a sub menu is already open (to prevent the user from spamming open multiple copies of it)
                    if (!subMenuOpened) {
                        subMenuOpened = true

                        // check if a completion status was selected
                        if (selectedCompletionStatus == null) {
                            // create the error dialog
                            dh.createErrorDialog(this, "Empty Field Detected", "Please select a completion status!") {
                                subMenuOpened = false   // on close, unset subMenuOpened
                            }
                        }
                        // otherwise, apply the filters
                        else {
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
                            Log.d("MainActivity", "sortOrder: $currentSortOrder")
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

                            // disable the move buttons
                            shinyHuntListAdapter.setMoveButtonsEnabled(false)

                            // update the recycler view
                            shinyHuntListAdapter.submitList(filteredHunts) {
                                shinyHuntRecyclerView.scrollToPosition(0)
                            }

                            subMenuOpened = false

                            // close the filter dialog
                            filterDialog.dismiss()
                            filterMenuOpened = false
                        }
                    }
                }
            }
        }

        // on click listener for the counter multiplier button
        counterMultiplierBtn.setOnClickListener {
            // check that a sub menu isn't currently open (to prevent the user from opening multiple dialogs at once)
            if (!subMenuOpened && !sortMenuOpened && !filterMenuOpened && !individualHuntOpening) {
                subMenuOpened = true

                val counterMultiplierLayout = layoutInflater.inflate(R.layout.counter_multiplier_editor, null)

                // access the UI elements
                counterMultiplierText = counterMultiplierLayout.findViewById(R.id.counter_multiplier_text)
                counterMultiplierDecrementBtn = counterMultiplierLayout.findViewById(R.id.counter_multiplier_decrement)
                counterMultiplierIncrementBtn = counterMultiplierLayout.findViewById(R.id.counter_multiplier_increment)

                // set the counter multiplier text
                counterMultiplierText.text = String.format("x%s", ma.counterMultiplier)

                // create a dialog for editing the counter multiplier
                dh.createLayoutDialog(this, "Set the Counter Multiplier", counterMultiplierLayout) {
                    subMenuOpened = false   // on close, unset subMenuOpened
                }

                // on click listener for the decrement button
                counterMultiplierDecrementBtn.setOnClickListener {
                    // decrement the value of the global counter multiplier, to a minimum of 1
                    ma.counterMultiplier = (ma.counterMultiplier - 1).coerceAtLeast(1)

                    // update the UI
                    counterMultiplierText.text = String.format("x%s", ma.counterMultiplier)     // text in dialog
                    counterMultiplierBtn.text = String.format("x%s", ma.counterMultiplier)      // button in main page
                }

                // on click listener for the increment button
                counterMultiplierIncrementBtn.setOnClickListener {
                    // increment the value of the global counter multiplier
                    ma.counterMultiplier++

                    // update the UI
                    counterMultiplierText.text = String.format("x%s", ma.counterMultiplier)     // text in dialog
                    counterMultiplierBtn.text = String.format("x%s", ma.counterMultiplier)      // button in main page
                }
            }
        }

        // on click listener for the expand all checkbox
        expandAllCheckbox.setOnClickListener {
            if (expandAllCheckbox.isChecked) {
                shinyHuntListAdapter.expandAll()
            } else {
                shinyHuntListAdapter.collapseAll()
            }
        }
    }

    // Function that handles when a sort method is selected in the "Sort By" spinner
    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        Log.d("MainActivity", "Item $position selected in the sort method spinner")

        // set the current sort method index to the currently selected index in the spinner
        currentSortMethodIndex = position

        // using the current sort method index, set the current sort method to the corresponding sort method
        // 0: Default, 1: Start Date, 2: Finish Date, 3: Name, 4: Generation
        currentSortMethod =
            when (currentSortMethodIndex) {
                0 -> SortMethod.DEFAULT
                1 -> SortMethod.DATE_STARTED
                2 -> SortMethod.DATE_FINISHED
                3 -> SortMethod.NAME
                4 -> SortMethod.GENERATION
                else -> SortMethod.DEFAULT      // if the index is somehow out of range, default to the DEFAULT sort method
            }
    }

    // Function that handles when nothing is selected in the "Sort By" spinner
    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("MainActivity", "Nothing selected in the sort method spinner")
    }

    // Function that is called when this activity is resumed after returning from another activity
    override fun onResume() {
        super.onResume()
        individualHuntOpening = false   // the IndividualHunt activity is now closed
    }

    // Function for adjusting the UI layout when the screen is rotated
    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("MainActivity", "onConfigurationChanged() started")
        super.onConfigurationChanged(newConfig)

        // update layout of the shiny hunt recycler view
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

        /* Filter Menu: Selection Dialogs */
        // get span counts based on device orientation
        val pokemonSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.POKEMON_SPAN_LANDSCAPE else MyApplication.POKEMON_SPAN_PORTRAIT
        val gameSpanCount = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) MyApplication.GAME_SPAN_LANDSCAPE else MyApplication.GAME_SPAN_PORTRAIT

        // reinitialize GridLayoutManagers with updated span counts
        val pokemonGridLayoutManager = GridLayoutManager(this, pokemonSpanCount)
        val gameGridLayoutManager = GridLayoutManager(this, gameSpanCount)

        // reset spanSizeLookup to ensure headers take up the full row
        pokemonGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((pokemonListRecyclerView.adapter as? PokemonSelectionAdapter)?.getItemViewType(position)) {
                    PokemonSelectionAdapter.VIEW_TYPE_HEADER -> pokemonSpanCount // header spans full row
                    PokemonSelectionAdapter.VIEW_TYPE_POKEMON -> 1               // Pokemon takes 1 column
                    else -> 1 // default fallback
                }
            }
        }

        gameGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((gamesRecyclerView.adapter as? GameSelectionAdapter)?.getItemViewType(position)) {
                    GameSelectionAdapter.VIEW_TYPE_HEADER -> gameSpanCount // header spans full row
                    GameSelectionAdapter.VIEW_TYPE_GAME -> 1               // game takes 1 column
                    else -> 1 // default fallback
                }
            }
        }

        // apply the updated layout managers
        pokemonListRecyclerView.layoutManager = pokemonGridLayoutManager
        gamesRecyclerView.layoutManager = gameGridLayoutManager

        Log.d("MainActivity", "onConfigurationChanged() completed")
    }

    // Helper function for filtering/sorting the shiny hunts
    private fun getFilteredAndSortedHunts(): List<ShinyHunt> {
        Log.d("MainActivity", "getFilteredAndSortedHunts() started")

        // get shiny hunts from the database using the applied filters and sorting method
        val hunts = db.getHunts(
            sortMethod = currentSortMethod,
            sortOrder = currentSortOrder,
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