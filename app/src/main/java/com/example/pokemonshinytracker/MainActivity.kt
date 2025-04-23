package com.example.pokemonshinytracker

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    // lateinit UI declarations
    private lateinit var newHuntBtn: Button             // new hunt button
    private lateinit var filterBtn: Button              // filter button
    private lateinit var clearFiltersBtn: Button        // clear filters button
    private lateinit var expandAllCheckbox: CheckBox    // expand all checkbox
    private lateinit var sortBtn: Button                // sort button
    private lateinit var sortDefaultBtn: Button         // sort by default button
    private lateinit var sortStartDateBtn: Button       // sort by start date button
    private lateinit var sortFinishDateBtn: Button      // sort by finish date button
    private lateinit var sortNameBtn: Button            // sort by name button
    private lateinit var sortGenerationBtn: Button      // sort by generation button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate() started")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // access the database
        val db = DBHelper(this, null)
        Log.d("MainActivity", "Database opened")

        // force update the database (for testing purposes)
        //db.forceUpgrade()
        //Log.d("MainActivity","Force updating the database")

        // initialize variables for tracking the current sort method
        // 0: Default, 1: Start Date, 2: Finish Date, 3: Name, 4: Generation
        var currentSortMethodIndex = 0          // default sort method is DEFAULT
        var currentSortOrders =
            arrayOf(SortOrder.DESC, SortOrder.DESC, SortOrder.DESC, SortOrder.ASC, SortOrder.ASC)

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
        val shinyHuntRecyclerView: RecyclerView = findViewById(R.id.shiny_hunts_recycler_view)  // recycler view that displays the user's saved hunts
        newHuntBtn = findViewById(R.id.new_hunt_button)                                         // new hunt button
        filterBtn = findViewById(R.id.filter_button)                                            // filter button
        clearFiltersBtn = findViewById(R.id.clear_filters_button)                               // clear filters button
        expandAllCheckbox = findViewById(R.id.expand_all_checkbox)                              // expand all checkbox
        sortBtn = findViewById(R.id.sort_button)                                                // floating sort button
        val sortMenu = findViewById<LinearLayout>(R.id.sort_menu)                               // sort menu
        sortDefaultBtn = findViewById(R.id.sort_default_button)                                 // sort by default button
        sortStartDateBtn = findViewById(R.id.sort_start_date_button)                            // sort by start date button
        sortFinishDateBtn = findViewById(R.id.sort_finish_date_button)                          // sort by finish date button
        sortNameBtn = findViewById(R.id.sort_name_button)                                       // sort by name button
        sortGenerationBtn = findViewById(R.id.sort_generation_button)                           // sort by generation button

        // instantiate an adapter for the shiny hunt recycler view
        val shinyHuntListAdapter = ShinyHuntListAdapter(this, pokemonList, gameList).apply {
            onScrollToPosition = { position ->
                shinyHuntRecyclerView.scrollToPosition(position)
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

        // on click listener for the filter button
        filterBtn.setOnClickListener {
            // TODO: Implement filter button logic
        }

        // on click listener for the clear filters button
        clearFiltersBtn.setOnClickListener {
            // TODO: Implement clear filters button logic
        }

        // on click listener for the expand all checkbox
        expandAllCheckbox.setOnClickListener {
            // TODO: Implement expand all checkbox logic
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

        // Helper function to unselect the current sort method
        fun unselectCurrentSortMethod() {
            Log.d("MainActivity", "unselectCurrentSortMethod() started")

            // find the currently selected button. set its background transparent and remove arrow from the text
            if (currentSortMethodIndex == 0) {  // default
                sortDefaultBtn.setBackgroundResource(0x00000000)
                sortDefaultBtn.text = "Default"
            }
            else if (currentSortMethodIndex == 1) { // by start date
                sortStartDateBtn.setBackgroundResource(0x00000000)
                sortStartDateBtn.text = "Start Date"
            }
            else if (currentSortMethodIndex == 2) { // by finish date
                sortFinishDateBtn.setBackgroundResource(0x00000000)
                sortFinishDateBtn.text = "Finish Date"
            }
            else if (currentSortMethodIndex == 3) { // by name
                sortNameBtn.setBackgroundResource(0x00000000)
                sortNameBtn.text = "Name"
            }
            else {  // by generation
                sortGenerationBtn.setBackgroundResource(0x00000000)
                sortGenerationBtn.text = "Generation"
            }

            Log.d("MainActivity", "unselectCurrentSortMethod() completed")
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

        // on click listener for the sort by default button
        sortDefaultBtn.setOnClickListener {
            Log.d("MainActivity", "Sort by default button clicked")

            // invert the sort order if current sort method is default (0)
            if (currentSortMethodIndex == 0) {
                currentSortOrders[0] = if (currentSortOrders[0] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method index
                currentSortMethodIndex = 0
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[0] == SortOrder.DESC) "DF ↓" else "DF ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = db.getHunts(sortMethod = SortMethod.DEFAULT, sortOrder = currentSortOrders[0])

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

            // invert the sort order if current sort method is by start date (1)
            if (currentSortMethodIndex == 1) {
                currentSortOrders[1] = if (currentSortOrders[1] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method index
                currentSortMethodIndex = 1
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[1] == SortOrder.DESC) "SD ↓" else "SD ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = db.getHunts(sortMethod = SortMethod.DATE_STARTED, sortOrder = currentSortOrders[1])

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

            // invert the sort order if current sort method is by finish date (2)
            if (currentSortMethodIndex == 2) {
                currentSortOrders[2] = if (currentSortOrders[2] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method index
                currentSortMethodIndex = 2
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[2] == SortOrder.DESC) "FD ↓" else "FD ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = db.getHunts(sortMethod = SortMethod.DATE_FINISHED, sortOrder = currentSortOrders[2])

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

            // invert the sort order if current sort method is by name (3)
            if (currentSortMethodIndex == 3) {
                currentSortOrders[3] = if (currentSortOrders[3] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method index
                currentSortMethodIndex = 3
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[3] == SortOrder.DESC) "NA ↓" else "NA ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = db.getHunts(sortMethod = SortMethod.NAME, sortOrder = currentSortOrders[3])

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

            // invert the sort order if current sort method is by generation (4)
            if (currentSortMethodIndex == 4) {
                currentSortOrders[4] = if (currentSortOrders[4] == SortOrder.DESC) SortOrder.ASC else SortOrder.DESC
            } else {
                // set the current method index
                currentSortMethodIndex = 4
            }

            // set the text of the floating sort button
            sortBtn.text = if (currentSortOrders[4] == SortOrder.DESC) "GE ↓" else "GE ↑"

            // close the sort menu
            closeSortMenu()

            // get the new shiny hunt data set
            val sortedHunts = db.getHunts(sortMethod = SortMethod.GENERATION, sortOrder = currentSortOrders[4])

            // update the sort method and order in the adapter
            shinyHuntListAdapter.updateSortMethod(SortMethod.GENERATION, currentSortOrders[4])

            // update the recycler view
            shinyHuntListAdapter.submitList(sortedHunts) {
                shinyHuntRecyclerView.scrollToPosition(0)
            }

        }

        Log.d("MainActivity", "onCreate() completed")
    }

}