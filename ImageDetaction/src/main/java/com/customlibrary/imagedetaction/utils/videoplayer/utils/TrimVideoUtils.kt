package com.customlibrary.imagedetaction.utils.videoplayer.utils

import java.util.Formatter
import java.util.Locale

object TrimVideoUtils {
    const val N_1000 = 1000
    const val N_60 = 60
    const val N_3600 = 3600
    fun stringForTime(timeMs: Long): String {
        val totalSeconds = (timeMs / N_1000).toInt()
        val seconds = totalSeconds % N_60
        val minutes = totalSeconds / N_60 % N_60
        val hours = totalSeconds / N_3600
        val mFormatter = Formatter(Locale.ENGLISH)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}
