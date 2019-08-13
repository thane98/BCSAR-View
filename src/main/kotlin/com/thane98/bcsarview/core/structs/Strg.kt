package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.verifyMagic
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.nio.charset.StandardCharsets
import java.util.*

data class StrgEntry(val name: String, val resourceId: Int, val type: Int) {
    override fun toString(): String {
        return name
    }
}

class Strg {
    val entries: ObservableList<StrgEntry>

    constructor(entries: ObservableList<StrgEntry>) {
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

    private fun readLookupTable(reader: IBinaryReader, baseAddress: Long, names: List<String>): ObservableList<StrgEntry> {
        val tempList = mutableListOf<Pair<Int, StrgEntry>>()
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
                tempList.add(Pair(strgIndex, StrgEntry(name, resourceId, resourceType)))
            }
        }

        // Create observable list of entries sorted by index in names table
        val result = FXCollections.observableArrayList<StrgEntry>()
        tempList.sortedWith(compareBy { it.first }).mapTo(result, { it.second })
        return result
    }
}