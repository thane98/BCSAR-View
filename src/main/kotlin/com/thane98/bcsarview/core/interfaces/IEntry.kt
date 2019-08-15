package com.thane98.bcsarview.core.interfaces

import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.structs.Csar

interface IEntry {
    fun <T> accept(visitor: IEntryVisitor<T>): T
    fun serializeTo(csar: Csar, writer: IBinaryWriter)

    fun serialize(csar: Csar): ByteArray {
        val result = mutableListOf<Byte>()
        serializeTo(csar, ByteListWriter(result, csar.byteOrder))
        return result.toByteArray()
    }
}