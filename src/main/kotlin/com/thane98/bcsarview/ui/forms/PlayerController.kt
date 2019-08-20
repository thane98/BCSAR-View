package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Player
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class PlayerController : AbstractEntryController<Player>() {
    @FXML
    private lateinit var nameColumn: TableColumn<Player, String>
    @FXML
    private lateinit var soundLimitColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var unknownColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var heapSizeColumn: TableColumn<Player, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { it.value.name }
        nameColumn.cellFactory = TextFieldTableCell.forTableColumn()
        soundLimitColumn.setCellValueFactory { it.value.soundLimit }
        soundLimitColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        heapSizeColumn.setCellValueFactory { it.value.heapSize }
        heapSizeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    override fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.players) }
}