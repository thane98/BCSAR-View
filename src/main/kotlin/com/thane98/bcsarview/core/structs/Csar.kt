package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.io.BinaryReader
import com.thane98.bcsarview.core.io.BinaryWriter
import com.thane98.bcsarview.core.io.retrievers.InternalFileRetriever
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.structs.entries.*
import com.thane98.bcsarview.core.structs.files.Cwar
import com.thane98.bcsarview.core.structs.files.Cwsd
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Csar(var path: Path) {
    private val strg: Strg
    private val info: Info
    var fileAddress: Long
    val byteOrder: ByteOrder
    val configs: ObservableList<AudioConfig>
    val soundSets: ObservableList<SoundSet>
    val sequenceSets: ObservableList<SequenceSet>
    val banks: ObservableList<Bank>
    val archives: ObservableList<Archive>
    val groups: ObservableList<SoundGroup>
    val players: ObservableList<Player>
    val files: ObservableList<IEntry>

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
            this.strg = Strg(reader, strgAddress)
            this.info = Info(reader, infoAddress, strg)

            configs = info.configs
            soundSets = info.soundSets
            sequenceSets = info.sequenceSets
            banks = info.banks
            archives = info.archives
            groups = info.groups
            players = info.players
            files = info.files
        } finally {
            channel.close()
        }
    }

    fun save(destination: Path) {
        val channel = FileChannel.open(
            destination,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        val reader = reopen()
        val writer = BinaryWriter(channel, byteOrder)
        reader.use {
            writer.use {
                val strg = strg.serialize(this)
                val info = info.serialize(this)
                writeHeader(reader, writer)
                writer.write(strg)
                writer.write(info)
                val filePartitionSize = writeFilePartition(writer)
                fixHeader(writer, strg.size, info.size, filePartitionSize)
            }
        }
    }

    private fun writeHeader(reader: IBinaryReader, writer: IBinaryWriter) {
        writer.write("CSAR".toByteArray(StandardCharsets.UTF_8))
        writer.writeShort(0xFEFF)
        writer.writeShort(0x40)
        reader.seek(writer.tell().toLong())
        writer.write(reader.read(4)) // Version
        writer.writeInt(0) // BCSAR size. Need to revisit.
        writer.writeInt(3) // Number of partitions
        writer.writeInt(0x2000)
        writer.writeInt(0) // STRG address. Always 0x40.
        writer.writeInt(0) // STRG length. Need to revisit
        writer.writeInt(0x2001)
        writer.writeInt(0) // INFO address. Need to revisit.
        writer.writeInt(0) // INFO length. Need to revisit.
        writer.writeInt(0x2002)
        writer.writeInt(0) // FILE address. Need to revisit.
        writer.writeInt(0) // FILE length. Need to revisit.
        reader.seek(writer.tell().toLong())
        writer.write(reader.read(0x8))
    }

    private fun fixHeader(writer: IBinaryWriter, strgSize: Int, infoSize: Int, fileSize: Int) {
        writer.seek(0xC)
        writer.writeInt(strgSize + infoSize + fileSize + 0x40)
        writer.seek(0x18)
        writer.writeInt(0x40)
        writer.writeInt(strgSize)
        writer.seek(0x24)
        writer.writeInt(strgSize + 0x40)
        writer.writeInt(infoSize)
        writer.seek(0x30)
        writer.writeInt(strgSize + infoSize + 0x40)
        writer.writeInt(fileSize)
    }

    private fun writeFilePartition(writer: IBinaryWriter): Int {
        val baseAddress = writer.tell()
        writer.write("FILE".toByteArray(StandardCharsets.UTF_8))
        while (writer.tell() != baseAddress + 0x20) writer.writeInt(0) // Fill out the rest of the header...
        for (entry in files) {
            if (entry is InternalFileReference) {
                while (writer.tell() % 0x20 != 0) writer.writeByte(0)
                val retriever = entry.retriever ?: InternalFileRetriever(this, entry)
                writer.write(retriever.retrieve())
            }
        }
        val filePartitionSize = writer.tell() - baseAddress
        writer.seek(baseAddress + 4)
        writer.writeInt(filePartitionSize)
        return filePartitionSize
    }

    fun dumpFile(record: InternalFileReference, destination: Path) {
        val reader = reopen()
        reader.use {
            reader.seek(fileAddress + record.fileAddress + 8)
            Files.write(destination, reader.read(record.fileSize.toInt()).array())
        }
    }

    fun dumpSound(config: AudioConfig, destination: Path) {
        if (config.configType == ConfigType.SEQUENCE)
            dumpFile(config.file.value as InternalFileReference, destination)
        else {
            val soundIndex = configs.indexOf(config)
            val soundSet = findTargetSoundSet(soundIndex)
                ?: throw IllegalArgumentException("Target sound is not in a sound set!")
            val reader = reopen()
            reader.use {
                val wsdIndex = soundIndex - soundSet.soundStartIndex.value
                val war = openArchive(reader, soundSet.archive.value)
                val wsd = openSoundSet(reader, soundSet)
                Files.write(destination, war.extractFile(reader, wsd.entries[wsdIndex].archiveIndex))
            }
        }
    }

    private fun findTargetSoundSet(soundId: Int): SoundSet? {
        return soundSets.find { soundId >= it.soundStartIndex.value && soundId <= it.soundEndIndex.value }
    }

    fun extractSoundSet(soundSet: SoundSet, destination: Path) {
        val targetArchive = soundSet.archive.value
        val sounds = findAssociatedSounds(soundSet)
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

    fun addExternalSound(name: String, path: String, player: Player, sourceConfig: AudioConfig) {
        // TODO: Verify that name isn't in use
        val fileEntry = ExternalFileReference()
        fileEntry.path = path
        files.add(fileEntry)

        val newConfig = AudioConfig()
        newConfig.configType = ConfigType.EXTERNAL_SOUND
        newConfig.file.value = fileEntry
        newConfig.player.value = player
        newConfig.unknown.value = sourceConfig.unknown.value
        newConfig.unknownTwo.value = sourceConfig.unknownTwo.value.copyOf()
        newConfig.strgEntry.value = strg.allocateEntry(name, 1)
        newConfig.unknownThree.value = sourceConfig.unknownThree.value.copyOf()
        configs.add(newConfig)
    }
}