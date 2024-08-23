package com.mineinabyss.features.guidebook

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

@Serializable
data class GuideBookPages(val pages: List<GuideBookPage> = listOf()) {

    @Transient private val ids = pages.map { it.id }

    class Serializer : InnerSerializer<Map<String, GuideBookPage>, GuideBookPages>(
        "mineinabyss:guidepages",
        MapSerializer(String.serializer(), GuideBookPage.serializer()),
        { GuideBookPages(it.map { it.value.copy(id = it.key) }) },
        { it.pages.associateBy { it.id } }
    )
}

@Serializable
data class GuideBookPage(
    // This is blank by default to avoid marking it as null
    // The Serializer in PackyTemplates will always ensure the id is properly set
    @Transient val id: String = "",
    val title: String,
    val buttons: List<SerializableItemStack>
)
