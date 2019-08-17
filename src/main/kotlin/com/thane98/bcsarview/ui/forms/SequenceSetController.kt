package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.SequenceSet
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import java.net.URL
import java.util.*

class SequenceSetController: Initializable {
    @FXML
    private lateinit var table: TableView<SequenceSet>
    @FXML
    private lateinit var nameColumn: TableColumn<SequenceSet, String>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<SequenceSet, String>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { SimpleStringProperty(it.value.toString()) }
        unknownThreeColumn.setCellValueFactory { SimpleStringProperty(it.value.unknownThree.toString()) }
    }

    fun onFileChange(csar: Csar?) { table.items = csar?.sequenceSets}
}