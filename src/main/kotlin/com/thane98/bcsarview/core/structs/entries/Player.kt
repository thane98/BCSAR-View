package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty

class Player(): IEntry {
    val soundLimit = SimpleIntegerProperty()
    val unknown = SimpleIntegerProperty()
    val strgEntry = SimpleObjectProperty<StrgEntry>()
    val heapSize = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, strg: Strg) : this() {
        soundLimit.value = reader.readInt()
        unknown.value = reader.readInt()
        val strgIndex = reader.readInt()
        strgEntry.value = strg.entries[strgIndex]
        heapSize.value = reader.readInt()
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(soundLimit.value)
        writer.writeInt(unknown.value)
        writer.writeInt(strgEntry.value.index)
        writer.writeInt(heapSize.value)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitPlayer(this) }

    override fun toString(): String {
        return if (strgEntry.value != null)
            strgEntry.value.name
        else
            "AnonymousPlayer"
    }
}