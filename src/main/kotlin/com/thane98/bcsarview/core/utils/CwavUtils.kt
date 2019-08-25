package com.thane98.bcsarview.core.utils

import com.thane98.bcsarview.core.Configuration
import org.apache.commons.text.StringSubstitutor
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class FailedConversionException(command: String): Exception(command)

fun dumpCwavToWav(wavPath: Path, rawCwav: ByteArray) {
    val commandTemplate = Configuration.cwavToWavCommand.value
    if (commandTemplate == null || commandTemplate.isEmpty())
        throw IllegalStateException("Cannot convert cwav to wav without a valid conversion command.")

    val tempFilePath = File.createTempFile("bcsarview_wav_to_cwav_conv_step", ".cwav").toPath()
    try {
        Files.write(tempFilePath, rawCwav)
        val command = createConversionCommand(commandTemplate, tempFilePath, wavPath)
        executeConversionCommand(command)
    } finally {
        Files.delete(tempFilePath)
    }
}

private fun createConversionCommand(template: String, cwavPath: Path, wavPath: Path): String {
    val argMap = mapOf(
        "cwavPath" to cwavPath.toString(),
        "wavPath" to wavPath.toString()
    )
    return StringSubstitutor(argMap).replace(template)
}

private fun executeConversionCommand(command: String) {
    val process = Runtime.getRuntime().exec(command)
    process.waitFor(10, TimeUnit.SECONDS)
    val failed = process.exitValue() != 0 || process.isAlive
    if (process.isAlive)
        process.destroy()
    if (failed)
        throw FailedConversionException(command)
}

fun readAndConvertWav(source: Path): ByteArray {
    val file = source.toFile()
    return if (file.extension == ".cwav")
        Files.readAllBytes(source)
    else
        convertWavToCwav(source)
}

private fun convertWavToCwav(source: Path): ByteArray {
    val commandTemplate = Configuration.wavToCwavCommand.value
    if (commandTemplate == null || commandTemplate.isEmpty())
        throw IllegalArgumentException("Cannot convert wav to cwav without a conversion command.")

    val tempFilePath = File.createTempFile("bcsarview_wav_to_cwav_conv_step", ".cwav").toPath()
    try {
        val command = createConversionCommand(commandTemplate, tempFilePath, source)
        executeConversionCommand(command)
        return Files.readAllBytes(tempFilePath)
    } finally {
        Files.delete(tempFilePath)
    }
}