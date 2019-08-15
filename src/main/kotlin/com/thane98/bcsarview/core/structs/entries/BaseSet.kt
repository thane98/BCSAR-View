package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.IllegalArgumentException

abstract class BaseSet: IEntry {
    val strgEntry = SimpleObjectProperty<StrgEntry>()
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

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt24(soundStartIndex.value)
        writer.writeByte(soundType.value)
        writer.writeInt24(soundEndIndex.value)
        writer.writeByte(soundType.value)
        writer.write(unknown.value)
    }

    override fun toString(): String {
        return if (strgEntry.value != null)
            strgEntry.value.name
        else
            "AnonymousSet"
    }
}