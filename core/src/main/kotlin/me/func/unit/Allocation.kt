package me.func.unit

import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.IBlockData
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut
import org.bukkit.Location
import ru.cristalix.core.math.V3

// Единица относительного блока с данными
internal class BlockDataUnit(
    val offset: Short,
    val data: IBlockData
)

// Форма размещенного фантомного построения
data class Allocation(
    val origin: Location? = null,
    val blocks: HashMap<BlockPosition, IBlockData?>? = null,
    val updatePackets: Collection<Packet<PacketListenerPlayOut>>? = null,
    val removePackets: Collection<Packet<PacketListenerPlayOut>>? = null,
    val allocatedBlocks: List<Location>? = null,
    val min: V3,
    val max: V3
)