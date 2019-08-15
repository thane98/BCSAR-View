package com.thane98.bcsarview.core.io

import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class BinaryWriter(private val channel: FileChannel, byteOrder: ByteOrder) : AbstractBinaryWriter(byteOrder) {
    override fun write(buffer: ByteBuffer) {
        verifyWritePosition(buffer.capacity())
        buffer.rewind()
        channel.write(buffer)
    }

    override fun write(array: ByteArray) {
        verifyWritePosition(array.size)
        val buffer = ByteBuffer.allocate(array.size)
        buffer.put(array)
        write(buffer)
    }

    private fun verifyWritePosition(numBytesToWrite: Int) {
        if (tell() > channel.size())
            throw IndexOutOfBoundsException()
        if (tell() != channel.size().toInt() && tell() + numBytesToWrite > channel.size())
            throw IllegalArgumentException("Alignment violation")
    }

    override fun seek(position: Int) { channel.position(position.toLong()) }
    override fun tell(): Int { return channel.position().toInt() }
    override fun close() { channel.close() }
}