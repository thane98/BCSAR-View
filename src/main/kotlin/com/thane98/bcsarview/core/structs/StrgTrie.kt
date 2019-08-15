package com.thane98.bcsarview.core.structs

import com.thane98.bcsarview.core.io.ByteListWriter
import java.lang.StringBuilder

data class StrgTrieNode(
    var left: Int,
    var right: Int,
    var charIndex: Int,
    var bit: Int,
    val data: StrgEntry?
) {
    var isLeaf = data != null

    override fun toString(): String {
        return "StrgTrieNode(left=$left, right=$right, charIndex=$charIndex, bit=$bit, data=$data)"
    }
}

class StrgTrie {
    private val con = mutableListOf<StrgTrieNode>()
    private var rootIndex = -1

    fun insert(strgEntry: StrgEntry) {
        rootIndex = insertHelper(strgEntry, rootIndex)
    }

    fun serialize(csar: Csar): ByteArray {
        val result = mutableListOf<Byte>()
        val writer = ByteListWriter(result, csar.byteOrder)
        writer.writeInt(rootIndex)
        writer.writeInt(con.size)
        for (node in con) {
            writer.writeShort(if (node.isLeaf) 1 else 0)
            writer.writeShort(node.charIndex.shl(3).or(node.bit))
            writer.writeInt(node.left)
            writer.writeInt(node.right)
            if (node.data != null) {
                writer.writeInt(node.data.index)
                writer.writeInt24(node.data.resourceId)
                writer.writeByte(node.data.type)
            } else {
                writer.writeInt(-1)
                writer.writeInt(-1)
            }
        }
        return result.toByteArray()
    }

    private fun insertHelper(strgEntry: StrgEntry, index: Int): Int {
        assert(index <= con.size)
        if (index == -1)
            return createAndInsertLeaf(strgEntry)

        val curNode = con[index]
        return if (!curNode.isLeaf) {
            if (getTestBit(strgEntry, curNode.charIndex, curNode.bit))
                curNode.right = insertHelper(strgEntry, curNode.right)
            else
                curNode.left = insertHelper(strgEntry, curNode.left)
            index
        } else {
            createAndInsertInternalNode(strgEntry, index)
        }
    }

    private fun getTestBit(strgEntry: StrgEntry, charIndex: Int, bit: Int): Boolean {
        if (charIndex >= strgEntry.name.length)
            return false
        val char = strgEntry.name[charIndex].toInt()
        val bits = Integer.toBinaryString(char).padStart(8, '0')
        return bits[bit] == '1'
    }

    private fun createAndInsertLeaf(strgEntry: StrgEntry): Int {
        val node = StrgTrieNode(-1, -1, -1, -1, strgEntry)
        con.add(node)
        return con.lastIndex
    }

    private fun createAndInsertInternalNode(strgEntry: StrgEntry, oldNodeIndex: Int): Int {
        // First, find the bit test info for the new internal node
        assert(oldNodeIndex >= 0 && oldNodeIndex < con.size && con[oldNodeIndex].isLeaf)
        val newNodeIndex = createAndInsertLeaf(strgEntry)
        val smaller = if (con[newNodeIndex].data!!.name < con[oldNodeIndex].data!!.name) con[newNodeIndex] else con[oldNodeIndex]
        val larger = if (smaller == con[oldNodeIndex]) con[newNodeIndex] else con[oldNodeIndex]
        val testInfo = findTestInfo(smaller.data!!.name, larger.data!!.name)

        // Next, determine which node goes left and which node goes right
        val newInternalNode = if (getTestBit(strgEntry, testInfo.first, testInfo.second))
            StrgTrieNode(oldNodeIndex, newNodeIndex, testInfo.first, testInfo.second, null)
        else
            StrgTrieNode(newNodeIndex, oldNodeIndex, testInfo.first, testInfo.second, null)
        con.add(newInternalNode)
        return con.lastIndex
    }

    private fun findTestInfo(smaller: String, larger: String): Pair<Int, Int> {
        val paddedSmaller = smaller.padEnd(larger.length, 0.toChar())
        var i = 0
        while (paddedSmaller[i] == larger[i]) i++

        val charOne = Integer.toBinaryString(paddedSmaller[i].toInt()).padStart(8, '0')
        val charTwo = Integer.toBinaryString(larger[i].toInt()).padStart(8, '0')
        var bit = 0
        while (charOne[bit] == charTwo[bit]) bit++
        return Pair(i, bit)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        sb.append(con.joinToString { it.toString() })
        sb.append(']')
        return sb.toString()
    }
}