package com.thane98.bcsarview.core.enums

import java.lang.IllegalArgumentException

enum class ConfigType(val value: Int) {
    EXTERNAL_SOUND(0x2201),
    INTERNAL_SOUND(0x2202),
    SEQUENCE(0x2203);

    companion object {
        fun fromValue(value: Int): ConfigType {
            return when (value) {
                EXTERNAL_SOUND.value -> EXTERNAL_SOUND
                INTERNAL_SOUND.value -> INTERNAL_SOUND
                SEQUENCE.value -> SEQUENCE
                else -> throw IllegalArgumentException("Unrecognized sound config type: ${value.toString(16)}")
            }
        }
    }
}