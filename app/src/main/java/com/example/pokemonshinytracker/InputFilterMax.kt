package com.example.pokemonshinytracker

import android.text.InputFilter
import android.text.Spanned

class InputFilterMax(private val max: Int) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            // build the new string with the incoming change
            val newVal = (dest?.substring(0, dstart) ?: "") +
                    source?.subSequence(start, end) +
                    (dest?.substring(dend, dest.length) ?: "")

            if (newVal.isEmpty()) return null   // allow empty for now

            val input = newVal.toInt()
            if (input <= max) {
                return null // keep change
            }
        } catch (e: NumberFormatException) {
            // if parsing fails, block the input
        }
        return ""   // block change if > max
    }
}
