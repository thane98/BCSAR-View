package com.thane98.bcsarview.core.io.retrievers

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IFileRetriever
import com.thane98.bcsarview.core.io.ByteArrayBinaryReader
import java.nio.ByteOrder

class InMemoryFileRetriever(
    private val rawFile: ByteArray,
    private val byteOrder: ByteOrder,
    private val sourceFile: String = "In-Memory File"
) : IFileRetriever {
    override fun fileSize(): Int { return rawFile.size }

    override fun retrieve(): ByteArray {
        return rawFile
    }

    override fun open(): IBinaryReader {
        return ByteArrayBinaryReader(rawFile, byteOrder)
    }

    override fun toString(): String { return sourceFile }
}