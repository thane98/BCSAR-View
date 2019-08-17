package com.thane98.bcsarview.core.io

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class BinaryReader(private val channel: FileChannel, byteOrder: ByteOrder) : AbstractBinaryReader(byteOrder) {
    override fun read(numBytes: Int): ByteBuffer {
        val buffer = ByteBuffer.allocate(numBytes)
        channel.read(buffer)
        return buffer
    }

    override fun seek(position: Long) { channel.position(position) }
    override fun tell(): Long { return channel.position() }
    override fun close() { channel.close() }
}