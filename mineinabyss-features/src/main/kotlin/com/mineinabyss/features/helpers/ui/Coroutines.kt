package com.mineinabyss.features.helpers.ui

import kotlinx.coroutines.flow.SharingStarted

val SharingStarted = SharingStarted.Companion.WhileSubscribed(stopTimeoutMillis = 5000)
