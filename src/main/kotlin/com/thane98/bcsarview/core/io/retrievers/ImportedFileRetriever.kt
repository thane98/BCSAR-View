package com.thane98.bcsarview.core.io.retrievers

import com.thane98.bcsarview.core.interfaces.IFileRetriever
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path

class ImportedFileRetriever(private val source: Path, private val address: Long, private val size: Int) : IFileRetriever {
    override fun retrieve(): ByteArray {
        val reader = FileChannel.open(source)
        reader.use {
            reader.position(address)
            val buffer = ByteBuffer.allocate(size)
            reader.read(buffer)
            return buffer.array()
        }
    }
}