package me.func

import me.func.core.ChunkInjector
import me.func.core.ChunkModifyManager
import me.func.core.MutableChunk
import me.func.player.PlayerBuildings
import net.minecraft.server.v1_12_R1.World
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object MetaWorld : Listener {

    // Получение объекта главного класса плагина
    val provided: JavaPlugin = JavaPlugin.getProvidingPlugin(this.javaClass)

    // Обработчики чанков при регистрации
    var chunkContributer = mutableListOf<(MutableChunk) -> MutableChunk>()

    // UUID - список построек, не для пользователя - разработчика
    val storage = mutableMapOf<UUID, PlayerBuildings>()

    // Определить миры как Meta World
    @JvmStatic
    fun append(vararg world: World) = world.forEach { ChunkInjector.inject(it) }

    // Сделать миры общими (противоположность #append)
    @JvmStatic
    fun clear(vararg world: World) = world.forEach { it.chunkInterceptor = null }

    // Удобный метод для добавления мира регистрации модификаторов
    @JvmStatic
    fun universe(world: org.bukkit.World, vararg accepter: (MutableChunk) -> MutableChunk) {
        append((world as CraftWorld).handle)
        registerModifiers(*accepter)
    }

    // Регистрация модификаторов чанков
    @JvmStatic
    fun registerModifiers(vararg accepter: (MutableChunk) -> MutableChunk) {
        Bukkit.getPluginManager().registerEvents(this, provided)
        chunkContributer = accepter.toMutableList()
    }

    // Включение в pipeline чанков игрока модификаторов по умолчанию
    @EventHandler
    fun PlayerJoinEvent.handle() = ChunkModifyManager.subscribe(player.uniqueId, *chunkContributer.toTypedArray())

    // При выходе игрока - стираем его storage
    @EventHandler
    fun PlayerQuitEvent.handle() = storage.remove(player.uniqueId)
}