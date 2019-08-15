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

class Archive(): IEntry {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleIntegerProperty()
    val strgEntry = SimpleObjectProperty<StrgEntry>()
    val entryCount = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        val isNamed = reader.readInt() != 0
        unknown.value = reader.readInt()
        if (isNamed) {
            strgEntry.value = strg.entries[reader.readInt()]
            entryCount.value = reader.readInt()
        }
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeBoolean(strgEntry.value != null)
        writer.writeInt(unknown.value)
        if (strgEntry.value != null) {
            writer.writeInt(strgEntry.value.index)
            writer.writeInt(entryCount.value)
        }
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitArchive(this) }

    override fun toString(): String {
        return if (strgEntry.value != null)
            strgEntry.value.name
        else
            "AnonymousArchive"
    }
}