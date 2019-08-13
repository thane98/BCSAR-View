package com.thane98.bcsarview.core.io

fun MutableList<Byte>.addInt32(value: Int) {
    for (i in 0 until 4)
        this.add(value.shr(i * 8).and(0xFF).toByte())
}

fun MutableList<Byte>.addInt24(value: Int) {
    for (i in 0 until 3)
        this.add(value.shr(i * 8).and(0xFF).toByte())
}

fun MutableList<Byte>.addInt16(value: Int) {
    for (i in 0 until 2)
        this.add(value.shr(i * 8).and(0xFF).toByte())
}