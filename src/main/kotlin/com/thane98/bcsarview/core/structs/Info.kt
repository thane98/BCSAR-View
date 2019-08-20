package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.structs.entries.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.nio.charset.StandardCharsets

private data class Sets(val soundSets: ObservableList<SoundSet>, val sequenceSets: ObservableList<SequenceSet>)

class Info(reader: IBinaryReader, baseAddress: Long, csar: Csar, strg: Strg) {
    val configs: ObservableList<AudioConfig>
    val soundSets: ObservableList<SoundSet>
    val sequenceSets: ObservableList<SequenceSet>
    val banks: ObservableList<Bank>
    val archives: ObservableList<Archive>
    val groups: ObservableList<SoundGroup>
    val players: ObservableList<Player>
    val files: ObservableList<IEntry>
    val footer: ByteArray

    init {
        reader.seek(baseAddress)
        reader.verifyMagic("INFO")
        reader.seek(baseAddress + 0xC)
        val configTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x14)
        val setTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x1C)
        val bankTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x24)
        val archiveTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x2C)
        val groupTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x34)
        val playerTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x3C)
        val fileTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x44)
        val footerAddress = baseAddress + reader.readInt() + 8

        files = readFileTable(reader, fileTableAddress, csar)
        archives = readArchiveTable(reader, archiveTableAddress, strg)
        banks = readBankTable(reader, bankTableAddress, strg)
        groups = readGroupTable(reader, groupTableAddress, strg)
        players = readPlayerTable(reader, playerTableAddress, strg)
        configs = readConfigTable(reader, configTableAddress, strg, setTableAddress)
        val sets = readSetTable(reader, setTableAddress, strg)
        soundSets = sets.soundSets
        sequenceSets = sets.sequenceSets
        reader.seek(footerAddress)
        footer = reader.read(0x1C).array()
    }

    private fun generateConfigTable(): List<AudioConfig> {
        val newConfigs = mutableListOf<AudioConfig>()
        newConfigs.addAll(configs.filtered { it.configType == ConfigType.EXTERNAL_SOUND })
        newConfigs.addAll(soundSets.flatMap { it.sounds })
        newConfigs.addAll(configs.filtered { it.configType == ConfigType.SEQUENCE})
        return newConfigs
    }

    // To figure out the fileSize of a config, we look at where the next config (or the next table) starts.
    // This avoids needing to figure out the layout of every possible config right now.
    // We can worry about that later.
    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long, strg: Strg, endAddress: Long): ObservableList<AudioConfig> {
        val result = FXCollections.observableArrayList<AudioConfig>()
        val addresses = readConfigAddresses(reader, baseAddress)
        for (i in 0 until addresses.size) {
            val lastAddress = if (i == addresses.lastIndex) endAddress else addresses[i + 1]
            result.add(AudioConfig(reader, addresses[i], this, strg, lastAddress))
        }
        return result
    }

    private fun readConfigAddresses(reader: IBinaryReader, baseAddress: Long): List<Long> {
        val result = mutableListOf<Long>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            result.add(baseAddress + reader.readInt().toLong())
        }
        return result
    }

    private fun readSetTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): Sets {
        val soundSets = FXCollections.observableArrayList<SoundSet>()
        val sequenceSets = FXCollections.observableArrayList<SequenceSet>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            reader.seek(entryAddress + 0x10)
            when (reader.readInt()) {
                0x2205 -> soundSets.add(SoundSet(reader, entryAddress, this, strg))
                0 -> sequenceSets.add(SequenceSet(reader, entryAddress, this, strg))
                else -> throw IllegalArgumentException("Unrecognized set type!")
            }
        }
        return Sets(soundSets, sequenceSets)
    }

    private fun readBankTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<Bank> {
        val result = FXCollections.observableArrayList<Bank>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(Bank(reader, entryAddress, this, strg))
        }
        return result
    }

    private fun readArchiveTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<Archive> {
        val result = FXCollections.observableArrayList<Archive>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(Archive(reader, entryAddress, this, strg))
        }
        return result
    }

    private fun readGroupTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<SoundGroup> {
        val result = FXCollections.observableArrayList<SoundGroup>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(SoundGroup(reader, entryAddress, this, strg))
        }
        return result
    }

    private fun readPlayerTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<Player> {
        val result = FXCollections.observableArrayList<Player>()
        reader.seek(baseAddress)
        val numPlayers = reader.readInt()
        for (i in 0 until numPlayers) {
            reader.seek(baseAddress + i * 0x8 + 8)
            reader.seek(baseAddress + reader.readInt())
            result.add(Player(reader, strg))
        }
        return result
    }

    private fun readFileTable(reader: IBinaryReader, baseAddress: Long, csar: Csar): ObservableList<IEntry> {
        val result = FXCollections.observableArrayList<IEntry>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(readFileEntry(reader, entryAddress, csar))
        }
        return result
    }

    private fun readFileEntry(reader: IBinaryReader, entryAddress: Long, csar: Csar): IEntry {
        reader.seek(entryAddress)
        return when (reader.readInt()) {
            0x220C -> InternalFileReference(reader, entryAddress, csar)
            0x220D -> ExternalFileReference(reader, entryAddress)
            else -> throw IllegalArgumentException("Unknown entry type in file table!")
        }
    }

    fun serialize(csar: Csar): ByteArray {
        val configs = generateConfigTable()
        val sets = mutableListOf<IEntry>()
        sets.addAll(soundSets)
        sets.addAll(sequenceSets)
        updateFileTableAddresses()

        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, csar.byteOrder)
        writer.write("INFO".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0) // Partition fileSize. Need to revisit later.
        reserveSpaceForHeaderEntry(writer, 0x2100)
        reserveSpaceForHeaderEntry(writer, 0x2104)
        reserveSpaceForHeaderEntry(writer, 0x2101)
        reserveSpaceForHeaderEntry(writer, 0x2103)
        reserveSpaceForHeaderEntry(writer, 0x2105)
        reserveSpaceForHeaderEntry(writer, 0x2102)
        reserveSpaceForHeaderEntry(writer, 0x2106)
        reserveSpaceForHeaderEntry(writer, 0x220B)
        val configAddress = result.size
        writeEntryList(writer, configAddress, csar, configs, 0x2200)
        val setAddress = result.size
        writeEntryList(writer, setAddress, csar, sets, 0x2204)
        val bankAddress = result.size
        writeEntryList(writer, bankAddress, csar, banks, 0x2206)
        val archiveAddress = result.size
        writeEntryList(writer, archiveAddress, csar, archives, 0x2207)
        val groupAddress = result.size
        writeEntryList(writer, groupAddress, csar, groups, 0x2208)
        val playerAddress = result.size
        writeEntryList(writer, playerAddress, csar, players, 0x2209)
        val fileAddress = result.size
        writeEntryList(writer, fileAddress, csar, files, 0x220A)
        val footerAddress = result.size
        writer.write(footer)
        while (result.size % 0x20 != 0)
            writer.writeByte(0)
        writer.seek(0xC)
        writer.writeInt(configAddress - 8)
        writer.seek(0x14)
        writer.writeInt(setAddress - 8)
        writer.seek(0x1C)
        writer.writeInt(bankAddress - 8)
        writer.seek(0x24)
        writer.writeInt(archiveAddress - 8)
        writer.seek(0x2C)
        writer.writeInt(groupAddress - 8)
        writer.seek(0x34)
        writer.writeInt(playerAddress - 8)
        writer.seek(0x3C)
        writer.writeInt(fileAddress - 8)
        writer.seek(0x44)
        writer.writeInt(footerAddress - 8)
        writer.seek(4)
        writer.writeInt(result.size)
        return result.toByteArray()
    }

    private fun updateFileTableAddresses() {
        var nextAddress = 0x20
        for (file in files) {
            if (file is InternalFileReference) {
                file.fileAddress = nextAddress.toLong() - 8
                nextAddress += file.fileSize()
                nextAddress += (0x20 - (nextAddress % 0x20)) % 0x20
            }
        }
    }

    private fun reserveSpaceForHeaderEntry(writer: IBinaryWriter, resourceType: Int) {
        writer.writeInt(resourceType)
        writer.writeInt(0)
    }

    private fun writeEntryList(writer: IBinaryWriter, baseAddress: Int, csar: Csar, targets: List<IEntry>, resourceType: Int) {
        // Reserve space for the table before writing the raw data
        writer.seek(baseAddress)
        writer.writeInt(targets.size)
        for (target in targets) {
            writer.writeInt(resourceType)
            writer.writeInt(0)
        }

        // Write raw data and update pointer in table
        var nextEntryAddress = targets.size * 8 + 4
        for (i in 0 until targets.size) {
            writer.seek(baseAddress + i * 0x8 + 8)
            writer.writeInt(nextEntryAddress)
            writer.seek(baseAddress + nextEntryAddress)
            targets[i].serializeTo(csar, writer)
            nextEntryAddress = writer.tell() - baseAddress
        }
    }
}