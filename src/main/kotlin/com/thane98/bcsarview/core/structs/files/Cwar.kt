package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.ByteListWriter
import com.thane98.bcsarview.core.io.verifyMagic
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class Cwar() {
    val files = mutableListOf<ByteArray>()

    constructor(reader: IBinaryReader): this() {
        val baseAddress = reader.tell()
        reader.verifyMagic("CWAR")
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x24)
        val fileAddress = baseAddress + reader.readInt() + 8
        files.addAll(readFiles(reader, infoAddress, fileAddress))
    }

    private fun readFiles(reader: IBinaryReader, baseAddress: Long, fileAddress: Long): MutableList<ByteArray> {
        reader.seek(baseAddress)
        val result = mutableListOf<ByteArray>()
        val numFiles = reader.readInt()
        for (i in 0 until numFiles) {
            reader.seek(baseAddress + i * 0xC + 8)
            val address = reader.readInt()
            val size = reader.readInt()
            reader.seek(fileAddress + address)
            result.add(reader.read(size).array())
        }
        return result
    }

    fun serialize(byteOrder: ByteOrder): ByteArray {
        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, byteOrder)
        val rawFiles = mutableListOf<Byte>()
        writer.write("CWAR".toByteArray(StandardCharsets.UTF_8))
        writer.writeShort(0xFEFF)
        writer.writeShort(0x40)
        writer.writeInt(0x1000000)
        writer.writeInt(0) // File fileSize. Need to revisit.
        writer.writeInt(0x2)
        writer.writeInt(0x6800)
        writer.writeInt(0x40)
        writer.writeInt(0) // INFO fileSize. Need to revisit.
        writer.writeInt(0x6801)
        writer.writeInt(0) // FILE address. Need to revisit.
        writer.writeInt(0) // FILE fileSize. Need to revisit.
        while(result.size != 0x40) writer.writeInt(0)
        writer.write("INFO".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(0) // INFO fileSize. Need to revisit.
        writer.writeInt(files.size)
        for (i in 0 until files.size) {
            val file = files[i]
            writer.writeInt(0x1F00)
            writer.writeInt(rawFiles.size + 0x18)
            writer.writeInt(file.size)
            for (byte in file)
                rawFiles.add(byte)
            if (i != files.lastIndex)
                while (rawFiles.size % 0x20 != 0) rawFiles.add(0)
        }
        while (result.size % 0x20 != 0) writer.writeByte(0)
        val infoSize = result.size - 0x40
        writer.write("FILE".toByteArray(StandardCharsets.UTF_8))
        writer.writeInt(rawFiles.size)
        while (result.size % 0x20 != 0) writer.writeInt(0)
        writer.write(rawFiles.toByteArray())
        val fileSize = writer.tell()
        writer.seek(0xC)
        writer.writeInt(fileSize)
        writer.seek(0x1C)
        writer.writeInt(infoSize)
        writer.seek(0x24)
        writer.writeInt(infoSize + 0x40)
        writer.writeInt(fileSize - infoSize - 0x40)
        writer.seek(0x44)
        writer.writeInt(infoSize)
        return result.toByteArray()
    }
}