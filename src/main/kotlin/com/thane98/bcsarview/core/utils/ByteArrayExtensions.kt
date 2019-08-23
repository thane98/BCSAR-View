package com.thane98.bcsarview.core.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.putInt(value: Int, index: Int, byteOrder: ByteOrder) {
    val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
    buffer.putInt(value)
    buffer.rewind()
    for (i in 0 until Int.SIZE_BYTES) {
        this[index + i] = buffer.get()
    }
}