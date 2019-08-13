package com.thane98.bcsarview.ui.forms

import com.aneagle.SpecialTextField
import com.thane98.bcsarview.core.structs.entries.SequenceSet
import com.thane98.bcsarview.ui.utils.ByteArrayStringConverter
import com.thane98.bcsarview.ui.utils.byteArrayToText
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import java.net.URL
import java.util.*

class SequenceSetController(private val set: SequenceSet): Initializable {
    @FXML
    private lateinit var unknownField: SpecialTextField
    @FXML
    private lateinit var unknownTwoField: ListView<Int>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        unknownField.forceSetText(byteArrayToText(set.unknown.value))
        unknownField.textProperty().bindBidirectional(set.unknown, ByteArrayStringConverter())
        unknownTwoField.items = set.unknownTwo
    }
}