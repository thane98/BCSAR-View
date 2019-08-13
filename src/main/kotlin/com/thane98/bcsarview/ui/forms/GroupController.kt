package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.SoundGroup
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class GroupController : Initializable {
    @FXML
    private lateinit var table: TableView<SoundGroup>
    @FXML
    private lateinit var nameColumn: TableColumn<SoundGroup, String>
    @FXML
    private lateinit var unknownColumn: TableColumn<SoundGroup, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    fun onFileChange(csar: Csar?) { table.items = csar?.groups }
}