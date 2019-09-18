package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Archive
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import java.net.URL
import java.util.*

class ImportArchiveController(destinationCsar: Csar) : AbstractImportController<Archive>(destinationCsar) {
    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        super.initialize(p0, p1)
        val hasNoSelectionProperty = Bindings.isEmpty(selectionView.selectionModel.selectedItems)
        val noPlayerProperty = Bindings.isNull(playerBox.selectionModel.selectedItemProperty())
        importButton.disableProperty().bind(hasNoSelectionProperty.or(noPlayerProperty))
        selectionView.selectionModel.selectionMode = SelectionMode.MULTIPLE
        stage.title = "Import Archives"
    }

    override fun onOpenCsar() {
        selectionView.items = sourceCsar?.archives?.filtered { it.name.value != null }
    }

    override fun doImport() {
        destinationCsar.importArchives(
            sourceCsar!!,
            selectionView.selectionModel.selectedItems,
            playerBox.selectionModel.selectedItem
        )
    }
}