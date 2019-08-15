package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*

data class StrgEntry(val name: String, val resourceId: Int, val type: Int, var index: Int) {
    override fun toString(): String {
        return name
    }
}

class Strg {
    val entries: List<StrgEntry>

    constructor(entries: List<StrgEntry>) {
        this.entries = entries
    }

    constructor(reader: IBinaryReader, baseAddress: Long) {
        reader.seek(baseAddress)
        reader.verifyMagic("STRG")

        reader.seek(baseAddress + 0xC)
        val nameTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x14)
        val lookupTableAddress = baseAddress + reader.readInt() + 8

        val names = readNamesTable(reader, nameTableAddress)
        entries = readLookupTable(reader, lookupTableAddress, names)
    }

    private fun readNamesTable(reader: IBinaryReader, baseAddress: Long): List<String> {
        val result = mutableListOf<String>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            // Skip to ith entry, skip the type Id
            reader.seek(baseAddress + i * 0xC + 8)
            val nameAddress = reader.readInt()
            val nameLength = reader.readInt() - 1

            reader.seek(baseAddress + nameAddress)
            val rawString = reader.read(nameLength)
            result.add(String(rawString.array(), StandardCharsets.UTF_8))
        }
        return result
    }

    private fun readLookupTable(reader: IBinaryReader, baseAddress: Long, names: List<String>): List<StrgEntry> {
        val result = mutableListOf<StrgEntry>()
        reader.seek(baseAddress + 4) // Skip root index for now; not needed
        val numNodes = reader.readInt()
        for (i in 0 until numNodes) {
            val leafStart = baseAddress + i * 0x14 + 8
            reader.seek(leafStart)
            if (reader.readShort() != 0) {
                reader.seek(leafStart + 0xC)
                val strgIndex = reader.readInt()
                val name = names[strgIndex]
                val resourceId = reader.readInt24()
                val resourceType = reader.readByte()
                result.add(StrgEntry(name, resourceId, resourceType, strgIndex))
            }
        }
        return result.sortedWith(compareBy { it.index })
    }

    fun serialize(csar: Csar): ByteArray {
        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, csar.byteOrder)
        val entryTable = serializeEntries(csar)
        val lookupTable = serializeLookupTable(csar)
        writer.write("STRG".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0) // Size, need to come back to this later
        writer.writeInt(0x2400)
        writer.writeInt(0x10) // Entry table is always in the same place
        writer.writeInt(0x2401)
        writer.writeInt(entryTable.size + 0x10)
        writer.write(entryTable)
        writer.write(lookupTable)
        while (result.size % 0x20 != 0) result.add(0)
        writer.seek(4)
        writer.writeInt(result.size)
        return result.toByteArray()
    }

    private fun serializeEntries(csar: Csar): ByteArray {
        val result = mutableListOf<Byte>()
        val rawStrings = mutableListOf<Byte>()
        val writer = ByteListWriter(result, csar.byteOrder)
        writer.writeInt(entries.size)
        for (entry in entries) {
            writer.writeInt(0x1F01)
            writer.writeInt(entries.size * 0xC + rawStrings.size + 4)
            writer.writeInt(entry.name.length + 1) // Null terminator
            rawStrings.addAll(entry.name.toByteArray(StandardCharsets.UTF_8).toList())
            rawStrings.add(0)
        }
        result.addAll(rawStrings)
        while (result.size % 4 != 0) result.add(0) // Padding
        return result.toByteArray()
    }

    private fun serializeLookupTable(csar: Csar): ByteArray {
        val trie = StrgTrie()
        for (entry in entries)
            trie.insert(entry)
        return trie.serialize(csar)
    }
}