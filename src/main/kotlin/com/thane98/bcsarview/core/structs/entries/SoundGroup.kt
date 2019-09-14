package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.files.Cgrp
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import java.lang.IllegalArgumentException
import java.util.*

class SoundGroup(): AbstractNamedEntry() {
    val file = SimpleObjectProperty<InternalFileReference>()
    val items = FXCollections.observableArrayList<IEntry>()
    val unknown = SimpleIntegerProperty()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknown.value = reader.readInt()
        name.value = strg.entries[reader.readInt()].name
        buildItemsList(info)
    }

    private fun buildItemsList(info: Info) {
        val cgrp = Cgrp(file.value.open())
        for (entry in cgrp.infxEntries) {
            when (entry.itemType) {
                0x1 -> items.add(info.configs[entry.itemId])
                0x2 -> items.add(getSetFromId(entry.itemId, info))
                0x3 -> items.add(info.banks[entry.itemId])
                else -> throw IllegalArgumentException("Unexpected item type in sound group.")
            }
        }
    }

    private fun getSetFromId(setId: Int, info: Info): IEntry {
        return if (setId < info.soundSets.size)
            info.soundSets[setId]
        else
            info.sequenceSets[setId - info.soundSets.size]
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeInt(unknown.value)
        writer.writeInt(strgEntry!!.index)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSoundGroup(this) }
}