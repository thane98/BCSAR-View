package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.Player
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class PlayerController : AbstractEntryController<Player>() {
    @FXML
    private lateinit var nameColumn: TableColumn<Player, StrgEntry>
    @FXML
    private lateinit var soundLimitColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var unknownColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var heapSizeColumn: TableColumn<Player, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { it.value.strgEntry }
        nameColumn.setCellFactory { StrgEntryTableCell<Player>() }
        soundLimitColumn.setCellValueFactory { it.value.soundLimit }
        soundLimitColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        heapSizeColumn.setCellValueFactory { it.value.heapSize }
        heapSizeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.players) }
}