package com.thane98.bcsarview.core.io.retrievers

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IFileRetriever
import com.thane98.bcsarview.core.io.BinaryReader
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.InternalFileReference
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Path

class BasicFileRetriever(
    private val source: Path,
    private val address: Long,
    private val size: Int,
    private val byteOrder: ByteOrder
) : IFileRetriever {
    override fun fileSize(): Int { return size }

    override fun retrieve(): ByteArray {
        val reader = FileChannel.open(source)
        reader.use {
            reader.position(address)
            val buffer = ByteBuffer.allocate(size)
            reader.read(buffer)
            return buffer.array()
        }
    }

    override fun open(): IBinaryReader {
        val reader = BinaryReader(FileChannel.open(source), byteOrder)
        reader.seek(address)
        return reader
    }
}