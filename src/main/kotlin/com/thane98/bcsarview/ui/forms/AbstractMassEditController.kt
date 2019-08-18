package com.thane98.bcsarview.ui.forms

import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.net.URL
import java.util.*

abstract class AbstractMassEditController<T>(private val items: ObservableList<T>): Initializable {
    @FXML
    private lateinit var stage: Stage
    @FXML
    private lateinit var searchField: TextField
    @FXML
    private lateinit var selectionView: ListView<T>
    @FXML
    protected lateinit var editorContainer: VBox
    @FXML
    protected lateinit var commitButton: Button

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        val filteredList = FilteredList<T>(items)
        searchField.textProperty().addListener { _ -> filteredList.setPredicate { it.toString().contains(searchField.text) } }
        selectionView.items = filteredList
        selectionView.selectionModel.selectionMode = SelectionMode.MULTIPLE
    }

    @FXML
    private fun commit() {
        commitChanges()
        stage.close()
    }

    @FXML
    private fun cancel() { stage.close() }

    protected abstract fun commitChanges()

    protected fun selection(): List<T> { return selectionView.selectionModel.selectedItems }
}