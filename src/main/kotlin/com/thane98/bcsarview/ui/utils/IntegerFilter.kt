package com.thane98.bcsarview.ui.utils

import javafx.scene.control.TextFormatter
import javafx.util.converter.IntegerStringConverter
import java.util.function.UnaryOperator
import java.util.regex.Pattern

fun createIntegerTextFormatter(): TextFormatter<Int> {
    return TextFormatter(IntegerStringConverter(), 0, IntegerFilter())
}

class IntegerFilter : UnaryOperator<TextFormatter.Change?> {
    companion object {
        private val NUMBER_PATTERN = Pattern.compile("\\d*")
    }

    override fun apply(at: TextFormatter.Change?): TextFormatter.Change? {
        return if (NUMBER_PATTERN.matcher(at?.text).matches()) at else null
    }
}