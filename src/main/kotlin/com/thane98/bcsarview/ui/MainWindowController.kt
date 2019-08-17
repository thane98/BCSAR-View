package com.thane98.bcsarview.ui

import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.ui.forms.*
import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.createBcsarOpenDialog
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.net.URL
import java.nio.file.CopyOption
import java.nio.file.Files
import java.util.*

class MainWindowController: Initializable {
    @FXML
    private lateinit var editMenu: Menu
    @FXML
    private lateinit var saveMenuItem: MenuItem
    @FXML
    private lateinit var saveAsMenuItem: MenuItem
    @FXML
    private lateinit var closeMenuItem: MenuItem
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
        editMenu.disableProperty().bind(Bindings.isNull(csar))
        saveMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveAsMenuItem.disableProperty().bind(Bindings.isNull(csar))
        closeMenuItem.disableProperty().bind(Bindings.isNull(csar))
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
        val dialog = createBcsarOpenDialog()
        val selection = dialog.showOpenDialog(tabs.scene.window)
        if (selection != null)
            csar.value = Csar(selection.toPath())
    }

    @FXML
    private fun saveFile() {
        val oldPath = csar.value.path
        val tempPath = oldPath.resolveSibling("BCSARVIEW_TEMP_${oldPath.fileName}")
        Files.move(oldPath, tempPath)
        csar.value.path = tempPath
        csar.value.save(oldPath)
        csar.value.path = oldPath
        Files.delete(tempPath)
    }

    @FXML
    private fun saveFileAs() {
        val dialog = FileChooser()
        dialog.title = "Save Sound Archive"
        dialog.extensionFilters.addAll(
            FileChooser.ExtensionFilter("3DS Sound Archives", "*.bcsar"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )

        val selection = dialog.showSaveDialog(tabs.scene.window)
        if (selection != null)
            csar.value.save(selection.toPath())
    }

    @FXML
    private fun close() {
        csar.value = null
    }

    @FXML
    private fun quit() {
        Platform.exit()
    }

    @FXML
    private fun importArchive() {

    }

    @FXML
    private fun importExternalSound() {
        val loader = FXMLLoader()
        loader.setController(ImportExternalSoundsController(csar.value))
        val stage = loader.load<Stage>(this.javaClass.getResourceAsStream("Import.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }

    @FXML
    private fun addExternalSound() {
        val loader = FXMLLoader()
        loader.setController(AddExternalSoundController(csar.value))
        val stage = loader.load<Stage>(this.javaClass.getResourceAsStream("AddExternalSound.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }
}