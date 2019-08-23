package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.utils.putInt
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioConfig() : AbstractNamedEntry() {
    var configType: ConfigType = ConfigType.INTERNAL_SOUND
    val file = SimpleObjectProperty<IEntry>()
    val player = SimpleObjectProperty<Player>()
    val unknown = SimpleIntegerProperty()
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val unknownThree = SimpleObjectProperty<ByteArray>()

    var setIndexAddress = 0
    var setIndex = 0

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg, endAddress: Long): this() {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()]
        player.value = info.players[reader.readInt24()] as Player
        reader.readByte() // Skip resource ID, we already know what it is for players
        unknown.value = reader.readInt()
        configType = ConfigType.fromValue(reader.readInt())

        // Outside of the name, the rest of the config block is more or less unknown.
        // We need bits and pieces of this section, but we don't really care about the rest.
        setIndexAddress = reader.readInt()
        unknownTwo.value = reader.read(4).array()
        name.value = strg.entries[reader.readInt()].name
        unknownThree.value = reader.read((endAddress - reader.tell()).toInt()).array()
        if (configType == ConfigType.INTERNAL_SOUND) {
            reader.seek(baseAddress + setIndexAddress)
            setIndex = reader.readInt()
        }
        validate()
    }

    private fun validate() {
        val valid = when (configType) {
            ConfigType.EXTERNAL_SOUND -> file.value is ExternalFileReference
            ConfigType.INTERNAL_SOUND, ConfigType.SEQUENCE -> file.value is InternalFileReference
        }
        if (!valid)
            throw IllegalStateException("Mismatch between config type and file type: $this")
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        if (configType == ConfigType.INTERNAL_SOUND) {
            unknownThree.value.putInt(setIndex, setIndexAddress - 0x1C, csar.byteOrder)
        }
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeInt24(csar.players.indexOf(player.value))
        writer.writeByte(4) // Player resource type
        writer.writeInt(unknown.value)
        writer.writeInt(configType.value)
        writer.writeInt(setIndexAddress)
        writer.write(unknownTwo.value)
        writer.writeInt(strgEntry!!.index)
        writer.write(unknownThree.value)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitBaseConfig(this) }
}