package me.func.builder

import me.func.core.MutableChunk
import me.func.unit.Building
import java.util.*

class MetaSubscriber {

    // Модификаторы чанков
    private var modifiers = arrayListOf<(MutableChunk) -> MutableChunk>()

    // Удобный метод для настройки обработки списка построек
    fun buildingLoader(accepter: (UUID) -> List<Building>) = customModifier { chunk ->
        accepter(chunk.owner).asSequence()
            .filter {
                val allocation = it.allocation ?: return@filter false
                allocation.min.x.toInt() shr 4 <= chunk.chunk.locX &&
                        allocation.min.z.toInt() shr 4 <= chunk.chunk.locZ &&
                        allocation.max.x.toInt() shr 4 >= chunk.chunk.locX &&
                        allocation.max.z.toInt() shr 4 >= chunk.chunk.locZ
            }.filter { it.box != null && it.owner == chunk.owner }
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