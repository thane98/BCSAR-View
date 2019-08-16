package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import java.nio.charset.StandardCharsets
import java.util.*

data class StrgEntry(var name: String, var resourceId: Int, var type: Int, var index: Int) {
    override fun toString(): String {
        return name
    }
}

class Strg(reader: IBinaryReader, baseAddress: Long) {
    val entries: MutableList<StrgEntry>

    init {
        reader.seek(baseAddress)
        reader.verifyMagic("STRG")
        reader.seek(baseAddress + 0xC)
        val nameTableAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x14)
        val lookupTableAddress = baseAddress + reader.readInt() + 8
        val names = readNamesTable(reader, nameTableAddress)
        entries = readLookupTable(reader, lookupTableAddress, names)
    }

    fun allocateEntry(name: String, resourceType: Int): StrgEntry {
        val index = entries.indexOfLast { it.type == resourceType }
        val prevEntry = entries[index]
        val entry = StrgEntry(name, prevEntry.resourceId + 1, resourceType, index + 1)
        for (i in index + 1 until entries.size)
            entries[i].index += 1
        entries.add(index + 1, entry)
        return entry
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

    private fun readLookupTable(reader: IBinaryReader, baseAddress: Long, names: List<String>): MutableList<StrgEntry> {
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
        return result.sortedWith(compareBy { it.index }).toMutableList()
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
        // It looks like archives are always inserted last?
        // Sort entries such that everything else is inserted before archives.
        val trie = StrgTrie()
        val sortedEntries = entries.sortedWith(Comparator { a, b ->
            val aValue = if (a.type == 5) Integer.MAX_VALUE else a.type
            val bValue = if (b.type == 5) Integer.MAX_VALUE else b.type
            when {
                aValue < bValue -> -1
                aValue == bValue -> 0
                else -> 1
            }
        })
        for (entry in sortedEntries)
            trie.insert(entry)
        return trie.serialize(csar)
    }
}