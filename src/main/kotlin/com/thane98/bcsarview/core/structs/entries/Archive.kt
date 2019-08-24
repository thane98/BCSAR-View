package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty

// TODO: Generate entry count before serializing
class Archive(): AbstractNamedEntry() {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleIntegerProperty()
    val entryCount = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        val isNamed = reader.readInt() != 0
        unknown.value = reader.readInt()
        if (isNamed) {
            name.value = strg.entries[reader.readInt()].name
            entryCount.value = reader.readInt()
        }
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeBoolean(strgEntry != null)
        writer.writeInt(unknown.value)
        if (strgEntry != null) {
            writer.writeInt(strgEntry!!.index)
            writer.writeInt(entryCount.value)
        }
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitArchive(this) }
}