package com.thane98.bcsarview.core.interfaces

import java.io.Closeable
import java.nio.ByteBuffer

interface IBinaryWriter : Closeable {
    fun writeBoolean(value: Boolean) { writeInt(if (value) 1 else 0) }
    fun writeByte(value: Int)
    fun writeShort(value: Int)
    fun writeInt24(value: Int)
    fun writeInt(value: Int)
    fun writeFloat(value: Float)
    fun write(buffer: ByteBuffer)
    fun write(array: ByteArray)
    fun seek(position: Int)
    fun tell(): Int
}