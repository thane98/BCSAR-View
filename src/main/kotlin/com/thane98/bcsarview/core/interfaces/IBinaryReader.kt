package com.thane98.bcsarview.core.interfaces

import java.io.Closeable
import java.nio.ByteBuffer

interface IBinaryReader : Closeable {
    fun readByte(): Int
    fun readShort(): Int
    fun readInt24(): Int
    fun readInt(): Int
    fun readFloat(): Float
    fun read(numBytes: Int): ByteBuffer
    fun seek(position: Long)
    fun tell(): Long
}