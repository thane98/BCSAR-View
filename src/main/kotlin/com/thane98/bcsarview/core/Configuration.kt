package com.thane98.bcsarview.core

import com.thane98.bcsarview.ui.Main
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.lang.Exception
import java.util.logging.Level
import java.util.prefs.Preferences

object Configuration {
    val theme = SimpleStringProperty()
    val loggingLevel = SimpleObjectProperty<Level>()

    init {
        val preferences = Preferences.userNodeForPackage(Main::class.java)
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
    }
}