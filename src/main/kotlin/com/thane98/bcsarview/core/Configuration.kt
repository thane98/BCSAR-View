package com.thane98.bcsarview.core

import com.thane98.bcsarview.ui.Main
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.Exception
import java.util.logging.Level
import java.util.prefs.Preferences

object Configuration {
    val theme = SimpleStringProperty()
    val loggingLevel = SimpleObjectProperty<Level>()
    val cwavToWavCommand = SimpleStringProperty()
    val wavToCwavCommand = SimpleStringProperty()
    val showToolBar = SimpleBooleanProperty()
    val showStatusBar = SimpleBooleanProperty()

    init {
        val preferences = Preferences.userNodeForPackage(Main::class.java)
        cwavToWavCommand.value = preferences.get("cwavToWavCommand", null)
        wavToCwavCommand.value = preferences.get("wavToCwavCommand", null)
        showToolBar.value = preferences.getBoolean("showToolBar", true)
        showStatusBar.value = preferences.getBoolean("showStatusBar", true)
        theme.value = preferences.get("theme", "Light")
        if (theme.value != "Light" && theme.value != "Dark")
            theme.value = "Light"
        try {
            loggingLevel.value = Level.parse(preferences.get("loggingLevel", "INFO"))
        } catch (ex: Exception) {
            loggingLevel.value = Level.INFO
        }
    }

    fun save() {
        val preferences = Preferences.userNodeForPackage(Main::class.java)
        preferences.put("theme", theme.value)
        preferences.put("loggingLevel", loggingLevel.value.name)
        preferences.put("cwavToWavCommand", cwavToWavCommand.value)
        preferences.put("wavToCwavCommand", wavToCwavCommand.value)
        preferences.putBoolean("showToolBar", showToolBar.value)
        preferences.putBoolean("showStatusBar", showStatusBar.value)
        preferences.flush()
    }
}