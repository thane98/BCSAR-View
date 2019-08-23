package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class CwsdEntry(var archiveIndex: Int, var archiveId: Int)

class Cwsd(reader: IBinaryReader) {
    val entries: List<CwsdEntry>
    val configs: List<ByteArray>

    init {
        val baseAddress = reader.tell()
        reader.verifyMagic("CWSD")
        reader.seek(baseAddress + 0xC)
        val fileSize = reader.readInt()
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt()
        reader.seek(infoAddress)
        reader.verifyMagic("INFO")
        reader.seek(infoAddress + 0xC)
        val entryTableAddress = infoAddress + reader.readInt() + 8
        reader.seek(infoAddress + 0x14)
        val configTableAddress = infoAddress + reader.readInt() + 8
        entries = readEntryTable(reader, entryTableAddress)
        configs = readConfigTable(reader, configTableAddress, fileSize)
    }

    fun moveToArchive(archiveId: Int) {
        for (entry in entries)
            entry.archiveId = archiveId
    }

    fun transferTo(archiveId: Int) {
        for (i in 0 until entries.size) {
            entries[i].archiveId = archiveId
            entries[i].archiveIndex = i
        }
    }

    private fun readEntryTable(reader: IBinaryReader, baseAddress: Long): List<CwsdEntry> {
        val result = mutableListOf<CwsdEntry>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            val archiveId = reader.readInt24()
            reader.readByte() // Don't care about resource type, already known
            val archiveIndex = reader.readInt()
            result.add(CwsdEntry(archiveIndex, archiveId))
        }
        return result
    }

    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long, fileSize: Int): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        val addresses = readConfigAddresses(reader, baseAddress)
        for (i in 0 until addresses.size) {
            val length = if (i == addresses.lastIndex)
                fileSize - addresses[i]
            else
                addresses[i + 1] - addresses[i]
            reader.seek(baseAddress + addresses[i])
            result.add(reader.read(length.toInt()).array())
        }
        return result
    }

    private fun readConfigAddresses(reader: IBinaryReader, baseAddress: Long): List<Long> {
        reader.seek(baseAddress)
        val result = mutableListOf<Long>()
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + i * 0x8 + 8)
            result.add(reader.readInt().toLong())
        }
        return result
    }

    fun serialize(byteOrder: ByteOrder): ByteArray {
        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, byteOrder)
        val fileSize = calculateFileSize()
        writer.write("CWSD".toByteArray(StandardCharsets.UTF_8))
        writer.writeShort(0xFEFF)
        writer.writeShort(0x20)
        writer.writeInt(0x1000100)
        writer.writeInt(fileSize)
        writer.writeInt(1)
        writer.writeInt(0x6800)
        writer.writeInt(0x20)
        writer.writeInt(fileSize - 0x20) // Size of info partition
        serializeInfo(writer, fileSize - 0x20)
        return result.toByteArray()
    }

    private fun serializeInfo(writer: IBinaryWriter, size: Int) {
        writer.write("INFO".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(size)
        writer.writeInt(0x100)
        writer.writeInt(0x10)
        writer.writeInt(0x101)
        writer.writeInt(entries.size * 0x8 + 0x14)
        writer.writeInt(entries.size)
        for (entry in entries) {
            writer.writeInt24(entry.archiveId)
            writer.writeByte(0x5)
            writer.writeInt(entry.archiveIndex)
        }

        val rawConfigs = mutableListOf<Byte>()
        writer.writeInt(configs.size)
        for (i in 0 until configs.size) {
            writer.writeInt(0x4900)
            writer.writeInt(rawConfigs.size + configs.size * 8 + 4)
            for (byte in configs[i])
                rawConfigs.add(byte)
        }
        writer.write(rawConfigs.toByteArray())
    }

    private fun calculateFileSize(): Int {
        return entries.size * 8 + configs.size * 0x94 + 0x40
    }
}