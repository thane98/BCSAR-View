package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.IllegalArgumentException

abstract class BaseSet: IEntry {
    val name = SimpleStringProperty()
    val soundStartIndex = SimpleIntegerProperty()
    val soundEndIndex = SimpleIntegerProperty()
    val soundType = SimpleIntegerProperty()
    val unknown = SimpleObjectProperty<ByteArray>()

    protected fun readBaseSetProperties(reader: IBinaryReader, baseAddress: Long) {
        reader.seek(baseAddress)
        soundStartIndex.value = reader.readInt24()
        soundType.value = reader.readByte()
        soundEndIndex.value = reader.readInt24()
        if (soundType.value != reader.readByte())
            throw IllegalArgumentException("Type mismatch in sound set!")
        unknown.value = reader.read(8).array()
    }
}