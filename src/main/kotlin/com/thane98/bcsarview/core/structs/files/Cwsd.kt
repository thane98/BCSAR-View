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
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt()
        reader.seek(infoAddress)
        reader.verifyMagic("INFO")
        reader.seek(infoAddress + 0xC)
        val entryTableAddress = infoAddress + reader.readInt() + 8
        reader.seek(infoAddress + 0x14)
        val configTableAddress = infoAddress + reader.readInt() + 8
        entries = readEntryTable(reader, entryTableAddress)
        configs = readConfigTable(reader, configTableAddress)
    }

    fun moveToArchive(archiveId: Int) {
        for (entry in entries)
            entry.archiveId = archiveId
    }

    fun transferTo(source: Cwar, destination: Cwar, archiveId: Int) {
        for (i in 0 until entries.size) {
            destination.files.add(source.files[entries[i].archiveIndex])
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

    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        reader.seek(baseAddress)
        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(baseAddress + numEntries * 0x8 + i * 0x8C + 4)
            result.add(reader.read(0x8C).array())
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
        writer.writeInt(configs.size)
        for (i in 0 until configs.size) {
            writer.writeInt(0x4900)
            writer.writeInt(i * 0x8C + configs.size * 8 + 4)
        }
        for (config in configs)
            writer.write(config)
    }

    private fun calculateFileSize(): Int {
        return entries.size * 8 + configs.size * 0x94 + 0x40
    }
}