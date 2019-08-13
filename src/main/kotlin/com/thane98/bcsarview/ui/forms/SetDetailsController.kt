package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.BaseSet
import com.thane98.bcsarview.core.structs.entries.SequenceSet
import com.thane98.bcsarview.core.structs.entries.SoundSet
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*

class SetDetailsController(private val csar: Csar): Initializable {
    @FXML
    private lateinit var detailsContainer: VBox
    @FXML
    private lateinit var associatedSoundsList: ListView<AudioConfig>

    private var subentryForm: Node? = null

    val currentSet = SimpleObjectProperty<BaseSet>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        currentSet.addListener { _ ->
            refreshDetails()
        }
    }

    private fun refreshDetails() {
        associatedSoundsList.items.clear()
        val set = currentSet.value
        for (i in set.soundStartIndex.value until set.soundEndIndex.value) {
            associatedSoundsList.items.add(csar.configs[i])
        }

        val loader = FXMLLoader()
        if (subentryForm != null)
            detailsContainer.children.remove(subentryForm)
        if (set.subEntry.value is SoundSet) {
            loader.setController(SoundSetController(csar, set.subEntry.value as SoundSet))
            subentryForm = loader.load(this.javaClass.getResourceAsStream("SoundSet.fxml"))
        } else if (set.subEntry.value is SequenceSet) {
            loader.setController(SequenceSetController(set.subEntry.value as SequenceSet))
            subentryForm = loader.load(this.javaClass.getResourceAsStream("SequenceSet.fxml"))
        }
        detailsContainer.children.add(subentryForm!!)
    }
}