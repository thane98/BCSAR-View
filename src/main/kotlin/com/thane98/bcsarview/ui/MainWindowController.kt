package com.thane98.bcsarview.ui

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.ui.forms.*
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.net.URL
import java.util.*

class MainWindowController: Initializable {
    @FXML
    private lateinit var tabs: TabPane
    @FXML
    private lateinit var configsController: ConfigController
    @FXML
    private lateinit var soundSetsController: SoundSetController
    @FXML
    private lateinit var sequenceSetsController: SequenceSetController
    @FXML
    private lateinit var banksController: BankController
    @FXML
    private lateinit var archivesController: ArchiveController
    @FXML
    private lateinit var groupsController: GroupController
    @FXML
    private lateinit var playersController: PlayerController

    private val csar = SimpleObjectProperty<Csar>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        configsController.csar.bind(csar)
        soundSetsController.csar.bind(csar)
        banksController.csar.bind(csar)
        archivesController.csar.bind(csar)
        groupsController.csar.bind(csar)
        csar.addListener { _ ->
            soundSetsController.onFileChange(csar.value)
            sequenceSetsController.onFileChange(csar.value)
            configsController.onFileChange(csar.value)
            banksController.onFileChange(csar.value)
            archivesController.onFileChange(csar.value)
            groupsController.onFileChange(csar.value)
            playersController.onFileChange(csar.value)
        }
    }

    @FXML
    private fun openFile() {
        val dialog = FileChooser()
        dialog.title = "Open Sound Archive"
        dialog.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Archives", "*.bcsar"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )

        val selection = dialog.showOpenDialog(tabs.scene.window)
        if (selection != null)
            csar.value = Csar(selection.toPath())
    }
}