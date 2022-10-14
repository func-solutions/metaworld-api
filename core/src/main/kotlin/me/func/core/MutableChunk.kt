package me.func.core

import net.minecraft.server.v1_12_R1.*
import java.util.*

val AIR_DATA: IBlockData = Block.getById(0).getBlockData()

// Класс Unit чанка, которой хранит сам чанк, пакет для отправки и список измененных блоков
class MutableChunk(val owner: UUID, val chunk: Chunk) {
    private val modified: MutableList<BlockPosition> = arrayListOf()
    private var readyPacket: PacketPlayOutMapChunk? = null

    // Метод для изменения блока в чанке
    fun modify(position: BlockPosition, blockData: IBlockData?): MutableChunk {
        chunk.a(position, blockData)
        modified.add(position)
        return this
    }

    // Получение пакета чанка для отправки
    fun build(flags: Int) =
        (readyPacket ?: PacketPlayOutMapChunk(chunk, flags)).apply {
            modified.forEach { chunk.a(it, AIR_DATA) }
            readyPacket = this
        }
}