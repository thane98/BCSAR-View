package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

class Player(): IEntry {
    val soundLimit = SimpleIntegerProperty()
    val unknown = SimpleIntegerProperty()
    val name = SimpleStringProperty()
    val heapSize = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, strg: Strg) : this() {
        soundLimit.value = reader.readInt()
        unknown.value = reader.readInt()
        val strgIndex = reader.readInt()
        name.value = strg.entries[strgIndex].name
        heapSize.value = reader.readInt()
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitPlayer(this) }
    override fun toString(): String { return name.value ?: "AnonymousPlayer" }
}