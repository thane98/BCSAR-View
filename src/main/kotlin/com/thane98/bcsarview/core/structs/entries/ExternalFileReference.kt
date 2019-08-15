package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import javafx.beans.property.SimpleStringProperty
import java.nio.charset.StandardCharsets

class ExternalFileReference() : IEntry {
    var path = ""

    constructor(reader: IBinaryReader, baseAddress: Long): this() {
        reader.seek(baseAddress + 4)
        reader.seek(baseAddress + reader.readInt())

        val start = reader.tell()
        while (reader.readByte() != 0) {}
        val length = (reader.tell() - start - 1).toInt()
        reader.seek(start)
        path = String(reader.read(length).array(), StandardCharsets.UTF_8)
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(0x220D) // Resource ID
        writer.writeInt(0xC) // Data start address. Always 0xC
        writer.writeInt(0) // Always 0
        writer.write(path.toByteArray(StandardCharsets.UTF_8))
        writer.writeByte(0)
        while (writer.tell() % 4 != 0) writer.writeByte(0) // Padding
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitExternalFileReference(this) }
}