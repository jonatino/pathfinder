package org.rsmod.pathfinder

internal sealed class CardinalDirection(val offX: Int, val offY: Int) {
    object North : CardinalDirection(0, 1)
    object South : CardinalDirection(0, -1)
    object East : CardinalDirection(1, 0)
    object West : CardinalDirection(-1, 0)
}
