package com.minhhoang.ui

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(fragmentView: View) {
    val imm = fragmentView.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(fragmentView.windowToken, 0)
}
