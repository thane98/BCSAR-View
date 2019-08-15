package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.*
import com.thane98.bcsarview.core.structs.Csar
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import java.nio.file.Path

class InternalFileReference(reader: IBinaryReader, baseAddress: Long) : IEntry {
    var fileAddress: Long = 0
    var fileSize: Long = 0
    val unknown = SimpleObjectProperty<ByteArray>()
    val retriever: IFileRetriever? = null

    init {
        reader.seek(baseAddress + 4)
        val fileInfoAddress = baseAddress + reader.readInt() + 4
        unknown.value = reader.read(8).array()
        reader.seek(fileInfoAddress)
        fileAddress = reader.readInt().toLong()
        fileSize = reader.readInt().toLong()
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(0x220C) // Resource ID
        writer.writeInt(0xC) // Data start address. Always 0xC
        writer.write(unknown.value)
        writer.writeInt(fileAddress.toInt())
        writer.writeInt(fileSize.toInt())
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitInternalFileReference(this) }
}