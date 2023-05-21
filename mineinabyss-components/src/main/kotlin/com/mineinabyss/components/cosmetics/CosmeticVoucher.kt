package com.mineinabyss.components.cosmetics

import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("cosmetics:voucher")
class CosmeticVoucher(val permission: String, val originalItem: SerializableItemStack)
