package com.thane98.bcsarview.core.io.retrievers

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IFileRetriever
import com.thane98.bcsarview.core.io.BinaryReader
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.InternalFileReference
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class InternalFileRetriever(private val csar: Csar, private val fileReference: InternalFileReference) : IFileRetriever {
    override fun retrieve(): ByteArray {
        val address = csar.fileAddress + fileReference.fileAddress + 8
        val reader = FileChannel.open(csar.path)
        reader.use {
            reader.position(address)
            val buffer = ByteBuffer.allocate(fileReference.fileSize.toInt())
            reader.read(buffer)
            return buffer.array()
        }
    }

    override fun open(): IBinaryReader {
        val reader = csar.reopen()
        reader.seek(csar.fileAddress + fileReference.fileAddress + 8)
        return reader
    }
}