package me.func.unit

import dev.implario.bukkit.world.Box
import dev.implario.bukkit.world.V3
import dev.implario.games5e.sdk.cristalix.WorldMeta
import me.func.MetaWorld.storage
import me.func.core.AIR_DATA
import net.minecraft.server.v1_12_R1.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Color
import java.util.*

data class Building(
    val owner: UUID, // Владелец постройки
    val category: String, // Категория постройки (для поиска)
    val tag: String, // Тэг постройки для уточнения
    var relativeX: Double, // Относительные координаты для хранения
    var relativeY: Double,
    var relativeZ: Double,
    @Transient val box: Box? = null, // Коробка прототипа постройки
    @Transient var allocation: Allocation? = null, // Объект размещенный фантомной постройки
) {

    init {
        // Добавления этой постройки в storage пользователя
        storage[owner]?.buildings?.add(this)
    }

    // Удобный конструктор для создания постройки по миру и адресу
    constructor(owner: UUID, category: String, tag: String, world: WorldMeta) : this(
        owner,
        category,
        tag,
        0.0, 0.0, 0.0,
        world.getBox(category, tag)
    )

    // Стереть размещенную постройку из данного объекта (не убирает у игрока)
    fun deallocate() {
        allocation = null
    }

    // Приватный метод для отправки игрокам заготовленных пакетов удаления/создания постройки
    private fun changeState(
        players: Array<out Player>,
        get: (Allocation?) -> Collection<Packet<PacketListenerPlayOut>>?
    ) {
        if (allocation != null) {
            get(allocation)?.let {
                players.map { player -> it.forEach { (player as CraftPlayer).handle.playerConnection.sendPacket(it) } }
            }
        }
    }

    // Показать игрокам постройку
    fun show(vararg players: Player) = changeState(players) { it?.updatePackets }

    // Скрыть игрокам постройку
    fun hide(vararg players: Player) = changeState(players) { it?.removePackets }

    // Метод размещения постройки на указанных координатах (можно перекрасить блоки)
    fun allocate(origin: Location, color: Color? = null) {
        if (box == null) throw RuntimeException("$category/$tag allocation failed! No box assigned")

        val blocks = mutableMapOf<BlockPosition, IBlockData?>()
        val allocated = mutableListOf<Location>()
        val nmsWorld = (box.world as CraftWorld).handle
        val absoluteOrigin: V3 = V3.of(origin.x, origin.y, origin.z)

        relativeX = origin.x
        relativeY = origin.y
        relativeZ = origin.z

        var relativeOrigin: V3 = box.dimensions.multiply(0.5)
        relativeOrigin = relativeOrigin.withY(0.0)

        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        var maxZ = Int.MIN_VALUE

        val chunkMap = mutableMapOf<ChunkCoordIntPair, MutableList<BlockDataUnit>>()

        // Перебор коробки прототипа постройки поблочно
        for (x in box.min.x.toInt()..box.max.x.toInt()) {
            for (y in box.min.y.toInt()..box.max.y.toInt()) {
                for (z in box.min.z.toInt()..box.max.z.toInt()) {

                    // Получение смещенной локации
                    val dst = box.transpose(absoluteOrigin, origin.orientation(), relativeOrigin, x, y, z)
                    val source = Location(box.world, x.toDouble(), y.toDouble(), z.toDouble())

                    // Нам интересны все блоки, но не воздух
                    if (source.block.type == Material.AIR) continue
                    if (minX > dst.blockX) minX = dst.blockX
                    if (minY > dst.blockY) minY = dst.blockY
                    if (minZ > dst.blockZ) minZ = dst.blockZ
                    if (maxX < dst.blockX) maxX = dst.blockX
                    if (maxY < dst.blockY) maxY = dst.blockY
                    if (maxZ < dst.blockZ) maxZ = dst.blockZ

                    // Получаем тип блока и выносим в удобный объект
                    val blockPos = dst.position()
                    val chunkPos = ChunkCoordIntPair(blockPos)
                    val data = applyColor(nmsWorld.getType(source.position()), color)

                    // Вычисление отступов
                    val xOffset = blockPos.x - (blockPos.x shr 4) * 16
                    val yOffset = blockPos.y
                    val zOffset = blockPos.z - (blockPos.z shr 4) * 16

                    val offset =
                        (((xOffset and 0xF) shl 12) or ((yOffset and 0xFF) % 256) or ((zOffset and 0xFF) shl 8)).toShort()
                    val blockData = BlockDataUnit(offset, data)

                    // Запись блока в буффер
                    chunkMap.computeIfAbsent(chunkPos) { ArrayList() }.add(blockData)
                    allocated.add(dst)
                    blocks[blockPos] = data
                }
            }
        }
        // Создание списков с пакетами для создания/удаления постройки
        val updatePackets = mutableListOf<Packet<PacketListenerPlayOut>>()
        val removePackets = mutableListOf<Packet<PacketListenerPlayOut>>()

        // Перебор чанков для создания пакетов с чанками
        for (entry in chunkMap.entries) {
            val updatePacket = PacketPlayOutMultiBlockChange()
            val removePacket = PacketPlayOutMultiBlockChange()
            removePacket.a = entry.key
            updatePacket.a = removePacket.a
            val list: List<BlockDataUnit> = entry.value
            updatePacket.b = arrayOfNulls(list.size)
            removePacket.b = arrayOfNulls(list.size)
            list.indices.forEachIndexed { index, i ->
                val blockData = list[index]
                updatePacket.b[index] = updatePacket.MultiBlockChangeInfo(blockData.offset, blockData.data)
                removePacket.b[index] = removePacket.MultiBlockChangeInfo(blockData.offset, AIR_DATA)
            }
            updatePackets.add(updatePacket)
            removePackets.add(removePacket)
        }

        // Создание аллокации постройки и заготовка пакетов с данными
        allocation = Allocation(
            origin, blocks, updatePackets, removePackets, allocated,
            V3.of(minX.toDouble(), minY.toDouble(), minZ.toDouble()),
            V3.of(maxX.toDouble(), maxY.toDouble(), maxZ.toDouble())
        )
    }
}