package com.thane98.bcsarview.core

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import java.io.*
import java.util.*
import java.util.logging.Level

object Configuration {
    private val propertiesPath = File(System.getProperty("user.dir") + "/bcsar-view.xml")
    val theme = SimpleStringProperty()
    val loggingLevel = SimpleObjectProperty<Level>()
    val cwavToWavCommand = SimpleStringProperty()
    val wavToCwavCommand = SimpleStringProperty()
    val showToolBar = SimpleBooleanProperty()
    val showStatusBar = SimpleBooleanProperty()

    init {
        val properties = Properties()
        readPropertiesFile(properties)
        cwavToWavCommand.value = properties.getProperty("cwavToWavCommand", "")
        wavToCwavCommand.value = properties.getProperty("wavToCwavCommand", "")
        showToolBar.value = properties.getBoolean("showToolBar", true)
        showStatusBar.value = properties.getBoolean("showStatusBar", true)
        theme.value = properties.getProperty("theme", "Light")
        if (theme.value != "Light" && theme.value != "Dark")
            theme.value = "Light"
        try {
            loggingLevel.value = Level.parse(properties.getProperty("loggingLevel", "INFO"))
        } catch (ex: Exception) {
            loggingLevel.value = Level.INFO
        }
    }

    private fun readPropertiesFile(destination: Properties) {
        if (propertiesPath.exists()) {
            val propertiesStream = BufferedInputStream(FileInputStream(propertiesPath))
            propertiesStream.use {
                destination.loadFromXML(propertiesStream)
            }
        }
    }

    private fun Properties.getBoolean(key: String, default: Boolean): Boolean {
        return try {
            this.getProperty(key)!!.toBoolean()
        } catch (ex: Exception) {
            default
        }
    }

    fun save() {
        val properties = Properties()
        properties.setProperty("theme", theme.value)
        properties.setProperty("loggingLevel", loggingLevel.value.name)
        properties.setProperty("cwavToWavCommand", cwavToWavCommand.value)
        properties.setProperty("wavToCwavCommand", wavToCwavCommand.value)
        properties.setProperty("showToolBar", showToolBar.value.toString())
        properties.setProperty("showStatusBar", showStatusBar.value.toString())
        properties.storeToXML(BufferedOutputStream(FileOutputStream(propertiesPath)), null)
    }
}