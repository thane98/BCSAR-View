package com.thane98.bcsarview.core.structs.entries

import com.thane98.bcsarview.core.interfaces.IEntry
import com.thane98.bcsarview.core.structs.StrgEntry
import javafx.beans.property.SimpleObjectProperty

abstract class AbstractNamedEntry : IEntry {
    val strgEntry = SimpleObjectProperty<StrgEntry>()
}