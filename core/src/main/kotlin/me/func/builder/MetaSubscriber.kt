package me.func.builder

import me.func.core.MutableChunk
import me.func.unit.Building
import java.util.*

class MetaSubscriber {

    // Модификаторы чанков
    private var modifiers = arrayListOf<(MutableChunk) -> MutableChunk>()

    // Удобный метод для настройки обработки списка построек
    fun buildingLoader(accepter: (UUID) -> List<Building>) = customModifier { chunk ->
        accepter(chunk.owner)
            .filter { it.box != null }
            .mapNotNull { it.allocation }
            .forEach {
                it.blocks
                    ?.filterKeys { key -> key.x shr 4 == chunk.chunk.locX && key.z shr 4 == chunk.chunk.locZ }
                    ?.forEach { (key, value) -> chunk.modify(key, value) }
            }
        chunk
    }

    // Кастомный модификатор для разных нужд
    fun customModifier(additional: (MutableChunk) -> MutableChunk): MetaSubscriber {
        modifiers.add(additional)
        return this
    }

    // Получить список модификаторов
    fun build() = modifiers.toTypedArray()

}