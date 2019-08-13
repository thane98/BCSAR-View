package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Bank
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class BankController : Initializable {
    @FXML
    private lateinit var table: TableView<Bank>
    @FXML
    private lateinit var nameColumn: TableColumn<Bank, String>
    @FXML
    private lateinit var unknownColumn: TableColumn<Bank, ByteArray>
    @FXML
    private lateinit var unknownTwoColumn: TableColumn<Bank, Number>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.setCellFactory { ByteArrayTableCell<Bank>() }
        unknownTwoColumn.setCellValueFactory { it.value.unknownTwo }
        unknownTwoColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    fun onFileChange(csar: Csar?) { table.items = csar?.banks }
}