package com.example.pokemonshinytracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import java.util.Calendar

class DialogHandler {

    // Function to create dialogs that utilize layouts for their views
    fun createDialogWithLayout(context: Context, title: String, layout: View, onClose: () -> Unit = {}): AlertDialog {
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss(); onClose() }
            .create().apply {
                setOnCancelListener { onClose() }
                setOnDismissListener { onClose() }
                window?.setBackgroundDrawableResource(R.drawable.ui_gradient_homepage)
                show()
            }
    }

    // Function to create date picker dialogs
    fun createDatePickerDialog(context: Context, onDateSelected: (String) -> Unit, onClose: () -> Unit = {}) {
        val c = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateSelected("$year-${month + 1}-$day")
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

}