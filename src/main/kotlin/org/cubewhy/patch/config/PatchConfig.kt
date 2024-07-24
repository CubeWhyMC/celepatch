package org.cubewhy.patch.config

import kotlinx.serialization.Serializable

@Serializable
data class PatchConfig(
    val entrypoint: String // the entrypoint
)
