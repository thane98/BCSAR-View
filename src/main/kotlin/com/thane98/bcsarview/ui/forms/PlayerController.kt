package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Player
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class PlayerController : Initializable {
    @FXML
    private lateinit var table: TableView<Player>
    @FXML
    private lateinit var nameColumn: TableColumn<Player, String>
    @FXML
    private lateinit var soundLimitColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var unknownColumn: TableColumn<Player, Number>
    @FXML
    private lateinit var heapSizeColumn: TableColumn<Player, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        soundLimitColumn.setCellValueFactory { it.value.soundLimit }
        soundLimitColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
        heapSizeColumn.setCellValueFactory { it.value.heapSize }
        heapSizeColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    fun onFileChange(csar: Csar?) { table.items = csar?.players }
}