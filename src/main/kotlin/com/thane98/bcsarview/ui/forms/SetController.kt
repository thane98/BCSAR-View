package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.*
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import com.thane98.bcsarview.ui.utils.DialogTableCell
import com.thane98.bcsarview.ui.utils.applyStyles
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*


class SetController : Initializable {
    @FXML
    private lateinit var table: TableView<BaseSet>
    @FXML
    private lateinit var nameColumn: TableColumn<BaseSet, String>
    @FXML
    private lateinit var soundTypeColumn: TableColumn<BaseSet, Number>
    @FXML
    private lateinit var unknownColumn: TableColumn<BaseSet, ByteArray>
    @FXML
    private lateinit var detailsColumn: TableColumn<BaseSet, BaseSet>

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        soundTypeColumn.cellValueFactory =
            Callback<TableColumn.CellDataFeatures<BaseSet, Number>, ObservableValue<Number>> { p -> p.value.soundType }
        soundTypeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.setCellFactory { ByteArrayTableCell<BaseSet>() }
        detailsColumn.setCellValueFactory { SimpleObjectProperty<BaseSet>(it.value) }
        detailsColumn.setCellFactory {
            val loader = FXMLLoader()
            val controller = SetDetailsController(csar.value)
            loader.setController(controller)
            val detailsForm = loader.load(this.javaClass.getResourceAsStream("SetDetails.fxml")) as Stage
            applyStyles(detailsForm.scene)
            detailsForm.initOwner(table.scene.window)
            detailsForm.initModality(Modality.WINDOW_MODAL)
            detailsForm.isResizable = false
            DialogTableCell<BaseSet, BaseSet>(detailsForm) { set: BaseSet -> controller.currentSet.value = set }
        }
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<BaseSet>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            val extractItem = MenuItem("Extract Sounds")
            contextMenu.items.addAll(dumpItem, extractItem)
            dumpItem.setOnAction { dumpSet(row.item) }
            extractItem.setOnAction { extractSet(row.item) }
            row.setOnContextMenuRequested { e ->
                if (row.item != null && row.item.subEntry.value !is SequenceSet)
                    contextMenu.show(row, e.screenX, e.screenY)
            }
            row
        }
    }

    private fun dumpSet(baseSet: BaseSet) {
        if (baseSet.subEntry.value is SequenceSet)
            return
        val chooser = FileChooser()
        chooser.title = "Save archive..."
        chooser.initialFileName = "$baseSet.cwsd"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Set", "*.cwsd"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )

        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null) {
            val soundSet = baseSet.subEntry.value as SoundSet
            csar.value.dumpFile(soundSet.file.value, result.toPath())
        }
    }

    private fun extractSet(baseSet: BaseSet) {
        val chooser = DirectoryChooser()
        chooser.title = "Extract sounds..."

        val result = chooser.showDialog(table.scene.window)
        if (result != null)
            csar.value.extractSoundSet(baseSet, result.toPath())
    }

    fun onFileChange(csar: Csar?) {
        if (csar == null)
            table.items = null
        else
            table.items = csar.sets as ObservableList<BaseSet>
    }
}