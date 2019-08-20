package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty

abstract class AbstractNamedEntry : IEntry {
    val name = SimpleStringProperty()
    var strgEntry: StrgEntry? = null

    override fun toString(): String { return name.value ?: "Anonymous" }
}