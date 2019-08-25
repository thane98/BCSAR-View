package com.thane98.bcsarview.ui

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.ui.forms.*
import com.thane98.bcsarview.ui.utils.Dialogs
import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.loadAndShowForm
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.thread

class MainWindowController : Initializable {
    @FXML
    private lateinit var waitingEffect: Region
    @FXML
    private lateinit var waitingIndicator: ProgressIndicator
    @FXML
    private lateinit var editMenu: Menu
    @FXML
    private lateinit var createMenu: Menu
    @FXML
    private lateinit var saveMenuItem: MenuItem
    @FXML
    private lateinit var saveAsMenuItem: MenuItem
    @FXML
    private lateinit var closeMenuItem: MenuItem
    @FXML
    private lateinit var tabs: TabPane
    @FXML
    private lateinit var saveButton: Button
    @FXML
    private lateinit var saveAsButton: Button
    @FXML
    private lateinit var closeButton: Button
    @FXML
    private lateinit var toolBarSpacer: Pane
    @FXML
    private lateinit var searchField: TextField
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
    private lateinit var controllers: List<AbstractEntryController<out AbstractNamedEntry>>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        Configuration.theme.addListener { _ -> applyStyles(tabs.scene) }
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS)
        waitingEffect.visibleProperty().bind(waitingIndicator.visibleProperty())
        editMenu.disableProperty().bind(Bindings.isNull(csar))
        createMenu.disableProperty().bind(Bindings.isNull(csar))
        saveMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveButton.disableProperty().bind(Bindings.isNull(csar))
        saveAsMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveAsButton.disableProperty().bind(Bindings.isNull(csar))
        closeMenuItem.disableProperty().bind(Bindings.isNull(csar))
        closeButton.disableProperty().bind(Bindings.isNull(csar))
        controllers = listOf(
            configsController,
            soundSetsController,
            sequenceSetsController,
            banksController,
            archivesController,
            groupsController,
            playersController
        )
        for (controller in controllers)
            controller.csar.bind(csar)
    }

    @FXML
    private fun applyFilter() {
        for (controller in controllers)
            controller.applyFilter(searchField.text)
    }

    @FXML
    private fun openFile() {
        val dialog = Dialogs.bcsarChooser
        val selection = dialog.showOpenDialog(tabs.scene.window)
        if (selection != null) {
            dialog.initialDirectory = selection.parentFile
            waitingIndicator.isVisible = true
            thread {
                val opened = Csar(selection.toPath())
                Platform.runLater {
                    csar.value = opened
                    waitingIndicator.isVisible = false
                }
            }
        }
    }

    @FXML
    private fun saveFile() {
        saveOnDifferentThread(csar.value.path)
    }

    @FXML
    private fun saveFileAs() {
        val dialog = Dialogs.bcsarChooser
        val selection = dialog.showSaveDialog(tabs.scene.window)
        if (selection != null) {
            dialog.initialDirectory = selection.parentFile
            saveOnDifferentThread(selection.toPath())
        }
    }

    private fun saveOnDifferentThread(path: Path) {
        waitingIndicator.isVisible = true
        thread {
            try {
                csar.value.save(path)
            } finally {
                Platform.runLater { waitingIndicator.isVisible = false }
            }
        }
    }

    @FXML
    private fun close() {
        csar.value = null
    }

    @FXML
    private fun openPreferences() {
        loadAndShowForm("Preferences.fxml")
    }

    @FXML
    private fun quit() {
        Platform.exit()
    }

    @FXML
    private fun importArchive() {
        loadAndShowForm("Import.fxml", ImportArchiveController(csar.value))
    }

    @FXML
    private fun importSoundSet() {
        loadAndShowForm("Import.fxml", ImportSoundSetController(csar.value))
    }

    @FXML
    private fun importExternalSound() {
        loadAndShowForm("Import.fxml", ImportExternalSoundsController(csar.value))
    }

    @FXML
    private fun openMassRename() {
        val currentController = controllers[tabs.selectionModel.selectedIndex]
        loadAndShowForm("MassRename.fxml", MassRenameController(currentController.retrieveEntries()))
        currentController.refresh()
    }

    @FXML
    private fun openAbout() {
        loadAndShowForm("About.fxml")
    }

    @FXML
    private fun openCreateExternalSound() {
        loadAndShowForm("CreateExternalSound.fxml", CreateExternalSoundController(csar.value))
    }

    @FXML
    private fun openCreatePlayer() {
        loadAndShowForm("CreatePlayer.fxml", CreatePlayerController(csar.value))
    }

    @FXML
    private fun openCreateSoundSet() {
        loadAndShowForm("CreateSoundSet.fxml", CreateSoundSetController(csar.value))
    }

    @FXML
    private fun openCreateArchive() {
        loadAndShowForm("CreateArchive.fxml", CreateArchiveController(csar.value))
    }
}