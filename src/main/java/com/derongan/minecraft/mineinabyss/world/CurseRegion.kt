package com.derongan.minecraft.mineinabyss.world

import com.derongan.minecraft.deeperworld.world.Region
import kotlinx.serialization.Serializable

@Serializable
class CurseRegion(val region: Region, val priority: Int, val strength: Double)