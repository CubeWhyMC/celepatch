package org.cubewhy.patch.config

import kotlinx.serialization.Serializable

@Serializable
data class PatchConfig(
    val entrypoint: String, // the entrypoint
    val classpath: Array<String> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PatchConfig

        if (entrypoint != other.entrypoint) return false
        if (!classpath.contentEquals(other.classpath)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entrypoint.hashCode()
        result = 31 * result + classpath.contentHashCode()
        return result
    }
}
