package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty

class SoundSet(): BaseSet() {
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val unknownThree = SimpleIntegerProperty()
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknownFour = SimpleObjectProperty<ByteArray>()
    val archive = SimpleObjectProperty<Archive>()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        readBaseSetProperties(reader, baseAddress)
        reader.seek(baseAddress + 0x14) // Skip base properties and type identifier
        unknownTwo.value = reader.read(8).array()
        name.value = strg.entries[reader.readInt()].name
        unknownThree.value = reader.readInt()
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknownFour.value = reader.read(0xC).array()
        if (reader.readInt() != 0)
            archive.value = info.archives[reader.readInt24()]
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSoundSet(this) }
    override fun toString(): String { return name.value ?: "AnonymousSet" }
}