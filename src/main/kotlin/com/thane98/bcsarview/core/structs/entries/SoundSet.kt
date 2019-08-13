package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class SoundSet(): IEntry {
    val unknown = SimpleObjectProperty<ByteArray>()
    val name = SimpleStringProperty()
    val unknownTwo = SimpleIntegerProperty()
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknownThree = SimpleObjectProperty<ByteArray>()
    val archiveIndex = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress + 4) // Skip type identifier
        unknown.value = reader.read(8).array()
        name.value = strg.entries[reader.readInt()].name
        unknownTwo.value = reader.readInt()
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknownThree.value = reader.read(0x10).array()
        archiveIndex.value = reader.readInt24()
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSoundSet(this) }
    override fun toString(): String { return name.value ?: "AnonymousSet" }
}