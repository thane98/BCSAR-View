package com.thane98.bcsarview.ui.utils

import com.aneagle.SpecialTextArea
import javafx.scene.Node
import javafx.scene.control.TableCell
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox

class HexAreaTableCell<T> : TableCell<T, ByteArray>() {
    override fun updateItem(item: ByteArray?, empty: Boolean) {
        super.updateItem(item, empty)
        graphic = if (empty || item == null) {
            null
        } else {
            generateEditor(item)
        }
    }

    private fun generateEditor(item: ByteArray): Node {
        val result = VBox()
        val toggle = ToggleButton("Toggle Editor")
        toggle.isSelected = false
        toggle.prefWidthProperty().bind(result.widthProperty())
        val editor = SpecialTextArea(generateMask(item))
        editor.visibleProperty().bind(toggle.selectedProperty())
        editor.managedProperty().bind(toggle.selectedProperty())
        editor.forceSetText(byteArrayToText(item))
        editor.id = "hex-area"
        editor.prefHeight = 150.0
        editor.isWrapText = true
        editor.textProperty().addListener { _ -> parseTextIntoArray(editor.text, item) }
        result.children.addAll(toggle, editor)
        return result
    }

    private fun parseTextIntoArray(text: String, array: ByteArray) {
        val newBytes = text.split(' ').map { Integer.parseInt(it, 16).toByte() }
        assert(newBytes.size == array.size)
        for (i in 0 until newBytes.size)
            array[i] = newBytes[i]
    }

    private fun generateMask(item: ByteArray): String {
        val sb = StringBuilder()
        for (i in 0 until item.size) {
            sb.append("HH")
            if (i < item.size - 1)
                sb.append(' ')
        }
        return sb.toString()
    }
}