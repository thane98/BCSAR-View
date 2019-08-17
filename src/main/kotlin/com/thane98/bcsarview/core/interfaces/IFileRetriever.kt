package com.thane98.bcsarview.core.interfaces

interface IFileRetriever {
    fun retrieve(): ByteArray
    fun open(): IBinaryReader
}