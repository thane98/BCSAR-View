package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.structs.entries.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class Info(reader: IBinaryReader, baseAddress: Long, strg: Strg) {
    val configs: ObservableList<AudioConfig>
    val sets: ObservableList<IEntry>
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

        files = readFileTable(reader, fileTableAddress)
        sets = readSetTable(reader, setTableAddress, strg)
        banks = readBankTable(reader, bankTableAddress, strg)
        archives = readArchiveTable(reader, archiveTableAddress, strg)
        groups = readGroupTable(reader, groupTableAddress, strg)
        players = readPlayerTable(reader, playerTableAddress, strg)
        configs = readConfigTable(reader, configTableAddress, strg)
        reader.seek(footerAddress)
        footer = reader.read(0x1C).array()
        println("Success!")
    }

    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<AudioConfig> {
        val result = FXCollections.observableArrayList<AudioConfig>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(AudioConfig(reader, entryAddress, this, strg))
        }
        return result
    }

    private fun readSetTable(reader: IBinaryReader, baseAddress: Long, strg: Strg): ObservableList<IEntry> {
        val result = FXCollections.observableArrayList<IEntry>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(BaseSet(reader, entryAddress, this, strg))
        }
        return result
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

    private fun readFileTable(reader: IBinaryReader, baseAddress: Long): ObservableList<IEntry> {
        val result = FXCollections.observableArrayList<IEntry>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            val entryAddress = baseAddress + reader.readInt()
            result.add(readFileEntry(reader, entryAddress))
        }
        return result
    }

    private fun readFileEntry(reader: IBinaryReader, entryAddress: Long): IEntry {
        reader.seek(entryAddress)
        return when (reader.readInt()) {
            0x220C -> InternalFileReference(reader, entryAddress)
            0x220D -> ExternalFileReference(reader, entryAddress)
            else -> throw IllegalArgumentException("Unknown entry type in file table!")
        }
    }
}