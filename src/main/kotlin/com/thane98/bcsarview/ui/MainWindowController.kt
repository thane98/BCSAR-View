package com.thane98.bcsarview.ui

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.ui.forms.*
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import java.net.URL
import java.nio.channels.FileChannel
import java.util.*

class MainWindowController: Initializable {
    @FXML
    private lateinit var tabs: TabPane
    @FXML
    private lateinit var configsController: ConfigController
    @FXML
    private lateinit var setsController: SetController
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
        setsController.csar.bind(csar)
        archivesController.csar.bind(csar)
        csar.addListener { _ ->
            configsController.onFileChange(csar.value)
            setsController.onFileChange(csar.value)
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