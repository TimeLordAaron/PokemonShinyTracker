package com.example.pokemonshinytracker

import android.os.Bundle
import android.widget.Spinner
import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
import android.view.View
import android.widget.*
import java.util.*

class IndividualHunt : ComponentActivity() {

    // Declare variables for the back and save buttons
    lateinit var backBtn: Button
    lateinit var saveBtn: Button

    // Declare variables for the start date button
    lateinit var pickStartDateBtn: Button
    lateinit var selectedStartDate: TextView

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

        // Get list of Pokemon
        val pokemon = resources.getStringArray(R.array.Pokemon)

        // Get list of Games
        val games = resources.getStringArray(R.array.Game)

        // Access all the UI elements
        val mainLayout = findViewById<LinearLayout>(R.id.background)                // main layout (i.e. the background gradient)
        backBtn = findViewById(R.id.backButton)                                     // back button
        saveBtn = findViewById(R.id.saveButton)                                     // save button
        val pokemonImage = findViewById<ImageView>(R.id.pokemonImage)               // pokemon image
        val pokemonSpinner = findViewById<Spinner>(R.id.pokemonSpinner)             // pokemon spinner (i.e. selector)
        pickStartDateBtn = findViewById(R.id.startDatePicker)                       // start date button
        selectedStartDate = findViewById(R.id.startDate)                            // start date text view
        val originGameSpinner = findViewById<Spinner>(R.id.originGameSpinner)       // origin game spinner (i.e. selector)
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

        // Retrieve data from the main window
        intent?.let {
            val selectedHuntID = it.getIntExtra("hunt_id", 0)
            val selectedHuntName = it.getStringExtra("pokemon_name") ?: "Unknown"
            val selectedHuntCounter = it.getIntExtra("pokemon_counter", 0)
            val selectedHuntStatus = it.getBooleanExtra("hunt_complete", false)

            // Set UI elements with received data
            // Proof of concept (hardcoded the spinner selection)
            if (selectedHuntName == "Darkrai") {
                pokemonSpinner.setSelection(1)
            }
            else if (selectedHuntName == "Cresselia") {
                pokemonSpinner.setSelection(2)
            }
            else if (selectedHuntName == "Celebi") {
                pokemonSpinner.setSelection(3)
            }
            else if (selectedHuntName == "Dialga") {
                pokemonSpinner.setSelection(4)
            }
            else if (selectedHuntName == "Palkia") {
                pokemonSpinner.setSelection(5)
            }
            else if (selectedHuntName == "Giratina") {
                pokemonSpinner.setSelection(6)
            }

            // Update the counter text to the counter value of the received hunt
            counter.setText(selectedHuntCounter.toString())

            // Change the background gradient if the received hunt was completed
            completionCheckbox.isChecked = selectedHuntStatus
            if (selectedHuntStatus) {
                mainLayout.setBackgroundResource(R.drawable.complete_hunt_gradient)
                finishDateLayout.visibility = View.VISIBLE
                currentGameLayout.visibility = View.VISIBLE
            }
        }

        // Back button handling
        backBtn.setOnClickListener {
            finish() // Goes back to the previous screen
        }

        // Save button handling
        saveBtn.setOnClickListener {
            // access the database
            val db = DBHelper(this,null)

            // insert the hunt into the database

        }

        // Populate the Pokemon spinner and handle selection
        if (pokemonSpinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, pokemon)
            pokemonSpinner.adapter = adapter

            pokemonSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    // Update the image (proof of concept)
                    if (pokemon[position] == "Select Pokemon:") {
                        pokemonImage.setImageResource(R.drawable.default_sprite)
                    }
                    else if (pokemon[position] == "Darkrai") {
                        pokemonImage.setImageResource(R.drawable.shiny_darkrai)
                    }
                    else if (pokemon[position] == "Cresselia") {
                        pokemonImage.setImageResource(R.drawable.shiny_cresselia)
                    }
                    else if (pokemon[position] == "Celebi") {
                        pokemonImage.setImageResource(R.drawable.shiny_celebi)
                    }
                    else if (pokemon[position] == "Dialga") {
                        pokemonImage.setImageResource(R.drawable.shiny_dialga)
                    }
                    else if (pokemon[position] == "Palkia") {
                        pokemonImage.setImageResource(R.drawable.shiny_palkia)
                    }
                    else if (pokemon[position] == "Giratina") {
                        pokemonImage.setImageResource(R.drawable.shiny_giratina)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        // Handle selection of the start date
        pickStartDateBtn.setOnClickListener {
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
                    selectedStartDate.text =
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

        // Populate the Origin Game spinner and handle selection
        if (originGameSpinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, games)
            originGameSpinner.adapter = adapter

            originGameSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
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
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, games)
            currentGameSpinner.adapter = adapter

            currentGameSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
    }
}