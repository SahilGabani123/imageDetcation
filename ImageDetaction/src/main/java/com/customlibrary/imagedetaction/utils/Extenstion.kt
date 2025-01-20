package com.customlibrary.imagedetaction.utils

import android.app.Activity
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun Activity.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Activity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.hideKeyboard(): Boolean {
    val view = window.currentFocus
    return hideKeyboard(window, view)
}

fun Activity.hideKeyboard(view: View): Boolean {
    return hideKeyboard(window, view)
}

private fun hideKeyboard(window: Window, view: View?): Boolean {
    if (view == null) {
        return false
    }
    val inputMethodManager =
        window.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as? InputMethodManager
    return inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0) ?: false
}