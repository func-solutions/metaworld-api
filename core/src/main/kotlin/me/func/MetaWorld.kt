package me.func

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import me.func.core.ChunkInjector
import me.func.core.ChunkModifyManager
import me.func.core.MutableChunk
import me.func.player.PlayerBuildings
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig
import net.minecraft.server.v1_12_R1.PacketPlayInUseItem
import net.minecraft.server.v1_12_R1.World
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.ArrayList

object MetaWorld : Listener {

    // Получение объекта главного класса плагина
    val provided: JavaPlugin = JavaPlugin.getProvidingPlugin(this.javaClass)

    // Обработчики чанков при регистрации
    var chunkContributer = arrayListOf<(MutableChunk) -> MutableChunk>()

    // UUID - список построек, не для пользователя - разработчика
    val storage = hashMapOf<UUID, PlayerBuildings>()

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
        chunkContributer = ArrayList(accepter.toList())
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        // Включение в pipeline чанков игрока модификаторов по умолчанию
        ChunkModifyManager.subscribe(player.uniqueId, *chunkContributer.toTypedArray())

        // Добавление в pipeline пакетов игрока обработку ломания/нажатия постройки
        (player as CraftPlayer).handle.playerConnection.networkManager.channel.pipeline()
            .addBefore("packet_handler", "meta-" + player.name, object : ChannelDuplexHandler() {
                override fun channelRead(context: ChannelHandlerContext, data: Any) {
                    if (data is PacketPlayInUseItem) {
                        // При нажатии игрока на блок постройки
                        filterPlayerStorage(player, data.a.x, data.a.y, data.a.z)?.map { it.onClick(it, player, data) }
                            ?.apply { data.a = BlockPosition.ZERO }
                    } else if (data is PacketPlayInBlockDig) {
                        // При ломании блока постройки
                        filterPlayerStorage(player, data.a.x, data.a.y, data.a.z)?.forEach { it.onBreak(it, player, data) }
                    }
                    // Кинуть пакет дальше в pipeline
                    super.channelRead(context, data)
                }
            })
    }

    // При выходе игрока - стираем его storage
    @EventHandler
    fun PlayerQuitEvent.handle() = storage.remove(player.uniqueId)

    // Метод для удобного получения построек содержащих локацию
    private fun filterPlayerStorage(player: Player, x: Number, y: Number, z: Number) =
        storage[player.uniqueId]?.buildings?.filter {
            it.isInside(x.toDouble(), y.toDouble(), z.toDouble())
        }
}