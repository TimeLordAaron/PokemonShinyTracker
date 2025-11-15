package com.example.pokemonshinytracker

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class DataTransferActivity : ComponentActivity() {

    // lateinit UI declarations
    private lateinit var drawerLayout: DrawerLayout             // drawer layout
    private lateinit var navView: NavigationView                // navigation view
    private lateinit var dataTransferMainView: ConstraintLayout // data transfer main view
    private lateinit var exportBtn: MaterialButton              // export button
    private lateinit var importBtn: MaterialButton              // import button

    // database helper
    private val db = DBHelper(this, null)

    // dialog handler
    private val dh = DialogHandler()

    // variable to track if a dialog is opened
    private var dialogOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.data_transfer)

        // hide the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // temporarily show the bars when swiping
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // access the UI elements
        drawerLayout = findViewById(R.id.drawer_layout)                     // drawer layout
        navView = findViewById(R.id.nav_view)                               // navigation view
        dataTransferMainView = findViewById(R.id.data_transfer_main_view)   // data transfer main view
        exportBtn = findViewById(R.id.export_hunts_button)                  // export button
        importBtn = findViewById(R.id.import_hunts_button)                  // import button

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item1 -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // close DataTransferActivity activity
                }
                R.id.item2 -> dh.createErrorDialog(this, "Shiny Living Dex", "This feature is still in development. Thank you for your patience!")
                R.id.item3 -> {}    // do nothing, as this is the Data Transfer screen
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // prepare the layout based on device orientation
        prepareLayout()

        // on click listener for the export hunts button
        exportBtn.setOnClickListener {
            // TODO: Implement export logic
            if (!dialogOpened) {
                dialogOpened = true
                dh.createErrorDialog(
                    this,
                    "Export Hunts",
                    "Sorry, this feature has not been implemented yet! Thank you for your patience",
                    { dialogOpened = false }
                )
            }
        }

        // on click listener for the import hunts button
        importBtn.setOnClickListener {
            // TODO: Implement import logic
            if (!dialogOpened) {
                dialogOpened = true
                dh.createErrorDialog(
                    this,
                    "Import Hunts",
                    "Sorry, this feature has not been implemented yet! Thank you for your patience!",
                    { dialogOpened = false }
                )
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // update the layout orientation based on the new configuration
        prepareLayout()
    }

    // Helper function to prepare the layout based on device orientation
    private fun prepareLayout() {
        // hunt details
        val dataTransferConstraintLayout = findViewById<ConstraintLayout>(R.id.data_transfer_main_view)
        val dataTransferConstraintSet = ConstraintSet()
        dataTransferConstraintSet.clone(dataTransferConstraintLayout)

        // handle portrait orientation
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // constrain the export hunts section
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.BOTTOM, R.id.import_hunts_layout, ConstraintSet.TOP)

            // constrain the import hunts section
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.TOP, R.id.export_hunts_layout, ConstraintSet.BOTTOM)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            // set the height and width of the sections


        // handle landscape orientation
        } else {
            // constrain the export hunts section
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.END, R.id.import_hunts_layout, ConstraintSet.START)
            dataTransferConstraintSet.connect(R.id.export_hunts_layout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            // constrain the import hunts section
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.START, R.id.export_hunts_layout, ConstraintSet.END)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            dataTransferConstraintSet.connect(R.id.import_hunts_layout, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            // set the height and width of the sections


        }

        dataTransferConstraintSet.applyTo(dataTransferConstraintLayout)
    }
}