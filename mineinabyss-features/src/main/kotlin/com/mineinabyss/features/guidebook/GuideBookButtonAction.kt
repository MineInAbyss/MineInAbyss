package com.mineinabyss.features.guidebook

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class GuideBookButtonAction(
    val action: Action,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val subPage: String? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val command: String? = null,
) {

    init {
        require(action != Action.OPEN_PAGE || subPage != null) { "Action is set to OPEN_PAGE, but no sugPage is specified" }
        require(action != Action.RUN_COMMAND || !command.isNullOrEmpty()) { "Action is set to RUN_COMMAND, but no command is specified" }
    }

    enum class Action {
        OPEN_PAGE, RUN_COMMAND
    }
}