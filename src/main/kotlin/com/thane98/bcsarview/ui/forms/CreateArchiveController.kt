package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.io.retrievers.InMemoryFileRetriever
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.core.structs.entries.Archive
import com.thane98.bcsarview.core.structs.entries.InternalFileReference
import com.thane98.bcsarview.core.structs.files.Cwar
import com.thane98.bcsarview.ui.utils.createIntegerTextFormatter
import javafx.fxml.FXML
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class CreateArchiveController(private val csar: Csar) : AbstractCreateController() {
    @FXML
    private lateinit var nameField: TextField
    @FXML
    private lateinit var unknownField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        unknownField.textFormatter = createIntegerTextFormatter()
        createButton.disableProperty().bind(nameField.textProperty().isEmpty.or(unknownField.textProperty().isEmpty))
    }

    override fun createAndInsert(): AbstractNamedEntry {
        val record = InternalFileReference()
        record.retriever = InMemoryFileRetriever(Cwar().serialize(csar.byteOrder), csar.byteOrder)
        csar.files.add(record)

        val archive = Archive()
        archive.name.value = nameField.text
        archive.file.value = record
        archive.entryCount.value = 0
        archive.unknown.value = Integer.parseInt(unknownField.text)
        csar.archives.add(archive)
        return archive
    }
}