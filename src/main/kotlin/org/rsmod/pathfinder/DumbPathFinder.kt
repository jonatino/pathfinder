package org.rsmod.pathfinder

import org.rsmod.pathfinder.bound.reachRectangle
import org.rsmod.pathfinder.flag.CollisionFlag

private sealed class Direction
private object South : Direction()
private object North : Direction()
private object West : Direction()
private object East : Direction()
private object NorthEast : Direction()
private object SouthEast : Direction()
private object NorthWest : Direction()
private object SouthWest : Direction()

public class DumbPathFinder(public val searchMapSize: Int = 128) {

    public fun findPath(
        clipFlags: IntArray,
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
        srcSize: Int = 1,
        destWidth: Int = 0,
        destHeight: Int = 0
    ): Route {
        val baseX = srcX - (searchMapSize / 2)
        val baseY = srcY - (searchMapSize / 2)
        val localSrcX = srcX - baseX
        val localSrcY = srcY - baseY
        val localDestX = destX - baseX
        val localDestY = destY - baseY
        var x = localSrcX
        var y = localSrcY
        val coords = mutableListOf<RouteCoordinates>()
        var success = false
        for (i in 0 until searchMapSize * searchMapSize) {
            if (reachRectangle(
                    clipFlags,
                    searchMapSize,
                    0,
                    srcX,
                    srcY,
                    localDestX,
                    localDestY,
                    srcSize,
                    destWidth,
                    destHeight
                )
            ) {
                success = true
                break
            }
            val startX = x
            val startY = y
            val dir = getDirection(x, y, localDestX, localDestY) ?: break
            val blocked = dir.isBlocked(clipFlags, x, y, srcSize)
            if (dir == South && !blocked) {
                y--
            } else if (dir == North && !blocked) {
                y++
            } else if (dir == West && !blocked) {
                x--
            } else if (dir == East && !blocked) {
                x++
            } else if (dir == SouthWest) {
                if (blocked) {
                    if (!South.isBlocked(clipFlags, x, y, srcSize)) {
                        y--
                    } else if (!West.isBlocked(clipFlags, x, y, srcSize)) {
                        x--
                    }
                } else {
                    x--
                    y--
                }
            } else if (dir == NorthWest) {
                if (blocked) {
                    if (!North.isBlocked(clipFlags, x, y, srcSize)) {
                        y++
                    } else if (!West.isBlocked(clipFlags, x, y, srcSize)) {
                        x--
                    }
                } else {
                    x--
                    y++
                }
            } else if (dir == SouthEast) {
                if (blocked) {
                    if (!South.isBlocked(clipFlags, x, y, srcSize)) {
                        y--
                    } else if (!East.isBlocked(clipFlags, x, y, srcSize)) {
                        x++
                    }
                } else {
                    x++
                    y--
                }
            } else if (dir == NorthEast) {
                if (blocked) {
                    if (!North.isBlocked(clipFlags, x, y, srcSize)) {
                        y++
                    } else if (!East.isBlocked(clipFlags, x, y, srcSize)) {
                        x++
                    }
                } else {
                    x++
                    y++
                }
            }
            if (startX == x && startY == y) {
                /* no valid tile found */
                break
            }
            coords.add(RouteCoordinates(baseX + x, baseY + y))
        }
        return Route(coords, alternative = false, success = success)
    }

    private fun Direction.isBlocked(
        clipFlags: IntArray,
        x: Int,
        y: Int,
        srcSize: Int
    ): Boolean = when (srcSize) {
        1 -> isBlocked1(clipFlags, x, y)
        2 -> isBlocked2(clipFlags, x, y)
        else -> isBlockedN(clipFlags, x, y, srcSize)
    }

    private fun Direction.isBlocked1(
        clipFlags: IntArray,
        x: Int,
        y: Int
    ): Boolean = when (this) {
        South -> (clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH) != 0
        North -> (clipFlags[x, y + 1] and CollisionFlag.BLOCK_NORTH) != 0
        West -> (clipFlags[x - 1, y] and CollisionFlag.BLOCK_WEST) != 0
        East -> (clipFlags[x + 1, y] and CollisionFlag.BLOCK_EAST) != 0
        SouthWest -> (clipFlags[x - 1, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x - 1, y] and CollisionFlag.BLOCK_WEST) != 0
            || (clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH) != 0
        NorthWest -> (clipFlags[x - 1, y + 1] and CollisionFlag.BLOCK_NORTH_WEST) != 0
            || (clipFlags[x - 1, y] and CollisionFlag.BLOCK_WEST) != 0
            || (clipFlags[x, y + 1] and CollisionFlag.BLOCK_NORTH) != 0
        SouthEast -> (clipFlags[x + 1, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
            || (clipFlags[x + 1, y] and CollisionFlag.BLOCK_EAST) != 0
            || (clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH) != 0
        NorthEast -> (clipFlags[x + 1, y + 1] and CollisionFlag.BLOCK_NORTH_EAST) != 0
            || (clipFlags[x + 1, y] and CollisionFlag.BLOCK_EAST) != 0
            || (clipFlags[x, y + 1] and CollisionFlag.BLOCK_NORTH) != 0
    }

    private fun Direction.isBlocked2(
        clipFlags: IntArray,
        x: Int,
        y: Int
    ): Boolean = when (this) {
        South -> (clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x + 1, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
        North -> (clipFlags[x, y + 2] and CollisionFlag.BLOCK_NORTH_WEST) != 0
            || (clipFlags[x + 1, y + 2] and CollisionFlag.BLOCK_NORTH_EAST) != 0
        West -> (clipFlags[x - 1, y] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x - 1, y + 1] and CollisionFlag.BLOCK_NORTH_WEST) != 0
        East -> (clipFlags[x + 2, y] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
            || (clipFlags[x + 2, y + 1] and CollisionFlag.BLOCK_NORTH_EAST) != 0
        SouthWest -> (clipFlags[x - 1, y] and CollisionFlag.BLOCK_NORTH_WEST) != 0
            || (clipFlags[x - 1, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
        NorthWest -> (clipFlags[x - 1, y + 1] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x - 1, y + 2] and CollisionFlag.BLOCK_NORTH_WEST) != 0
            || (clipFlags[x, y + 2] and CollisionFlag.BLOCK_NORTH_EAST) != 0
        SouthEast -> (clipFlags[x + 1, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) != 0
            || (clipFlags[x + 2, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
            || (clipFlags[x + 2, y] and CollisionFlag.BLOCK_NORTH_EAST) != 0
        NorthEast -> (clipFlags[x + 1, y + 2] and CollisionFlag.BLOCK_NORTH_WEST) != 0
            || (clipFlags[x + 2, y + 2] and CollisionFlag.BLOCK_NORTH_EAST) != 0
            || (clipFlags[x + 2, y + 1] and CollisionFlag.BLOCK_SOUTH_EAST) != 0
    }

    private fun Direction.isBlockedN(
        clipFlags: IntArray,
        x: Int,
        y: Int,
        srcSize: Int
    ): Boolean = when (this) {
        South -> {
            if ((clipFlags[x, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x + srcSize - 1, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                (1 until srcSize - 1).any { clipFlags[x + it, y] and clipFlag != 0 }
            } else true
        }
        North -> {
            if ((clipFlags[x, y + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x + srcSize - 1, y + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                (1 until srcSize - 1).any { clipFlags[x + it, y] and clipFlag != 0 }
            } else true
        }
        West -> {
            if ((clipFlags[x - 1, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x - 1, y + srcSize - 1] and CollisionFlag.BLOCK_NORTH_WEST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST
                (1 until srcSize - 1).any { clipFlags[x + it, y] and clipFlag != 0 }
            } else true
        }
        East -> {
            if ((clipFlags[x + srcSize, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[x + srcSize, y + srcSize - 1] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                (1 until srcSize - 1).any { clipFlags[x + it, y] and clipFlag != 0 }
            } else true
        }
        SouthWest -> {
            if ((clipFlags[x - 1, y + srcSize - 2] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x - 1, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x + srcSize - 2, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                (1 until srcSize - 1).any {
                    clipFlags[x - 1, y + it - 1] and clipFlag1 != 0
                        || clipFlags[x + it - 1, y - 1] and clipFlag2 != 0
                }
            } else true
        }
        NorthWest -> {
            if ((clipFlags[x - 1, y + 1] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x - 1, y + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x, y + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST
                val clipFlag2 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                (1 until srcSize - 1).any {
                    clipFlags[x - 1, y + it - 1] and clipFlag1 != 0
                        || clipFlags[x + it - 1, y + srcSize] and clipFlag2 != 0
                }
            } else true
        }
        SouthEast -> {
            if ((clipFlags[x + 1, y - 1] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x + srcSize, y - 1] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[x + srcSize, y + srcSize - 2] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                (1 until srcSize - 1).any {
                    clipFlags[x + srcSize, y + it - 1] and clipFlag1 != 0
                        || clipFlags[x + it + 1, y - 1] and clipFlag2 != 0
                }
            } else true
        }
        NorthEast -> {
            if ((clipFlags[x + 1, y + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x + srcSize, y + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
                && (clipFlags[x + srcSize, y + 1] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                (1 until srcSize - 1).any {
                    clipFlags[x + it + 1, y + srcSize] and clipFlag1 != 0
                        || clipFlags[x + srcSize, y + it + 1] and clipFlag2 != 0
                }
            } else true
        }
    }

    private fun getDirection(srcX: Int, srcY: Int, destX: Int, destY: Int): Direction? {
        if (srcX == destX) {
            if (srcY > destY) {
                return South
            } else if (srcY < destY) {
                return North
            }
        } else return if (srcY == destY) {
            if (srcX > destX) {
                West
            } else {
                East
            }
        } else {
            if (srcX < destX && srcY < destY) {
                NorthEast
            } else if (srcX < destX) {
                SouthEast
            } else if (srcY < destY) {
                NorthWest
            } else {
                SouthWest
            }
        }
        return null
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun IntArray.get(x: Int, y: Int): Int {
        val index = (y * searchMapSize) + x
        return this[index]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline operator fun IntArray.set(x: Int, y: Int, value: Int) {
        val index = (y * searchMapSize) + x
        this[index] = value
    }
}
