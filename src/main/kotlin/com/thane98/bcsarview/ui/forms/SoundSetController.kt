package com.thane98.bcsarview.ui.forms

import com.aneagle.SpecialTextField
import com.thane98.bcsarview.core.structs.Csar
import com.thane98.bcsarview.core.structs.entries.SoundSet
import com.thane98.bcsarview.ui.utils.ByteArrayStringConverter
import com.thane98.bcsarview.ui.utils.byteArrayToText
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.util.converter.NumberStringConverter
import java.net.URL
import java.util.*

class SoundSetController(private val csar: Csar, private val set: SoundSet): Initializable {
    @FXML
    private lateinit var unknownField: SpecialTextField
    @FXML
    private lateinit var unknownTwoField: TextField
    @FXML
    private lateinit var unknownThreeField: SpecialTextField
    @FXML
    private lateinit var archiveField: TextField

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        unknownField.forceSetText(byteArrayToText(set.unknown.value))
        unknownField.textProperty().bindBidirectional(set.unknown, ByteArrayStringConverter())
        unknownTwoField.textProperty().bindBidirectional(set.unknownTwo, NumberStringConverter())
        unknownThreeField.forceSetText(byteArrayToText(set.unknownThree.value))
        unknownThreeField.textProperty().bindBidirectional(set.unknownThree, ByteArrayStringConverter())
        archiveField.text = csar.archives[set.archiveIndex.value].toString()
    }
}