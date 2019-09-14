package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IBinaryWriter
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

data class InfxEntry(var itemId: Int, val itemType: Int, val unknown: Int)

class Cgrp() {
    val infoEntries = mutableListOf<ByteArray>()
    val infxEntries = mutableListOf<InfxEntry>()

    constructor(reader: IBinaryReader): this() {
        val baseAddress = reader.tell()
        reader.verifyMagic("CGRP")
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt()
        reader.seek(baseAddress + 0x30)
        val infxAddress = baseAddress + reader.readInt()

        infoEntries.addAll(readInfo(reader, infoAddress))
        infxEntries.addAll(readInfx(reader, infxAddress))
    }

    private fun readInfo(reader: IBinaryReader, infoAddress: Long): MutableList<ByteArray> {
        val result = mutableListOf<ByteArray>()
        reader.seek(infoAddress)
        reader.verifyMagic("INFO")
        reader.seek(infoAddress + 8)

        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(infoAddress + 0x10 + i * 8)
            reader.seek(infoAddress + reader.readInt() + 8)
            result.add(reader.read(0x10).array())
        }
        return result
    }

    private fun readInfx(reader: IBinaryReader, infxAddress: Long): MutableList<InfxEntry> {
        val result = mutableListOf<InfxEntry>()
        reader.seek(infxAddress)
        reader.verifyMagic("INFX")
        reader.seek(infxAddress + 8)

        val numEntries = reader.readInt()
        for (i in 0 until numEntries) {
            reader.seek(infxAddress + 0x10 + i * 8)
            reader.seek(infxAddress + reader.readInt() + 8)

            val itemId = reader.readInt24()
            val itemType = reader.readByte()
            val unknown = reader.readInt()
            result.add(InfxEntry(itemId, itemType, unknown))
        }
        return result
    }

    fun serialize(byteOrder: ByteOrder): ByteArray {
        // First pass of header
        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, byteOrder)
        writer.write("CGRP".toByteArray(StandardCharsets.UTF_8))
        writer.writeShort(0xFEFF)
        writer.writeShort(0x40)
        writer.writeInt(0x1010000)
        writer.writeInt(0) // File size. Need to revisit.
        writer.writeInt(0x3)
        writer.writeInt(0x7800)
        writer.writeInt(0x40)
        writer.writeInt(0) // INFO size. Need to revisit.
        writer.writeInt(0x7801)
        writer.writeInt(0) // FILE address. Need to revisit.
        writer.writeInt(0x20)
        writer.writeInt(0x7802)
        writer.writeInt(0) // INFX address. Need to revisit.
        writer.writeInt(0) // INFX size. Need to revisit
        while (writer.tell() % 0x20 != 0) writer.writeByte(0)

        // Write INFO
        serializeInfo(writer)
        while (writer.tell() % 0x20 != 0) writer.writeInt(0)
        val infoSize = (writer.tell() - 0x40) + (0x20 - (writer.tell() % 0x20)) % 0x20

        // Write FILE
        val fileAddress = writer.tell()
        writer.write("FILE".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0x20)
        while (writer.tell() % 0x20 != 0) writer.writeInt(0)

        // Write INFX
        val infxAddress = writer.tell()
        serializeInfx(writer)
        val infxSize = (writer.tell() - infxAddress) + (0x20 - (writer.tell() % 0x20)) % 0x20

        // Apply corrections to partition headers
        val fileSize = writer.tell() + (0x20 - (writer.tell() % 0x20)) % 0x20
        writer.seek(0xC)
        writer.writeInt(fileSize)
        writer.seek(0x1C)
        writer.writeInt(infoSize)
        writer.seek(0x24)
        writer.writeInt(fileAddress)
        writer.seek(0x30)
        writer.writeInt(infxAddress)
        writer.writeInt(infxSize)

        writer.seek(0x44)
        writer.writeInt(infoSize)
        writer.seek(infxAddress + 4)
        writer.writeInt(infxSize)
        return result.toByteArray()
    }

    private fun serializeInfo(writer: IBinaryWriter) {
        writer.write("INFO".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0)
        writer.writeInt(infoEntries.size)
        for (i in 0 until infoEntries.size) {
            writer.writeInt(0x7900)
            writer.writeInt(infoEntries.size * 0x8 + i * 0x10 + 4)
        }
        for (entry in infoEntries)
            writer.write(entry)
    }

    private fun serializeInfx(writer: IBinaryWriter) {
        writer.write("INFX".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0)
        writer.writeInt(infxEntries.size)
        for (i in 0 until infxEntries.size) {
            writer.writeInt(0x7901)
            writer.writeInt(infxEntries.size * 0x8 + i * 0x8 + 4)
        }
        for (entry in infxEntries) {
            writer.writeInt24(entry.itemId)
            writer.writeByte(entry.itemType)
            writer.writeInt(entry.unknown)
        }
    }
}