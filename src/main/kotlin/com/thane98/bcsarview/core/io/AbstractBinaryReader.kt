package com.thane98.bcsarview.core.io

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class AbstractBinaryReader(private val byteOrder: ByteOrder) : IBinaryReader {
    override fun readByte(): Int {
        return read(1)[0].toInt()
    }

    override fun readShort(): Int {
        val buffer = read(Short.SIZE_BYTES).order(byteOrder)
        return buffer.getShort(0).toInt()
    }

    override fun readInt24(): Int {
        val buffer = read(3)
        val resultBuffer = ByteBuffer.allocate(4).order(byteOrder)
        buffer.rewind()
        resultBuffer.put(buffer)
        return resultBuffer.getInt(0)
    }

    override fun readInt(): Int {
        val buffer = read(Int.SIZE_BYTES).order(byteOrder)
        return buffer.getInt(0)
    }

    override fun readFloat(): Float {
        val buffer = read(Int.SIZE_BYTES).order(byteOrder)
        return buffer.getFloat(0)
    }
}