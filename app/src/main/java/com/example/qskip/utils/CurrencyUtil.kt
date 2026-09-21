package com.example.qskip.utils

import java.util.Locale

fun Double.toCurrency(): String {
    return String.format(Locale.US, "%.2f", this)
}

fun Number.toCurrency(): String {
    return String.format(Locale.US, "%.2f", this.toDouble())
}
