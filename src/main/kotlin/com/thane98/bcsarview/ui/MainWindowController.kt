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
import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MainWindowController : AbstractFormController() {
    @FXML
    private lateinit var themeToggleGroup: ToggleGroup
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
    private lateinit var showToolBarItem: CheckMenuItem
    @FXML
    private lateinit var showStatusBarItem: CheckMenuItem
    @FXML
    private lateinit var tabs: TabPane
    @FXML
    private lateinit var toolBar: ToolBar
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
    @FXML
    private lateinit var statusBar: HBox
    @FXML
    private lateinit var statusText: Label

    private val statusTimer = Executors.newSingleThreadScheduledExecutor()
    private var statusTimerTask: Future<*>? = null

    private val csar = SimpleObjectProperty<Csar>()
    private lateinit var controllers: List<AbstractEntryController<out AbstractNamedEntry>>

    companion object {
        val statusLine = SimpleStringProperty()
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        setupListeners()
        setupControlBindingsAndConfigurations()
        setupThemeMenu()
        setupControllers()
    }

    fun shutdown() { statusTimer.shutdownNow() }

    private fun setupListeners() {
        Configuration.theme.addListener { _ -> applyStyles(tabs.scene) }
        statusLine.addListener { _ ->
            if (statusLine.value != "") {
                statusTimerTask?.cancel(true)
                val task = Runnable { Platform.runLater { statusLine.value = "" } }
                statusTimerTask = statusTimer.schedule(task, 5, TimeUnit.SECONDS)
            }
        }
    }

    private fun setupControlBindingsAndConfigurations() {
        HBox.setHgrow(toolBarSpacer, Priority.ALWAYS)
        editMenu.disableProperty().bind(Bindings.isNull(csar))
        createMenu.disableProperty().bind(Bindings.isNull(csar))
        saveMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveButton.disableProperty().bind(Bindings.isNull(csar))
        saveAsMenuItem.disableProperty().bind(Bindings.isNull(csar))
        saveAsButton.disableProperty().bind(Bindings.isNull(csar))
        closeMenuItem.disableProperty().bind(Bindings.isNull(csar))
        closeButton.disableProperty().bind(Bindings.isNull(csar))
        statusText.textProperty().bind(statusLine)
        statusBar.visibleProperty().bind(Configuration.showStatusBar)
        statusBar.managedProperty().bind(statusBar.visibleProperty())
        toolBar.visibleProperty().bind(Configuration.showToolBar)
        toolBar.managedProperty().bind(toolBar.visibleProperty())
        showStatusBarItem.selectedProperty().bindBidirectional(Configuration.showStatusBar)
        showToolBarItem.selectedProperty().bindBidirectional(Configuration.showToolBar)
    }

    private fun setupThemeMenu() {
        if (Configuration.theme.value == "Light")
            themeToggleGroup.selectToggle(themeToggleGroup.toggles[0])
        else
            themeToggleGroup.selectToggle(themeToggleGroup.toggles[1])
        themeToggleGroup.selectedToggleProperty().addListener { _ ->
            if (themeToggleGroup.selectedToggle != null) {
                val item = themeToggleGroup.selectedToggle as RadioMenuItem
                Configuration.theme.value = item.text
            }
        }
    }

    private fun setupControllers() {
        controllers = listOf(
            configsController,
            soundSetsController,
            sequenceSetsController,
            banksController,
            archivesController,
            groupsController,
            playersController
        )
        for (controller in controllers) {
            controller.parentForm = this
            controller.csar.bind(csar)
        }
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
            performWithWaitingScreen {
                val opened = Csar(selection.toPath())
                Platform.runLater {
                    csar.value = opened
                    statusLine.value = "Opened ${selection.name}."
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
        if (selection != null)
            saveOnDifferentThread(selection.toPath())
    }

    private fun saveOnDifferentThread(path: Path) {
        val originalName = csar.value.path.fileName
        performWithWaitingScreen {
            csar.value.save(path)
            Platform.runLater { statusLine.value = "Saved $originalName to ${path.fileName}." }
        }
    }

    @FXML
    private fun close() {
        val name = csar.value.path.fileName
        csar.value = null
        statusLine.value = "Closed $name."
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