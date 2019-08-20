package com.thane98.bcsarview.ui

import com.thane98.bcsarview.core.Configuration
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AbstractNamedEntry
import com.thane98.bcsarview.ui.forms.*
import com.thane98.bcsarview.ui.utils.applyStyles
import com.thane98.bcsarview.ui.utils.createBcsarOpenDialog
import com.thane98.bcsarview.ui.utils.createErrorDialog
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.lang.Exception
import java.net.URL
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.thread

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
        editMenu.disableProperty().bind(Bindings.isNull(csar))
        saveMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveButton.disableProperty().bind(Bindings.isNull(csar))
        saveAsMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveAsButton.disableProperty().bind(Bindings.isNull(csar))
        closeMenuItem.disableProperty().bind(Bindings.isNull(csar))
        closeButton.disableProperty().bind(Bindings.isNull(csar))
        configsController.csar.bind(csar)
        soundSetsController.csar.bind(csar)
        banksController.csar.bind(csar)
        archivesController.csar.bind(csar)
        groupsController.csar.bind(csar)
        controllers = listOf(
            configsController,
            soundSetsController,
            sequenceSetsController,
            banksController,
            archivesController,
            groupsController,
            playersController
        )
        csar.addListener { _ ->
            for (controller in controllers)
                controller.onFileChange(csar.value)
        }
    }

    @FXML
    private fun applyFilter() {
        for (controller in controllers)
            controller.applyFilter(searchField.text)
    }

    @FXML
    private fun openFile() {
        val dialog = createBcsarOpenDialog()
        val selection = dialog.showOpenDialog(tabs.scene.window)
        if (selection != null) {
            val task = object: Task<Unit>() {
                override fun call() {
                    val opened = Csar(selection.toPath())
                    Platform.runLater { csar.value = opened }
                }
            }
            Thread(task).run()
        }
    }

    @FXML
    private fun saveFile() {
        thread {
            val oldPath = csar.value.path
            val tempPath = oldPath.resolveSibling("BCSARVIEW_TEMP_${oldPath.fileName}")
            Files.move(oldPath, tempPath)
            csar.value.path = tempPath
            csar.value.save(oldPath)
            Files.delete(tempPath)
        }.run()
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
            thread { csar.value.save(selection.toPath()) }.run()
    }

    @FXML
    private fun close() {
        csar.value = null
    }

    @FXML
    private fun openPreferences() {
        val stage = FXMLLoader.load<Stage>(this.javaClass.getResource("Preferences.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }

    @FXML
    private fun quit() {
        Platform.exit()
    }

    @FXML
    private fun importArchive() {
        val loader = FXMLLoader()
        loader.setController(ImportArchiveController(csar.value))
        val stage = loader.load<Stage>(this.javaClass.getResourceAsStream("Import.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
    }

    @FXML
    private fun importSoundSet() {
        val loader = FXMLLoader()
        loader.setController(ImportSoundSetController(csar.value))
        val stage = loader.load<Stage>(this.javaClass.getResourceAsStream("Import.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
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

    @FXML
    private fun openMassRename() {
        val currentController = controllers[tabs.selectionModel.selectedIndex]
        val loader = FXMLLoader()
        loader.setController(MassRenameController(currentController.retrieveEntries()))
        val stage = loader.load<Stage>(this.javaClass.getResourceAsStream("MassRename.fxml"))
        applyStyles(stage.scene)
        stage.showAndWait()
        currentController.refresh()
    }
}