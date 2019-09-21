package com.thane98.bcsarview.ui.forms

import com.thane98.bcsarview.core.enums.ConfigType
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.AudioConfig
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.core.utils.readAndConvertWav
import com.thane98.bcsarview.ui.interfaces.IUserAction
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.controlsfx.glyphfont.Glyph
import java.io.File
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class SoundSetEditorController(private val csar: Csar, private val soundSet: SoundSet) : AbstractFormController() {
    @FXML
    lateinit var stage: Stage
    @FXML
    lateinit var searchField: TextField
    @FXML
    lateinit var soundsList: ListView<AudioConfig>
    @FXML
    lateinit var addSoundButton: Button
    @FXML
    lateinit var removeSoundButton: Button
    @FXML
    lateinit var templateSoundBox: ComboBox<AudioConfig>
    @FXML
    lateinit var commitButton: Button

    private val chooser = createSoundChooser()
    private val changesList = FXCollections.observableArrayList<IUserAction>()
    private val items = FXCollections.observableArrayList(soundSet.sounds)
    private val filteredList = FilteredList(items)
    private val pathData = hashMapOf<AudioConfig, SimpleStringProperty>()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        soundSet.sounds.map { pathData[it] = SimpleStringProperty(it.file.value.toString()) }
        soundsList.items = filteredList
        soundsList.selectionModel.selectionMode = SelectionMode.MULTIPLE
        soundsList.setCellFactory {
            object : ListCell<AudioConfig>() {
                override fun updateItem(config: AudioConfig?, empty: Boolean) {
                    super.updateItem(config, empty)
                    if (empty || config == null)
                        graphic = null
                    else {
                        val display = GridPane()
                        display.alignment = Pos.CENTER_LEFT
                        display.hgap = 10.0
                        display.id = "soundSetEditorCell"
                        val label = Label(config.toString())
                        label.prefWidth = 250.0
                        val pathField = TextField()
                        pathField.textProperty().bind(pathData[item])
                        pathField.isEditable = false
                        pathField.prefWidth = 450.0
                        val openButton = Button(null, Glyph("FontAwesome", "FOLDER_OPEN"))
                        openButton.setOnAction { onOpenButtonPressed() }
                        display.children.addAll(label, pathField, openButton)
                        GridPane.setColumnIndex(pathField, 1)
                        GridPane.setColumnIndex(openButton, 2)
                        graphic = display
                    }
                }

                private fun onOpenButtonPressed() {
                    val result = chooser.showOpenDialog(stage)
                    if (result != null) {
                        val target = item
                        changesList.add(object : IUserAction {
                            override fun apply() {
                                csar.replaceSound(soundSet, target, readAndConvertWav(result.toPath()))
                            }
                        })
                        pathData[item]!!.value = result.name
                    }
                }
            }
        }
        searchField.textProperty().addListener { _ ->
                filteredList.setPredicate { it.toString().contains(searchField.text)
            }
        }

        templateSoundBox.items = csar.configs.filtered { it.configType == ConfigType.INTERNAL_SOUND }
        addSoundButton.disableProperty().bind(Bindings.isNull(templateSoundBox.valueProperty()))
        removeSoundButton.disableProperty().bind(Bindings.isEmpty(soundsList.selectionModel.selectedItems))
        commitButton.disableProperty().bind(Bindings.isEmpty(changesList))
    }

    @FXML
    private fun addSound() {
        val result = chooser.showOpenMultipleDialog(stage)
        if (result != null && result.isNotEmpty())
            addSounds(result)
    }

    private fun addSounds(files: List<File>) {
        performWithWaitingScreen {
            val newSounds = createSoundsFromWAVs(files).unzip()
            items.addAll(newSounds.first)
            for (i in 0 until files.size)
                pathData[newSounds.first[i]] = SimpleStringProperty(files[i].name)
            changesList.add(object : IUserAction {
                override fun apply() {
                    csar.addNewSoundsToSet(soundSet, newSounds.first, newSounds.second, templateSoundBox.value)
                }
            })
        }
    }

    private fun createSoundChooser(): FileChooser {
        val chooser = FileChooser()
        chooser.title = "Choose Sounds"
        chooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Audio File", "*.wav", "*.cwav"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        return chooser
    }

    private fun createSoundsFromWAVs(wavs: List<File>): List<Pair<AudioConfig, ByteArray>> {
        val template = templateSoundBox.value
        val result = mutableListOf<Pair<AudioConfig, ByteArray>>()
        for (wav in wavs) {
            val rawCwav = readAndConvertWav(wav.toPath())
            val config = AudioConfig()
            config.configType = ConfigType.INTERNAL_SOUND
            config.name.value = wav.nameWithoutExtension
            config.file.value = soundSet.file.value
            config.player.value = template.player.value
            config.unknown.value = template.unknown.value
            config.unknownTwo.value = template.unknownTwo.value.copyOf()
            config.unknownThree.value = template.unknownThree.value.copyOf()
            config.setIndexAddress = template.setIndexAddress
            config.setIndex = soundSet.sounds.size + result.size
            result.add(Pair(config, rawCwav))
        }
        return result
    }

    @FXML
    private fun removeSound() {
        for (item in soundsList.selectionModel.selectedItems)
            pathData.remove(item)
        items.removeAll(soundsList.selectionModel.selectedItems)
        changesList.add(object : IUserAction {
            override fun apply() {
                soundSet.archive.value.entryCount.value -= soundsList.selectionModel.selectedItems.size
                soundSet.sounds.removeAll(soundsList.selectionModel.selectedItems)
            }
        })
    }

    @FXML
    private fun commit() {
        performWithWaitingScreen {
            for (change in changesList)
                change.apply()
            Platform.runLater { stage.close() }
        }
    }

    @FXML
    private fun cancel() { stage.close() }
}