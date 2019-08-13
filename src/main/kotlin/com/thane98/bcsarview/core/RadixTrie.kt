package com.thane98.bcsarview.core

import java.lang.StringBuilder

class RadixNode<T>(
    var left: RadixNode<T>?,
    var right: RadixNode<T>?,
    val index: Int,
    val bit: Int,
    val name: String?,
    val data: T?
) {
    val isLeaf = index == -1

    constructor(left: RadixNode<T>?, right: RadixNode<T>?, index: Int, bit: Int) : this(
        left,
        right,
        index,
        bit,
        null,
        null
    )

    constructor(name: String, data: T) : this(null, null, -1, -1, name, data)

    override fun toString(): String {
        val sb = StringBuilder("RadixNode(")
        if (isLeaf)
            sb.append("Name: $name, Data: ${data.toString()}")
        else
            sb.append("Index: $index, Bit: $bit")
        sb.append(")")
        return sb.toString()
    }
}

class RadixTrie<T> {
    var root: RadixNode<T>? = null
    var numNodes = 0

    fun lookup(name: String): T? {
        return lookupHelper(name, root)
    }

    private fun lookupHelper(name: String, curNode: RadixNode<T>?): T? {
        return when {
            curNode == null -> null
            curNode.isLeaf -> {
                if (curNode.name == name)
                    curNode.data
                else
                    null
            }
            else -> {
                if (getTestBit(name, curNode.index, curNode.bit))
                    lookupHelper(name, curNode.right)
                else
                    lookupHelper(name, curNode.left)
            }
        }
    }

    fun put(name: String, data: T) {
        root = putHelper(name, data, root)
    }

    private fun getTestBit(name: String, index: Int, bit: Int): Boolean {
        if (index > name.length)
            return false
        val bits = Integer.toBinaryString(name[index].toInt()).padStart(8, '0')
        return bits[bit] == '1'
    }

    private fun putHelper(name: String, data: T, curNode: RadixNode<T>?): RadixNode<T> {
        return when {
            curNode == null -> {
                numNodes++
                RadixNode(name, data)
            }
            !curNode.isLeaf -> {
                if (getTestBit(name, curNode.index, curNode.bit))
                    curNode.right = putHelper(name, data, curNode.right)
                else
                    curNode.left = putHelper(name, data, curNode.left)
                curNode
            }
            else -> {
                numNodes += 2 // New leaf + new internal node
                turnIntoInternalNode(name, data, curNode)
            }
        }
    }

    private fun turnIntoInternalNode(name: String, data: T, target: RadixNode<T>): RadixNode<T> {
        val newNode = RadixNode(name, data)
        val larger = if (name.length > target.name!!.length) newNode else target
        val smaller = if (larger != newNode) newNode else target
        val testInfo = findTestInfo(larger.name!!, smaller.name!!)

        var result = RadixNode<T>(null, null, testInfo.first, testInfo.second)
        result = putHelper(name, data, result)
        result = putHelper(target.name, target.data!!, result)
        return result
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
        toStringHelper(sb, root)
        return sb.toString()
    }

    private fun toStringHelper(sb: StringBuilder, curNode: RadixNode<T>?) {
        if (curNode == null)
            return
        if (curNode.isLeaf)
            sb.appendln(curNode.toString())
        else {
            sb.appendln(curNode.toString())
            toStringHelper(sb, curNode.right)
            toStringHelper(sb, curNode.left)
        }
    }
}