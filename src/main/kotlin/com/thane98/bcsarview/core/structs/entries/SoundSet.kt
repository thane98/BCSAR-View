package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import com.thane98.bcsarview.core.interfaces.IFileRetriever
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.Info
import com.thane98.bcsarview.core.structs.Strg
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty

class SoundSet(): BaseSet() {
    val unknownTwo = SimpleObjectProperty<ByteArray>()
    val unknownThree = SimpleIntegerProperty()
    val file = SimpleObjectProperty<InternalFileReference>()
    val unknownFour = SimpleObjectProperty<ByteArray>()
    val archive = SimpleObjectProperty<Archive>()

    constructor(reader: IBinaryReader, baseAddress: Long, info: Info, strg: Strg): this() {
        readBaseSetProperties(reader, baseAddress)
        reader.seek(baseAddress + 0x14) // Skip base properties and type identifier
        unknownTwo.value = reader.read(8).array()
        strgEntry.value = strg.entries[reader.readInt()]
        unknownThree.value = reader.readInt()
        file.value = info.files[reader.readInt()] as InternalFileReference
        unknownFour.value = reader.read(0xC).array()
        if (reader.readInt() != 0)
            archive.value = info.archives[reader.readInt24()]
    }

    override fun serializeTo(csar: Csar, writer: IBinaryWriter) {
        super.serializeTo(csar, writer)
        writer.writeInt(0x2205)
        writer.write(unknownTwo.value)
        writer.writeInt(strgEntry.value.index)
        writer.writeInt(unknownThree.value)
        writer.writeInt(csar.files.indexOf(file.value))
        writer.write(unknownFour.value)
        writer.writeBoolean(archive.value != null)
        if (archive.value != null) {
            writer.writeInt24(csar.archives.indexOf(archive.value))
            writer.writeByte(5) // Archive resource ID
        }
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitSoundSet(this) }
}