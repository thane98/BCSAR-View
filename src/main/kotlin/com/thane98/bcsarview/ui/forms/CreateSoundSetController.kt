package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.io.retrievers.InMemoryFileRetriever
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.core.structs.entries.Archive
import com.thane98.bcsarview.core.structs.entries.InternalFileReference
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.core.structs.files.Cwsd
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class CreateSoundSetController(private val csar: Csar) : AbstractCreateController() {
    @FXML
    private lateinit var nameField: TextField
    @FXML
    private lateinit var templateSetBox: ComboBox<SoundSet>
    @FXML
    private lateinit var archiveBox: ComboBox<Archive>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        templateSetBox.items = csar.soundSets.filtered { it.archive.value != null }
        archiveBox.items = csar.archives.filtered { it.name.value != null }
        createButton.disableProperty().bind(
            nameField.textProperty().isEmpty
                .or(templateSetBox.valueProperty().isNull)
                .or(archiveBox.valueProperty().isNull)
        )
    }

    override fun createAndInsert(): AbstractNamedEntry {
        val record = InternalFileReference()
        record.retriever = InMemoryFileRetriever(Cwsd().serialize(csar.byteOrder), csar.byteOrder)
        csar.files.add(record)

        val template = templateSetBox.value
        val set = SoundSet()
        set.name.value = nameField.text
        set.unknown.value = template.unknown.value.copyOf()
        set.unknownTwo.value = template.unknownTwo.value.copyOf()
        set.unknownThree.value = template.unknownThree.value
        set.file.value = record
        set.unknownFour.value = template.unknownFour.value.copyOf()
        set.archive.value = archiveBox.value
        csar.soundSets.add(set)
        return set
    }
}