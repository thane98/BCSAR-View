package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class Archive(): IEntry {
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknown = SimpleIntegerProperty()
    val name = SimpleStringProperty()
    val entryCount = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        val isNamed = reader.readInt() != 0
        unknown.value = reader.readInt()
        val strgIndex = reader.readInt()
        if (isNamed) name.value = strg.entries[strgIndex].name
        entryCount.value = reader.readInt()
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitArchive(this) }
    override fun toString(): String { return name.value ?: "AnonymousArchive" }
}