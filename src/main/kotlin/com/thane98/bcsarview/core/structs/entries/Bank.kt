package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class Bank(): IEntry {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleObjectProperty<ByteArray>()
    val strgEntry = SimpleObjectProperty<StrgEntry>()
    val archive = SimpleObjectProperty<Archive>()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknown.value = reader.read(0xC).array()
        strgEntry.value = strg.entries[reader.readInt()]
        if (reader.readInt() != 0)
            archive.value = info.archives[reader.readInt24()]
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(csar.files.indexOf(file.value))
        writer.write(unknown.value)
        writer.writeInt(strgEntry.value.index)
        writer.writeBoolean(archive.value != null)
        if (archive.value != null) {
            writer.writeInt24(csar.archives.indexOf(archive.value))
            writer.writeByte(0x5) // Archive resource type
        }
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitBank(this) }

    override fun toString(): String {
        return if (strgEntry.value != null)
            strgEntry.value.name
        else
            "AnonymousBank"
    }
}