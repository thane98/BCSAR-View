package com.thane98.bcsarview.core.io

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

fun FileChannel.verifyMagic(expected: String){
    val buffer = ByteBuffer.allocate(4)
    this.read(buffer)
    val actual = String(buffer.array(), StandardCharsets.UTF_8)
    if (actual != expected) {
        throw IllegalArgumentException(
            "Invalid magic number. Found $actual expected $expected"
        )
    }
}

fun IBinaryReader.verifyMagic(expected: String) {
    val actual = String(this.read(4).array(), StandardCharsets.UTF_8)
    if (actual!= expected) {
        throw IllegalArgumentException(
            "Invalid magic number. Found $actual expected $expected"
        )
    }
}