package me.func.core

import me.func.core.ChunkModifyManager.modifier
import net.minecraft.server.v1_12_R1.Chunk
import net.minecraft.server.v1_12_R1.EntityPlayer
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk
import net.minecraft.server.v1_12_R1.World
import ru.cristalix.ChunkInterceptor

// Утилита встраивающая в мир модификатор чанков под конкретного игрока
object ChunkInjector {

    fun inject(world: World) {
        // Inject в мир, доступно только на Dark Paper
        world.chunkInterceptor = ChunkInterceptor { chunk: Chunk, flags: Int, receiver: EntityPlayer? ->
            // Если игрока нет, то просто вернуть чанк
            val player = receiver ?: return@ChunkInterceptor PacketPlayOutMapChunk(chunk, flags)

            // Если для данного игрока нет модификаторов чанков, то просто вернуть чанк
            val loop = modifier(player.uniqueID) ?: return@ChunkInterceptor PacketPlayOutMapChunk(chunk, flags)

            // Создаем мутабельный чанк и применяем модификаторы игрока
            MutableChunk(player.uniqueID, world.getChunkAt(chunk.locX, chunk.locZ))
                .apply { loop.forEach { it(this) } }.build(flags)
        }
    }
}