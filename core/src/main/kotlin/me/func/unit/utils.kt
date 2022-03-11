package me.func.unit

import dev.implario.bukkit.world.Orientation
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.Blocks
import net.minecraft.server.v1_12_R1.IBlockData
import org.bukkit.Location
import ru.cristalix.core.formatting.Color

fun Location.orientation(): Orientation {
    if (yaw < 0) yaw += 360f
    if (yaw >= -45 && yaw <= 45) return Orientation.PX
    if (yaw in 45.0..135.0) return Orientation.PY
    return if (yaw in 135.0..225.0) Orientation.MX else Orientation.MY
}

fun Location.position() = BlockPosition(x, y, z)

fun applyColor(data: IBlockData, color: Color?): IBlockData {
    val block = data.block
    if (color == null)
        return data
    // concrete и concrete_powder меняют цвет
    return if (block === Blocks.dR || block === Blocks.dS || block === Blocks.STAINED_GLASS) block.fromLegacyData(color.woolData) else data
}
