package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.io.ByteArrayBinaryReader
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import com.thane98.bcsarview.core.utils.putInt
import java.lang.IllegalStateException
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class CwsdEntry(var archiveIndex: Int, var archiveId: Int)

class Cwsd() {
    val entries = mutableListOf<CwsdEntry>()
    val configs = mutableListOf<ByteArray>()

    constructor(reader: IBinaryReader): this() {
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
        entries.addAll(readEntryTable(reader, entryTableAddress))
        configs.addAll(readConfigTable(reader, configTableAddress, (baseAddress + fileSize).toInt()))
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

    private fun readEntryTable(reader: IBinaryReader, baseAddress: Long): MutableList<CwsdEntry> {
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

    private fun readConfigTable(reader: IBinaryReader, baseAddress: Long, fileEnd: Int): MutableList<ByteArray> {
        val result = mutableListOf<ByteArray>()
        val addresses = readConfigAddresses(reader, baseAddress)
        for (i in 0 until addresses.size) {
            val length = if (i == addresses.lastIndex)
                fileEnd - (baseAddress + addresses[i])
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
        writer.write("CWSD".toByteArray(StandardCharsets.UTF_8))
        writer.writeShort(0xFEFF)
        writer.writeShort(0x20)
        writer.writeInt(0x1000100)
        writer.writeInt(0) // File size. Need to revisit.
        writer.writeInt(1)
        writer.writeInt(0x6800)
        writer.writeInt(0x20)
        writer.writeInt(0) // INFO partition size. Need to revisit.
        val infoSize = serializeInfo(writer, byteOrder)
        writer.seek(0xC)
        writer.writeInt(result.size)
        writer.seek(0x1C)
        writer.writeInt(infoSize)
        return result.toByteArray()
    }

    private fun serializeInfo(writer: IBinaryWriter, byteOrder: ByteOrder): Int {
        val baseAddress = writer.tell()
        writer.write("INFO".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0) // INFO partition size. Need to revisit.
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
        adjustConfigs(byteOrder)
        writer.writeInt(configs.size)
        for (i in 0 until configs.size) {
            writer.writeInt(0x4900)
            writer.writeInt(rawConfigs.size + configs.size * 8 + 4)
            for (byte in configs[i])
                rawConfigs.add(byte)
        }
        writer.write(rawConfigs.toByteArray())
        val infoSize = writer.tell() - baseAddress
        writer.seek(baseAddress + 4)
        writer.writeInt(infoSize)
        return writer.tell() - 0x20
    }

    // CWSD configs have a field for their index. Ex. For sound 5 the value is 5, sound 4 the value is 4, etc.
    // To make insertion / removal easier, we'll redo this field whenever we go to save.
    private fun adjustConfigs(byteOrder: ByteOrder) {
        for (i in 0 until configs.size) {
            val reader = ByteArrayBinaryReader(configs[i], byteOrder)
            reader.use {
                reader.seek(0x14)
                reader.seek(reader.readInt().toLong())
                if (reader.readInt() == 1) {
                    val type = reader.readInt()
                    if (type != 0x4902)
                        throw IllegalStateException("Found type 0x${type.toString(16)} while adjust CWSD configs.")
                    reader.seek(reader.tell() + 4)
                    configs[i].putInt(i, reader.tell().toInt(), byteOrder)
                }
            }
        }
    }
}