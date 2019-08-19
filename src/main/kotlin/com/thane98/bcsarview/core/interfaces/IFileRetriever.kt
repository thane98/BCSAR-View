package com.thane98.bcsarview.core.interfaces

interface IFileRetriever {
    fun fileSize(): Int
    fun retrieve(): ByteArray
    fun open(): IBinaryReader
}