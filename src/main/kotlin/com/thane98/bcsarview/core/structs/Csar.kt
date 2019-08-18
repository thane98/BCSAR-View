package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.io.BinaryReader
import com.thane98.bcsarview.core.io.BinaryWriter
import com.thane98.bcsarview.core.io.determineByteOrder
import com.thane98.bcsarview.core.io.retrievers.ImportedFileRetriever
import com.thane98.bcsarview.core.io.retrievers.InMemoryFileRetriever
import com.thane98.bcsarview.core.io.retrievers.InternalFileRetriever
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.structs.entries.*
import com.thane98.bcsarview.core.structs.files.Cwar
import com.thane98.bcsarview.core.structs.files.Cwsd
import javafx.collections.ObservableList
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
            byteOrder = channel.determineByteOrder()
            val reader = BinaryReader(channel, byteOrder)
            reader.seek(0x18)
            val strgAddress = reader.readInt().toLong()
            reader.seek(0x24)
            val infoAddress = reader.readInt().toLong()
            reader.seek(0x30)
            fileAddress = reader.readInt().toLong()
            this.strg = Strg(reader, strgAddress)
            this.info = Info(reader, infoAddress, this, strg)

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

    fun reopen(): IBinaryReader {
        return BinaryReader(FileChannel.open(path), byteOrder)
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
        path = destination
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
                assert(baseAddress + entry.fileAddress + 8 == writer.tell().toLong())
                writer.write(entry.retriever.retrieve())
                entry.retriever = InternalFileRetriever(this, entry)
            }
        }
        fileAddress = baseAddress.toLong()
        val filePartitionSize = writer.tell() - baseAddress
        writer.seek(baseAddress + 4)
        writer.writeInt(filePartitionSize)
        return filePartitionSize
    }

    fun dumpFile(record: InternalFileReference, destination: Path) {
        Files.write(destination, record.retriever.retrieve())
    }

    fun dumpSound(config: AudioConfig, destination: Path) {
        if (config.configType == ConfigType.SEQUENCE)
            dumpFile(config.file.value as InternalFileReference, destination)
        else {
            val soundIndex = configs.indexOf(config)
            val soundSet = findSoundSetForSound(soundIndex)
                ?: throw IllegalArgumentException("Target sound is not in a sound set!")
            val wsdIndex = soundIndex - soundSet.soundStartIndex.value
            withCwsdCwarMapping(soundSet.file.value, soundSet.archive.value.file.value) { wsd, war ->
                Files.write(destination, war.files[wsd.entries[wsdIndex].archiveIndex])
            }
        }
    }

    private fun findSoundSetForSound(soundId: Int): SoundSet? {
        return soundSets.find { soundId >= it.soundStartIndex.value && soundId <= it.soundEndIndex.value }
    }

    fun extractSoundSet(soundSet: SoundSet, destination: Path) {
        val targetArchive = soundSet.archive.value
        val sounds = findAssociatedSounds(soundSet)
        withCwsdCwarMapping(soundSet.file.value, targetArchive.file.value) { wsd, war ->
            assert(wsd.entries.size == sounds.size)
            for (i in 0 until sounds.size) {
                val destinationPath = Paths.get(destination.toString(), "${sounds[i]}.cwav")
                Files.write(destinationPath, war.files[wsd.entries[i].archiveIndex])
            }
        }
    }

    private fun findAssociatedSounds(soundSet: SoundSet): List<AudioConfig> {
        val result = mutableListOf<AudioConfig>()
        for (i in soundSet.soundStartIndex.value until soundSet.soundEndIndex.value)
            result.add(configs[i])
        return result
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

    fun importExternalSounds(sounds: List<AudioConfig>, player: Player) {
        for (sound in sounds) {
            assert(sound.configType == ConfigType.EXTERNAL_SOUND)
            sound.player.value = player
            sound.strgEntry.value = strg.allocateEntry(sound.strgEntry.value.name, sound.strgEntry.value.type)
            files.add(sound.file.value)
            configs.add(sound)
        }
    }

    fun importArchives(source: Csar, archives: List<Archive>, player: Player) {
        for (archive in archives)
            importArchive(source, archive, player)
    }

    private fun importArchive(source: Csar, archive: Archive, player: Player) {
        importInternalFileWithRetrieverUpdate(source, archive.file.value)
        archive.strgEntry.value = strg.allocateEntry(archive.strgEntry.value.name, archive.strgEntry.value.type)
        this.archives.add(archive)

        val sets = source.findAssociatedSets(archive)
        for (set in sets)
            importSoundSet(source, set, player, this.archives.lastIndex)
    }

    private fun importInternalFileWithRetrieverUpdate(source: Csar, record: InternalFileReference) {
        record.retriever = ImportedFileRetriever(source, record)
        importInternalFile(record)
    }

    private fun importInternalFile(record: InternalFileReference) {
        var maxAddress: Long = 0x18
        for (file in files) {
            if (file is InternalFileReference) {
                val fileEnd = file.fileAddress + file.fileSize
                val endAddress = fileEnd + (0x20 - (fileEnd % 0x20))
                if (endAddress > maxAddress)
                    maxAddress = endAddress
            }
        }
        record.fileAddress = maxAddress - 8
        files.add(record)
    }

    private fun importSoundSet(source: Csar, set: SoundSet, player: Player, archiveId: Int) {
        importSetFileAsPartial(source, set, archiveId)
        importSoundsFromSoundSet(source, set, player)
        soundSets.add(set)
    }

    private fun importSoundSetWithNewArchive(source: Csar, set: SoundSet, player: Player) {
        val archive = createArchiveForSet(set)
        importSetFileAsExclusive(set, archives.size)
        importSoundsFromSoundSet(source, set, player)
        set.archive.value = archive
        archives.add(archive)
        soundSets.add(set)
    }

    private fun createArchiveForSet(set: SoundSet): Archive {
        val archive = Archive()
        archive.file.value = createArchiveFileRecordForSet(set)
        archive.unknown.value = 3 // Seems to be right for named archives
        archive.strgEntry.value = strg.allocateEntry("WARC_$set", 5)
        archive.entryCount.value = set.soundEndIndex.value - set.soundStartIndex.value + 1
        return archive
    }

    private fun createArchiveFileRecordForSet(set: SoundSet): InternalFileReference {
        return withCwsdCwarMapping(set.file.value, set.archive.value.file.value) { wsd, war ->
            // Move files from the old CWAR into the new one
            val newWar = Cwar()
            for (entry in wsd.entries)
                newWar.files.add(war.files[entry.archiveIndex])

            // Create a file record for the new CWAR and add it to the files list.
            val rawNewWar = newWar.serialize(byteOrder)
            val record = InternalFileReference()
            record.fileSize = rawNewWar.size.toLong()
            record.retriever = InMemoryFileRetriever(rawNewWar, byteOrder)
            importInternalFile(record)
            record
        }
    }

    private fun <T> withCwsdCwarMapping(
        wsdRecord: InternalFileReference,
        warRecord: InternalFileReference,
        action: (Cwsd, Cwar) -> T
    ): T {
        val wsdReader = wsdRecord.open()
        val warReader = warRecord.open()
        wsdReader.use {
            warReader.use {
                val wsd = Cwsd(wsdReader)
                val war = Cwar(warReader)
                return action(wsd, war)
            }
        }
    }

    private fun importSetFileAsExclusive(set: SoundSet, archiveId: Int) {
        val wsdReader = set.file.value.open()
        wsdReader.use {
            val wsd = Cwsd(wsdReader)
            wsd.transferTo(archiveId)
            val rawWsd = wsd.serialize(byteOrder)
            set.file.value.fileSize = rawWsd.size.toLong()
            set.file.value.retriever = InMemoryFileRetriever(rawWsd, byteOrder)
        }
        importInternalFile(set.file.value)
    }

    private fun importSetFileAsPartial(source: Csar, set: SoundSet, archiveId: Int) {
        importInternalFileWithRetrieverUpdate(source, set.file.value)
        val wsdReader = set.file.value.open()
        wsdReader.use {
            val wsd = Cwsd(wsdReader)
            wsd.moveToArchive(archiveId)
            set.file.value.retriever = InMemoryFileRetriever(wsd.serialize(byteOrder), byteOrder)
        }
    }

    private fun importSoundsFromSoundSet(source: Csar, set: SoundSet, player: Player) {
        val sounds = source.findAssociatedSounds(set)
        set.strgEntry.value = strg.allocateEntry(set.strgEntry.value.name, set.strgEntry.value.type)
        set.soundStartIndex.value = configs.size
        set.soundEndIndex.value = configs.size + sounds.size - 1
        for (sound in sounds) {
            sound.player.value = player
            sound.strgEntry.value = strg.allocateEntry(sound.strgEntry.value.name, sound.strgEntry.value.type)
            configs.add(sound)
        }
    }

    private fun findAssociatedSets(archive: Archive): List<SoundSet> {
        return soundSets.filtered { it.archive.value == archive }
    }

    fun importSoundSets(source: Csar, sets: List<SoundSet>, player: Player) {
        for (set in sets)
            importSoundSetWithNewArchive(source, set, player)
    }
}