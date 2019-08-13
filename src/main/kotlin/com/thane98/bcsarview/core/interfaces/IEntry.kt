package com.thane98.bcsarview.core.interfaces

interface IEntry {
    fun <T> accept(visitor: IEntryVisitor<T>): T
}