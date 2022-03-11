package me.func.core

import me.func.MetaWorld.storage
import me.func.player.PlayerBuildings
import org.bukkit.entity.Player
import java.util.*

// Утилита хранящая модификаторы чанков игроков
object ChunkModifyManager {

    private val players: MutableMap<UUID, List<(MutableChunk) -> MutableChunk>> = mutableMapOf()

    // Добавить модификатор для игрока
    fun subscribe(player: Player, action: (MutableChunk) -> MutableChunk) = subscribe(player.uniqueId, action)

    fun subscribe(uuid: UUID, vararg action: (MutableChunk) -> MutableChunk) {
        players[uuid] = action.toList()
        storage[uuid] = PlayerBuildings(uuid, mutableListOf())
    }

    // Убрать модификатор игрока
    fun unsubscribe(player: Player) = unsubscribe(player.uniqueId)

    fun unsubscribe(uuid: UUID) {
        players.remove(uuid)
        storage.remove(uuid)
    }

    // Получить модификатор игрока
    fun modifier(uuid: UUID) = players[uuid]

    fun modifier(player: Player) = modifier(player.uniqueId)

}