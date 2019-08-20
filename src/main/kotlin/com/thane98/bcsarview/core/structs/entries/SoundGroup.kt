package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty

class SoundGroup(): AbstractNamedEntry() {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknown.value = reader.readInt()
        name.value = strg.entries[reader.readInt()].name
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeInt(unknown.value)
        writer.writeInt(strgEntry!!.index)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSoundGroup(this) }
}