package com.thane98.bcsarview.core.io

import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteListWriter(private val list: MutableList<Byte>, byteOrder: ByteOrder) : AbstractBinaryWriter(byteOrder) {
    private var position = 0

    override fun write(buffer: ByteBuffer) {
        verifyWritePosition(buffer.capacity())
        buffer.rewind()
        if (position == list.size) {
            for (i in 0 until buffer.capacity())
                list.add(buffer.get())
        } else {
            for (i in 0 until buffer.capacity())
                list[position + i] = buffer.get()
        }
        position += buffer.capacity()
    }

    override fun write(array: ByteArray) {
        verifyWritePosition(array.size)
        if (position == list.size)
            list.addAll(array.toList())
        else {
            for (i in 0 until array.size)
                list[position + i] = array[i]
        }
        position += array.size
    }

    private fun verifyWritePosition(numBytesToWrite: Int) {
        if (position > list.size)
            throw IndexOutOfBoundsException()
        if (position != list.size && position + numBytesToWrite > list.size)
            throw IllegalArgumentException("Alignment violation")
    }

    override fun seek(position: Int) { this.position = position }
    override fun tell(): Int { return position }
    override fun close() {}
}