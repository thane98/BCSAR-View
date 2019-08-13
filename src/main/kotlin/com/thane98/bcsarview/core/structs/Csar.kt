package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.io.BinaryReader
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.structs.entries.*
import com.thane98.bcsarview.core.structs.files.Cwar
import com.thane98.bcsarview.core.structs.files.Cwsd
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Csar(private var path: Path) {
    private var fileAddress: Long
    private val byteOrder: ByteOrder
    val configs: ObservableList<AudioConfig>
    val sets: ObservableList<IEntry>
    val banks: ObservableList<Bank>
    val archives: ObservableList<Archive>
    val groups: ObservableList<SoundGroup>
    val players: ObservableList<Player>
    val footer = SimpleObjectProperty<ByteArray>()

    init {
        val channel = FileChannel.open(path, StandardOpenOption.READ)
        try {
            channel.verifyMagic("CSAR")
            byteOrder = determineByteOrder(channel)
            val reader = BinaryReader(channel, byteOrder)
            reader.seek(0x18)
            val strgAddress = reader.readInt().toLong()
            reader.seek(0x24)
            val infoAddress = reader.readInt().toLong()
            reader.seek(0x30)
            fileAddress = reader.readInt().toLong()
            val strg = Strg(reader, strgAddress)
            val info = Info(reader, infoAddress, strg)

            configs = info.configs
            sets = info.sets
            banks = info.banks
            archives = info.archives
            groups = info.groups
            players = info.players
            footer.value = info.footer
        } finally {
            channel.close()
        }
    }

    fun dumpFile(record: InternalFileReference, destination: Path) {
        val reader = reopen()
        reader.use {
            reader.seek(fileAddress + record.fileAddress + 8)
            Files.write(destination, reader.read(record.fileSize.toInt()).array())
        }
    }

    fun extractSoundSet(baseSet: BaseSet, destination: Path) {
        val soundSet = baseSet.subEntry.value as SoundSet
        val targetArchive = archives[soundSet.archiveIndex.value]
        val sounds = findAssociatedSounds(baseSet)

        val reader = reopen()
        reader.use {
            val war = openArchive(reader, targetArchive)
            val wsd = openSoundSet(reader, soundSet)
            assert(wsd.entries.size == sounds.size)
            for (i in 0 until sounds.size) {
                val destinationPath = Paths.get(destination.toString(), "${sounds[i]}.cwav")
                Files.write(destinationPath, war.extractFile(reader, wsd.entries[i].archiveIndex))
            }
        }
    }

    private fun findAssociatedSounds(baseSet: BaseSet): List<AudioConfig> {
        val result = mutableListOf<AudioConfig>()
        for (i in baseSet.soundStartIndex.value until baseSet.soundEndIndex.value)
            result.add(configs[i])
        return result
    }

    private fun openArchive(reader: IBinaryReader, archive: Archive): Cwar {
        return Cwar(reader, fileAddress + archive.file.value.fileAddress + 8)
    }

    private fun openSoundSet(reader: IBinaryReader, soundSet: SoundSet): Cwsd {
        return Cwsd(reader, fileAddress + soundSet.file.value.fileAddress + 8)
    }

    private fun determineByteOrder(channel: FileChannel): ByteOrder {
        val buffer = ByteBuffer.allocate(2)
        channel.read(buffer)
        return if (buffer[0].toInt() == 0xFE)
            ByteOrder.BIG_ENDIAN
        else
            ByteOrder.LITTLE_ENDIAN
    }

    private fun reopen(): IBinaryReader {
        return BinaryReader(FileChannel.open(path), byteOrder)
    }
}