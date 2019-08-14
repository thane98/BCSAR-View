package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.Bank
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.FileChooser
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

    val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupContextMenu()
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        unknownColumn.setCellValueFactory { it.value.unknown }
        unknownColumn.setCellFactory { ByteArrayTableCell<Bank>() }
        unknownTwoColumn.setCellValueFactory { it.value.unknownTwo }
        unknownTwoColumn.cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter())
    }

    private fun setupContextMenu() {
        table.setRowFactory {
            val row = TableRow<Bank>()
            val contextMenu = ContextMenu()
            val dumpItem = MenuItem("Dump")
            contextMenu.items.addAll(dumpItem)
            dumpItem.setOnAction { dumpBank(row.item) }
            row.contextMenu = contextMenu
            row
        }
    }

    private fun dumpBank(bank: Bank) {
        val chooser = createDumpBankDialog(bank)
        val result = chooser.showSaveDialog(table.scene.window)
        if (result != null)
            csar.value.dumpFile(bank.file.value, result.toPath())
    }

    private fun createDumpBankDialog(bank: Bank): FileChooser {
        val chooser = FileChooser()
        chooser.title = "Save bank..."
        chooser.initialFileName = "$bank.cbnk"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Bank", "*.cbnk"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        return chooser
    }

    fun onFileChange(csar: Csar?) { table.items = csar?.banks }
}