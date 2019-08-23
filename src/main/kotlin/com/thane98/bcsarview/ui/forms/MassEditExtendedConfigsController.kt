package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import javafx.beans.binding.Bindings
import javafx.scene.control.ComboBox
import javafx.scene.control.Tooltip
import java.net.URL
import java.util.*

class MassEditExtendedConfigsController(private val csar: Csar) : AbstractMassEditController<AudioConfig>(csar.configs) {
    private val editorBox = ComboBox<AudioConfig>()
    private val tooltip = Tooltip()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        tooltip.text = "All highlighted sounds in the list will be adjusted to use the selected sound's config."
        editorContainer.children.add(editorBox)
        editorBox.items = csar.configs
        editorBox.tooltip = tooltip
        editorBox.prefWidthProperty().bind(editorContainer.widthProperty())
        commitButton.disableProperty().bind(Bindings.isNull(editorBox.selectionModel.selectedItemProperty()))
    }

    override fun commitChanges() {
        val source = editorBox.value
        for (sound in selection()) {
            sound.setIndexAddress = source.setIndexAddress
            sound.unknownThree.value = source.unknownThree.value.copyOf()
        }
    }
}