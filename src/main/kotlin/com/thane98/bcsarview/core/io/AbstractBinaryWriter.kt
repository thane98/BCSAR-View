package com.thane98.bcsarview.core.io

import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class AbstractBinaryWriter(private val byteOrder: ByteOrder) : IBinaryWriter {
    override fun writeBoolean(value: Boolean) {
        writeInt(if (value) 1 else 0)
    }

    override fun writeByte(value: Int) {
        val buffer = ByteBuffer.allocate(1)
        buffer.put(value.toByte())
        write(buffer)
    }

    override fun writeShort(value: Int) {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES).order(byteOrder)
        buffer.putShort(value.toShort())
        write(buffer)
    }

    override fun writeInt24(value: Int) {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
        buffer.putInt(value)
        buffer.rewind()
        val trimmedBuffer = ByteBuffer.allocate(3)
        for (i in 0 until 3)
            trimmedBuffer.put(buffer.get())
        write(trimmedBuffer)
    }

    override fun writeInt(value: Int) {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
        buffer.putInt(value)
        write(buffer)
    }

    override fun writeFloat(value: Float) {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
        buffer.putFloat(value)
        write(buffer)
    }
}