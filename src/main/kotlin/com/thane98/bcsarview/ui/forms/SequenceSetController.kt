package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.StrgEntry
import com.thane98.bcsarview.core.structs.entries.SequenceSet
import com.thane98.bcsarview.ui.utils.ByteArrayTableCell
import com.thane98.bcsarview.ui.utils.StrgEntryTableCell
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import java.net.URL
import java.util.*

class SequenceSetController: AbstractEntryController<SequenceSet>() {
    @FXML
    private lateinit var nameColumn: TableColumn<SequenceSet, StrgEntry>
    @FXML
    private lateinit var unknownThreeColumn: TableColumn<SequenceSet, String>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        nameColumn.setCellValueFactory { it.value.strgEntry }
        nameColumn.setCellFactory { StrgEntryTableCell<SequenceSet>() }
        unknownThreeColumn.setCellValueFactory { SimpleStringProperty(it.value.unknownThree.toString()) }
    }

    fun onFileChange(csar: Csar?) { table.items = if (csar == null) null else FilteredList(csar.sequenceSets) }
}