package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class SequenceSet(): IEntry {
    val unknown = SimpleObjectProperty<ByteArray>()
    val name = SimpleStringProperty()
    val unknownTwo: ObservableList<Int> = FXCollections.observableArrayList<Int>()

    constructor(reader: IBinaryReader, baseAddress: Long, strg: Strg): this() {
        reader.seek(baseAddress + 4)
        unknown.value = reader.read(0x8).array()
        name.value = strg.entries[reader.readInt()].name
        val numUnknownTwoEntries = reader.readInt()
        for (i in 0 until numUnknownTwoEntries)
            unknownTwo.add(reader.readInt())
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSequenceSet(this) }
    override fun toString(): String { return name.value ?: "AnonymousSet" }
}