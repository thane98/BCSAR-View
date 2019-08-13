package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.verifyMagic

class CwsdEntry(val archiveIndex: Int, val config: ByteArray)

class Cwsd(reader: IBinaryReader, baseAddress: Long) {
    val entries: List<CwsdEntry>

    init {
        reader.seek(baseAddress)
        reader.verifyMagic("CWSD")
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt()
        reader.seek(infoAddress)
        reader.verifyMagic("INFO")
        reader.seek(infoAddress + 0xC)
        val indexTableAddress = infoAddress + reader.readInt() + 8
        reader.seek(infoAddress + 0x14)
        val configTableAddress = infoAddress + reader.readInt() + 8

        val indices = readIndexTable(reader, indexTableAddress)
        val configs = readConfigTable(reader, configTableAddress)
        assert(indices.size == configs.size)
        entries = indices.mapIndexed { index, value -> CwsdEntry(value, configs[index])}
    }

    private fun readIndexTable(reader: IBinaryReader, baseAddress: Long): List<Int> {
        val result = mutableListOf<Int>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            result.add(reader.readInt())
        }
        return result
    }

    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            result.add(reader.read(0x8C).array())
        }
        return result
    }
}