package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class AudioConfig(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg, endAddress: Long) : IEntry {
    var configType: ConfigType = ConfigType.INTERNAL_SOUND
    val file = SimpleObjectProperty<IEntry>()
    val player = SimpleObjectProperty<Player>()
    val unknown = SimpleIntegerProperty()
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val strgEntry = SimpleObjectProperty<StrgEntry>()
    val unknownThree = SimpleObjectProperty<ByteArray>()

    init {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()]
        player.value = info.players[reader.readInt24()] as Player
        reader.seek(reader.tell() + 1)
        unknown.value = reader.readInt()
        configType = ConfigType.fromValue(reader.readInt())
        unknownTwo.value = reader.read(8).array()
        strgEntry.value = strg.entries[reader.readInt()]
        unknownThree.value = reader.read((endAddress - reader.tell()).toInt()).array()
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
        writer.writeInt(csar.files.indexOf(file.value))
        writer.writeInt24(csar.players.indexOf(player.value))
        writer.writeByte(4) // Player resource type
        writer.writeInt(unknown.value)
        writer.writeInt(configType.value)
        writer.write(unknownTwo.value)
        writer.writeInt(strgEntry.value.index)
        writer.write(unknownThree.value)
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitBaseConfig(this) }

    override fun toString(): String {
        return if (strgEntry.value != null)
            strgEntry.value.name
        else
            "AnonymousConfig"
    }
}