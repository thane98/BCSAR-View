package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import java.net.URL
import java.util.*

class ImportExternalSoundsController(destinationCsar: Csar): AbstractImportController<AudioConfig>(destinationCsar) {
    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        val hasNoSelectionProperty = Bindings.isEmpty(selectionView.selectionModel.selectedItems)
        val noPlayerProperty = Bindings.isNull(playerBox.selectionModel.selectedItemProperty())
        importButton.disableProperty().bind(hasNoSelectionProperty.or(noPlayerProperty))
        selectionView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        stage.title = "Import External Sounds"
    }

    override fun onOpenCsar() {
        selectionView.items = sourceCsar?.configs?.filtered { it.configType == ConfigType.EXTERNAL_SOUND }
    }

    override fun doImport() {
        destinationCsar.importExternalSounds(
            selectionView.selectionModel.selectedItems,
            playerBox.selectionModel.selectedItem
        )
    }
}