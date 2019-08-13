package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class Bank(): IEntry {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleObjectProperty<ByteArray>()
    val name = SimpleStringProperty()
    val unknownTwo = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknown.value = reader.read(0xC).array()
        name.value = strg.entries[reader.readInt()].name
        unknownTwo.value = reader.readInt()
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitBank(this) }
    override fun toString(): String { return name.value ?: "AnonymousBank" }
}