package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class AudioConfig(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg) : IEntry {
    var configType: ConfigType = ConfigType.INTERNAL_SOUND
    val file = SimpleObjectProperty<IEntry>()
    val player = SimpleObjectProperty<Player>()
    val unknown = SimpleIntegerProperty()
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val name = SimpleStringProperty()
    val unknownThree = SimpleObjectProperty<ByteArray>()

    init {
        reader.seek(baseAddress)
        file.value = info.files[reader.readInt()]
        player.value = info.players[reader.readInt24()] as Player
        reader.seek(reader.tell() + 1)
        unknown.value = reader.readInt()
        configType = ConfigType.fromValue(reader.readInt())
        reader.seek(reader.tell() + 4)
        unknownTwo.value = reader.read(4).array()
        name.value = strg.entries[reader.readInt()].name
        when (configType) {
            ConfigType.EXTERNAL_SOUND -> unknownThree.value = reader.read(0x74).array()
            ConfigType.INTERNAL_SOUND -> unknownThree.value = reader.read(0x34).array()
            ConfigType.SEQUENCE -> unknownThree.value = reader.read(0x44).array()
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

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitBaseConfig(this) }
    override fun toString(): String { return name.value ?: "AnonymousConfig" }
}