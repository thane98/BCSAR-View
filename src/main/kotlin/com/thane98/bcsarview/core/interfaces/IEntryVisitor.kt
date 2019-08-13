package com.thane98.bcsarview.core.interfaces

import com.thane98.bcsarview.core.structs.entries.*

interface IEntryVisitor<T> {
    fun visitArchive(archive: Archive): T
    fun visitBank(bank: Bank): T
    fun visitBaseConfig(audioConfig: AudioConfig): T
    fun visitBaseSet(baseSet: BaseSet): T
    fun visitExternalFileReference(externalFileReference: ExternalFileReference): T
    fun visitInternalFileReference(internalFileReference: InternalFileReference): T
    fun visitPlayer(player: Player): T
    fun visitSoundGroup(soundGroup: SoundGroup): T
    fun visitSoundSet(soundSet: SoundSet): T
    fun visitSequenceSet(sequenceSet: SequenceSet): T
}