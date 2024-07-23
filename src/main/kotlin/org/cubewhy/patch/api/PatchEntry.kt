package org.cubewhy.patch.api

interface PatchEntry {
    fun patchClass(name: String, klass: ByteArray): ByteArray
    fun patchFile(name: String, bytes: ByteArray?): ByteArray
}