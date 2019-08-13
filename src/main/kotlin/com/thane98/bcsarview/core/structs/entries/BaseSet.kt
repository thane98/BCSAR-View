package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import java.lang.IllegalArgumentException

class BaseSet(): IEntry {
    val soundStartIndex = SimpleIntegerProperty()
    val soundEndIndex = SimpleIntegerProperty()
    val soundType = SimpleIntegerProperty()
    val unknown = SimpleObjectProperty<ByteArray>()
    val subEntry = SimpleObjectProperty<IEntry>()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        soundStartIndex.value = reader.readInt24()
        soundType.value = reader.readByte()
        soundEndIndex.value = reader.readInt24()
        if (soundType.value != reader.readByte())
            throw IllegalArgumentException("Type mismatch in sound set!")
        unknown.value = reader.read(8).array()

        when (reader.readInt()) {
            0x2205 -> subEntry.value = SoundSet(reader, baseAddress + 0x10, info, strg)
            0 -> subEntry.value = SequenceSet(reader, baseAddress + 0x10, strg)
        }
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T {
        return visitor.visitBaseSet(this)
    }

    override fun toString(): String {
        return if (subEntry.value != null)
            subEntry.value.toString()
        else
            "Anonymous Set"
    }
}