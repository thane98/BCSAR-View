package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class SequenceSet(): BaseSet() {
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val unknownThree: ObservableList<Int> = FXCollections.observableArrayList<Int>()

    constructor(reader: IBinaryReader, baseAddress: Long, strg: Strg): this() {
        readBaseSetProperties(reader, baseAddress)
        reader.seek(baseAddress + 0x14) // Skip base properties and type identifier
        unknownTwo.value = reader.read(0x8).array()
        strgEntry.value = strg.entries[reader.readInt()]
        val numUnknownTwoEntries = reader.readInt()
        for (i in 0 until numUnknownTwoEntries)
            unknownThree.add(reader.readInt())
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        super.serializeTo(csar, writer)
        writer.writeInt(0) // Type identifier
        writer.write(unknownTwo.value)
        writer.writeInt(strgEntry.value.index)
        writer.writeInt(unknownThree.size)
        for (entry in unknownThree)
            writer.writeInt(entry)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSequenceSet(this) }
}