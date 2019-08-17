package com.thane98.bcsarview.core.io.retrievers

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IFileRetriever
import com.thane98.bcsarview.core.io.ByteArrayBinaryReader
import java.nio.ByteOrder

class InMemoryFileRetriever(private val rawFile: ByteArray, private val byteOrder: ByteOrder) : IFileRetriever {
    override fun retrieve(): ByteArray {
        return rawFile
    }

    override fun open(): IBinaryReader {
        return ByteArrayBinaryReader(rawFile, byteOrder)
    }
}