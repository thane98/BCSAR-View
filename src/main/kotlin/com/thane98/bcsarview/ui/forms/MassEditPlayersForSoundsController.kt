package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.Player
import javafx.beans.binding.Bindings
import javafx.scene.control.ComboBox
import java.net.URL
import java.util.*

class MassEditPlayersForSoundsController(private val csar: Csar): AbstractMassEditController<AudioConfig>(csar.configs) {
    private val editorBox = ComboBox<Player>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        editorContainer.children.add(editorBox)
        editorBox.items = csar.players
        editorBox.prefWidthProperty().bind(editorContainer.widthProperty())
        commitButton.disableProperty().bind(Bindings.isNull(editorBox.selectionModel.selectedItemProperty()))
    }

    override fun commitChanges() {
        for (config in selection())
            config.player.value = editorBox.selectionModel.selectedItem
    }
}