package com.thane98.bcsarview.core.io

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class BinaryReader(private val channel: FileChannel, private val endian: ByteOrder) : IBinaryReader {
    override fun close() {
        channel.close()
    }

    override fun readByte(): Int {
        return read(1)[0].toInt()
    }

    override fun readShort(): Int {
        val buffer = read(Short.SIZE_BYTES).order(endian)
        return buffer.getShort(0).toInt()
    }

    override fun readInt24(): Int {
        val buffer = read(3)
        val resultBuffer = ByteBuffer.allocate(4).order(endian)
        buffer.rewind()
        resultBuffer.put(buffer)
        return resultBuffer.getInt(0)
    }

    override fun readInt(): Int {
        val buffer = read(Int.SIZE_BYTES).order(endian)
        return buffer.getInt(0)
    }

    override fun readFloat(): Float {
        val buffer = read(Int.SIZE_BYTES).order(endian)
        return buffer.getFloat(0)
    }

    override fun read(numBytes: Int): ByteBuffer {
        val buffer = ByteBuffer.allocate(numBytes)
        channel.read(buffer)
        return buffer
    }

    override fun length(): Long {
        return channel.size()
    }

    override fun seek(position: Long) {
        channel.position(position)
    }

    override fun tell(): Long {
        return channel.position()
    }
}