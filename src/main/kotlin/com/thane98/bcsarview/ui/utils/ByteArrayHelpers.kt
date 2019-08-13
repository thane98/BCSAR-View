package com.thane98.bcsarview.ui.utils

import javafx.util.StringConverter

fun parseByteArrayFromText(text: String): ByteArray {
    return text.split(' ').map { Integer.parseInt(it, 16).toByte() }.toByteArray()
}

fun byteArrayToText(bytes: ByteArray): String {
    return bytes.joinToString(" ") {
        it.toInt().and(0xFF).toString(16).padStart(2, '0')
    }
}

class ByteArrayStringConverter : StringConverter<ByteArray>() {
    override fun toString(input: ByteArray?): String {
        if (input == null) return ""
        return byteArrayToText(input)
    }

    override fun fromString(input: String?): ByteArray {
        if (input == null) return ByteArray(0)
        return parseByteArrayFromText(input)
    }
}