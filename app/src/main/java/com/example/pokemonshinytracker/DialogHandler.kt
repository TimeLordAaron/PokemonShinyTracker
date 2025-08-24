package com.example.pokemonshinytracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import java.util.Calendar

class DialogHandler {

    // Function to create a date picker dialog
    fun createDatePickerDialog(context: Context, selectedDate: String? = null, onDateSelected: (String) -> Unit = {}, onClose: () -> Unit = {}): DatePickerDialog {
        val c = Calendar.getInstance()

        // set the current selection (if a date was provided)
        if (!selectedDate.isNullOrBlank()) {
            try {
                val parts = selectedDate.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1    // calendar months are 0-based
                    val day = parts[2].toInt()
                    c.set(year, month, day)
                }
            } catch (e: Exception) {
                // if parsing fails, just use today's date
                e.printStackTrace()
            }
        }

        return DatePickerDialog(
            context,
            { _, year, month, day ->
                // format the date by adding leading 0s for month and day if needed
                val formattedDate = String.format("%d-%02d-%02d", year, month + 1, day)
                onDateSelected(formattedDate)
                onClose()
            },
            // pass the year, month, and day for the selected date
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)

        ).apply {
            setOnCancelListener { onClose() }
            setOnDismissListener { onClose() }
            show()
        }
    }

    // Function to create a confirmation dialog
    fun createConfirmationDialog(context: Context, title: String, message: String, onYes: () -> Unit = {}, onClose: () -> Unit = {}): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onYes() }
            .setNegativeButton("No") { _, _ -> onClose() }
            .create().apply {
                setOnCancelListener { onClose() }
                setOnDismissListener { onClose() }
                show()
            }

    }

    // Function to create an error dialog
    fun createErrorDialog(context: Context, title: String, message: String, onClose: () -> Unit = {}): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { dialog, _ -> dialog.dismiss(); onClose() }
            .create().apply {
                setOnCancelListener { onClose() }
                setOnDismissListener { onClose() }
                show()
            }
    }

    // Function to create a dialog that utilizes a layout for its view
    fun createLayoutDialog(context: Context, title: String, layout: View, onClose: () -> Unit = {}): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss(); onClose() }
            .create().apply {
                setOnCancelListener { onClose() }
                setOnDismissListener { onClose() }
                window?.setBackgroundDrawableResource(R.drawable.ui_gradient_homepage)  // change the background of the dialog
                show()
            }
    }

}