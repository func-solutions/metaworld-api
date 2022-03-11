package me.func.player

import me.func.unit.Building
import java.util.*

data class PlayerBuildings(val uuid: UUID, val buildings: MutableList<Building>)