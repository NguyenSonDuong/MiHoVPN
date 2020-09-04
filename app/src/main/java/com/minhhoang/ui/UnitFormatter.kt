package com.minhhoang.ui

import kotlin.math.floor
import kotlin.math.roundToInt

class FormattedBytesViewItem(val value: String, val units: String)

object UnitFormatter {
    val KB = 1024
    val MB = 1024 * KB
    val GB = 1024 * MB

    fun bytesDisplay(bytes: Long): FormattedBytesViewItem {
        val bytesDouble = bytes.toDouble()
        return when {
            bytesDouble < KB -> FormattedBytesViewItem("$bytesDouble", "B")
            bytesDouble < MB -> FormattedBytesViewItem("%.2f".format(bytesDouble / KB), "KB")
            bytesDouble < GB -> FormattedBytesViewItem("%.2f".format(bytesDouble / MB), "MB")
            else -> FormattedBytesViewItem("%.2f".format(bytesDouble / GB), "GB")
        }
    }

    fun timeDisplay(seconds: Long): String {
        val secondsDouble = seconds.toDouble()
        if (seconds < 0) {
            return "00:00:00"
        }

        val h = floor(secondsDouble / 3600).roundToInt()
        val hh = when {
            h > 9 -> h.toString()
            else -> "0$h"
        }

        val m = floor((secondsDouble % 3600) / 60).roundToInt()
        val mm = when {
            m > 9 -> m.toString()
            else -> "0$m"
        }

        val s = floor(secondsDouble % 60).roundToInt()
        val ss = when {
            s > 9 -> s.toString()
            else -> "0$s"
        }

        return "${hh}:${mm}:${ss}"
    }
}
