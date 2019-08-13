package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IBinaryReader
import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.interfaces.IEntryVisitor
import javafx.beans.property.SimpleLongProperty

class InternalFileReference() : IEntry {
    var fileAddress: Long = 0
    var fileSize: Long = 0

    constructor(reader: IBinaryReader, baseAddress: Long): this() {
        reader.seek(baseAddress + 4)
        reader.seek(baseAddress + reader.readInt() + 4) // Skip resource Id
        fileAddress = reader.readInt().toLong()
        fileSize = reader.readInt().toLong()
    }

    override fun <T> accept(visitor: IEntryVisitor<T>): T { return visitor.visitInternalFileReference(this) }
}