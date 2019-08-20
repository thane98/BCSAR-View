package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections

abstract class BaseSet: AbstractNamedEntry() {
    val sounds = FXCollections.observableArrayList<AudioConfig>()
    val unknown = SimpleObjectProperty<ByteArray>()

    protected fun readBaseSetProperties(reader: IBinaryReader, baseAddress: Long, info: Info) {
        reader.seek(baseAddress)
        val firstSoundId = reader.readInt()
        val firstSoundIndex = firstSoundId.and(0xFFFFFF)
        val lastSoundId = reader.readInt()
        val lastSoundIndex = lastSoundId.and(0xFFFFFF)
        unknown.value = reader.read(8).array()
        if (firstSoundId != -1 && lastSoundId != -1) {
            for (i in firstSoundIndex..lastSoundIndex)
                sounds.add(info.configs[i])
        }
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        // TODO: Empty sets?
        if (sounds.isNotEmpty()) {
            writer.writeInt24(sounds.first().strgEntry!!.resourceId)
            writer.writeByte(1)
            writer.writeInt24(sounds.last().strgEntry!!.resourceId)
            writer.writeByte(1)
        } else {
            writer.writeInt(-1)
            writer.writeInt(-1)
        }
        writer.write(unknown.value)
    }
}