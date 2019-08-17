package com.thane98.bcsarview.core.io

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteArrayBinaryReader(private val bytes: ByteArray, byteOrder: ByteOrder) :
    AbstractBinaryReader(byteOrder) {
    private var position: Long = 0

    override fun read(numBytes: Int): ByteBuffer {
        val intPos = position.toInt()
        val buffer = ByteBuffer.allocate(numBytes)
        for (i in intPos until intPos + numBytes)
            buffer.put(bytes[i])
        buffer.rewind()
        position += numBytes
        return buffer
    }

    override fun seek(position: Long) { this.position = position }
    override fun tell(): Long { return position }
    override fun close() {}
}