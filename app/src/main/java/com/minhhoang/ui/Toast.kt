package com.minhhoang.ui

import android.content.Context
import android.widget.Toast

fun showMessage(ctx: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    val t = Toast.makeText(ctx, message, duration)
    t.show()
}
