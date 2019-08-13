package com.thane98.bcsarview.core.structs.files

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.io.verifyMagic
import javafx.beans.property.SimpleIntegerProperty

private data class ArchiveEntry(val address: Long, val size: Long)

class Cwar(reader: IBinaryReader, baseAddress: Long) {
    val numFiles = SimpleIntegerProperty()
    private val files: List<ArchiveEntry>

    init {
        reader.seek(baseAddress)
        reader.verifyMagic("CWAR")
        reader.seek(baseAddress + 0x18)
        val infoAddress = baseAddress + reader.readInt() + 8
        reader.seek(baseAddress + 0x24)
        val fileAddress = baseAddress + reader.readInt() + 8
        files = readAddressTable(reader, infoAddress, fileAddress)
    }

    fun extractFile(reader: IBinaryReader, index: Int): ByteArray {
        reader.seek(files[index].address)
        return reader.read(files[index].size.toInt()).array()
    }

    private fun readAddressTable(reader: IBinaryReader, baseAddress: Long, fileAddress: Long): List<ArchiveEntry> {
        reader.seek(baseAddress)
        val result = mutableListOf<ArchiveEntry>()
        numFiles.value = reader.readInt()
        for (i in 0 until numFiles.value) {
            reader.seek(baseAddress + i * 0xC + 8)
            val address = fileAddress + reader.readInt()
            val size = reader.readInt().toLong()
            result.add(ArchiveEntry(address, size))
        }
        return result
    }
}