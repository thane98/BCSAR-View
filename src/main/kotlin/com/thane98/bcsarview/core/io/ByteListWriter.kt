package com.thane98.bcsarview.core.io

import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteListWriter(private val list: MutableList<Byte>, private val byteOrder: ByteOrder) : IBinaryWriter {
    private var position = 0

    override fun writeByte(value: Int) {
        val trimmed = value.and(0xFF).toByte()
        if (position == list.size)
            list.add(trimmed)
        else
            list[position] = trimmed
        position += 1
    }

    override fun writeShort(value: Int) {
        val buffer = ByteBuffer.allocate(Short.SIZE_BYTES).order(byteOrder)
        buffer.putShort(value.toShort())
        write(buffer.array())
    }

    override fun writeInt24(value: Int) {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
        buffer.putInt(value)
        val trimmedBuffer = ByteBuffer.allocate(3)
        buffer.rewind()
        for (i in 0 until 3)
            trimmedBuffer.put(buffer.get())
        write(trimmedBuffer.array())
    }

    override fun writeInt(value: Int) {
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES).order(byteOrder)
        buffer.putInt(value)
        write(buffer.array())
    }

    override fun writeFloat(value: Float) {
        val buffer = ByteBuffer.allocate(4).order(byteOrder)
        buffer.putFloat(value)
        write(buffer.array())
    }

    override fun write(array: ByteArray) {
        if (position > list.size)
            throw IndexOutOfBoundsException()
        if (position != list.size && position + array.size > list.size)
            throw IllegalArgumentException("Alignment violation")
        if (position == list.size)
            list.addAll(array.toList())
        else {
            for (i in 0 until array.size)
                list[position + i] = array[i]
        }
        position += array.size
    }

    override fun seek(position: Int) { this.position = position }
    override fun tell(): Int { return position }
    override fun close() {}
}