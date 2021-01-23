package org.rsmod.pathfinder

import org.rsmod.pathfinder.bound.reachRectangle
import org.rsmod.pathfinder.bound.reachWall
import org.rsmod.pathfinder.bound.reachWallDeco
import org.rsmod.pathfinder.flag.CollisionFlag
import org.rsmod.pathfinder.flag.DirectionFlag
import java.util.Arrays
import java.util.LinkedList
import kotlin.collections.ArrayList

private const val DEFAULT_RESET_ON_SEARCH = true
internal const val DEFAULT_SEARCH_MAP_SIZE = 144
private const val DEFAULT_RING_BUFFER_SIZE = 4096
private const val DEFAULT_MAX_TURNS = 24

private const val DEFAULT_DISTANCE_VALUE = 99999999
private const val MAX_ALTERNATIVE_ROUTE_LOWEST_COST = 1000
private const val MAX_ALTERNATIVE_ROUTE_SEEK_RANGE = 100
private const val MAX_ALTERNATIVE_ROUTE_DISTANCE_FROM_DESTINATION = 10

private val EMPTY_QUEUE = LinkedList<RouteCoordinates>()

/*
 * For optimization, we use this value to separate each section
 * where the list of route coordinates made a turn in direction.
 */
private val TURN_COORDS = RouteCoordinates(0)

public class SmartPathFinder(
    private val resetOnSearch: Boolean = DEFAULT_RESET_ON_SEARCH,
    private val searchMapSize: Int = DEFAULT_SEARCH_MAP_SIZE,
    private val ringBufferSize: Int = DEFAULT_RING_BUFFER_SIZE,
    private val directions: IntArray = IntArray(searchMapSize * searchMapSize),
    private val distances: IntArray = IntArray(searchMapSize * searchMapSize),
    private val ringBufferX: IntArray = IntArray(ringBufferSize),
    private val ringBufferY: IntArray = IntArray(ringBufferSize),
    private var bufReaderIndex: Int = 0,
    private var bufWriterIndex: Int = 0,
    private var currentX: Int = 0,
    private var currentY: Int = 0
) {

    public fun findPath(
        clipFlags: IntArray,
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
        destWidth: Int = 0,
        destHeight: Int = 0,
        srcSize: Int = 1,
        objRot: Int = 0,
        objShape: Int = 0,
        accessBitMask: Int = 0,
        moveNear: Boolean = true,
        maxTurns: Int = DEFAULT_MAX_TURNS
    ): List<RouteCoordinates> {
        require(clipFlags.size == directions.size) {
            "Clipping flag size must be same size as [directions] and [distances]"
        }
        if (resetOnSearch) {
            reset()
        }
        val baseX = srcX - (searchMapSize / 2)
        val baseY = srcY - (searchMapSize / 2)
        val localSrcX = srcX - baseX
        val localSrcY = srcY - baseY
        val localDestX = destX - baseX
        val localDestY = destY - baseY
        directions[localSrcX, localSrcY] = 99
        distances[localSrcX, localSrcY] = 0
        setAndIncrementWriterBuf(localSrcX, localSrcY)
        val pathFound = when (srcSize) {
            1 -> findPath1(
                clipFlags,
                localDestX,
                localDestY,
                destWidth,
                destHeight,
                srcSize,
                objRot,
                objShape,
                accessBitMask
            )
            2 -> findPath2(
                clipFlags,
                localDestX,
                localDestY,
                destWidth,
                destHeight,
                srcSize,
                objRot,
                objShape,
                accessBitMask
            )
            else -> findPathN(
                clipFlags,
                localDestX,
                localDestY,
                destWidth,
                destHeight,
                srcSize,
                objRot,
                objShape,
                accessBitMask
            )
        }
        if (!pathFound) {
            if (!moveNear) {
                return Route(EMPTY_QUEUE, alternative = false, success = false)
            } else if (!findClosestApproachPoint(localSrcX, localSrcY, localDestX, localDestY, destWidth, destHeight)) {
                return Route(EMPTY_QUEUE, alternative = false, success = false)
            }
        }
        val coordinates = ArrayList<RouteCoordinates>(bufWriterIndex)
        var nextDir = directions[currentX, currentY]
        var currDir = -1
        for (i in 0 until searchMapSize * searchMapSize) {
            if (currentX == localSrcX && currentY == localSrcY) {
                break
            }
            val coords = RouteCoordinates(currentX + baseX, currentY + baseY)
            if (currDir != nextDir) {
                coordinates.add(0, TURN_COORDS)
                currDir = nextDir
            }
            coordinates.add(0, coords)
            if ((currDir and DirectionFlag.EAST) != 0) {
                currentX++
            } else if ((currDir and DirectionFlag.WEST) != 0) {
                currentX--
            }
            if ((currDir and DirectionFlag.NORTH) != 0) {
                currentY++
            } else if ((currDir and DirectionFlag.SOUTH) != 0) {
                currentY--
            }
            nextDir = directions[currentX, currentY]
        }
        val turns = coordinates.count { it == TURN_COORDS }
        return if (turns > maxTurns) {
            val filtered = mutableListOf<RouteCoordinates>()
            var currTurns = 0
            for (coords in coordinates) {
                if (currTurns > maxTurns) break
                if (coords == TURN_COORDS) {
                    currTurns++
                    continue
                }
                filtered.add(coords)
            }
            Route(filtered, alternative = !pathFound, success = true)
        } else {
            Route(coordinates.filter { it != TURN_COORDS }, alternative = !pathFound, success = true)
        }
    }

    private fun findPath1(
        clipFlags: IntArray,
        destX: Int,
        destY: Int,
        destWidth: Int,
        destHeight: Int,
        srcSize: Int,
        objRot: Int,
        objShape: Int,
        accessBitMask: Int
    ): Boolean {
        var x: Int
        var y: Int
        var clipFlag: Int
        var dirFlag: Int
        val relativeSearchSize = searchMapSize - 1
        while (bufWriterIndex != bufReaderIndex) {
            currentX = ringBufferX[bufReaderIndex]
            currentY = ringBufferY[bufReaderIndex]
            bufReaderIndex = (bufReaderIndex + 1) and (ringBufferSize - 1)

            if (reached(
                    clipFlags,
                    currentX,
                    currentY,
                    destX,
                    destY,
                    destWidth,
                    destHeight,
                    srcSize,
                    objRot,
                    objShape,
                    accessBitMask
                )
            ) {
                return true
            }

            val nextDistance = distances[currentX, currentY] + 1

            /* east to west */
            x = currentX - 1
            y = currentY
            clipFlag = CollisionFlag.BLOCK_WEST
            dirFlag = DirectionFlag.EAST
            if (currentX > 0 && directions[x, y] == 0 && (clipFlags[x, y] and clipFlag) == 0) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* west to east */
            x = currentX + 1
            y = currentY
            clipFlag = CollisionFlag.BLOCK_EAST
            dirFlag = DirectionFlag.WEST
            if (currentX < relativeSearchSize && directions[x, y] == 0 && (clipFlags[x, y] and clipFlag) == 0) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north to south  */
            x = currentX
            y = currentY - 1
            clipFlag = CollisionFlag.BLOCK_SOUTH
            dirFlag = DirectionFlag.NORTH
            if (currentY > 0 && directions[x, y] == 0 && (clipFlags[x, y] and clipFlag) == 0) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south to north */
            x = currentX
            y = currentY + 1
            clipFlag = CollisionFlag.BLOCK_NORTH
            dirFlag = DirectionFlag.SOUTH
            if (currentY < relativeSearchSize && directions[x, y] == 0 && (clipFlags[x, y] and clipFlag) == 0) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north-east to south-west */
            x = currentX - 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_EAST
            if (currentX > 0 && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x, currentY] and CollisionFlag.BLOCK_WEST) == 0
                && (clipFlags[currentX, y] and CollisionFlag.BLOCK_SOUTH) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north-west to south-east */
            x = currentX + 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_WEST
            if (currentX < relativeSearchSize && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[x, currentY] and CollisionFlag.BLOCK_EAST) == 0
                && (clipFlags[currentX, y] and CollisionFlag.BLOCK_SOUTH) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south-east to north-west */
            x = currentX - 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_EAST
            if (currentX > 0 && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x, currentY] and CollisionFlag.BLOCK_WEST) == 0
                && (clipFlags[currentX, y] and CollisionFlag.BLOCK_NORTH) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south-west to north-east */
            x = currentX + 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_WEST
            if (currentX < relativeSearchSize && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_NORTH_EAST) == 0
                && (clipFlags[x, currentY] and CollisionFlag.BLOCK_EAST) == 0
                && (clipFlags[currentX, y] and CollisionFlag.BLOCK_NORTH) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }
        }
        return false
    }

    private fun findPath2(
        clipFlags: IntArray,
        destX: Int,
        destY: Int,
        destWidth: Int,
        destHeight: Int,
        srcSize: Int,
        objRot: Int,
        objShape: Int,
        accessBitMask: Int
    ): Boolean {
        var x: Int
        var y: Int
        var dirFlag: Int
        val relativeSearchSize = searchMapSize - 2
        while (bufWriterIndex != bufReaderIndex) {
            currentX = ringBufferX[bufReaderIndex]
            currentY = ringBufferY[bufReaderIndex]
            bufReaderIndex = (bufReaderIndex + 1) and (ringBufferSize - 1)

            if (reached(
                    clipFlags,
                    currentX,
                    currentY,
                    destX,
                    destY,
                    destWidth,
                    destHeight,
                    srcSize,
                    objRot,
                    objShape,
                    accessBitMask
                )
            ) {
                return true
            }

            val nextDistance = distances[currentX, currentY] + 1

            /* east to west */
            x = currentX - 1
            y = currentY
            dirFlag = DirectionFlag.EAST
            if (currentX > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x, currentY + 1] and CollisionFlag.BLOCK_NORTH_WEST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* west to east */
            x = currentX + 1
            y = currentY
            dirFlag = DirectionFlag.WEST
            if (currentX < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[currentX + 2, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[currentX + 2, currentY + 1] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north to south  */
            x = currentX
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH
            if (currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX + 1, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south to north */
            x = currentX
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH
            if (currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, currentY + 2] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX + 1, currentY + 2] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north-east to south-west */
            x = currentX - 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_EAST
            if (currentX > 0 && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, currentY] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* north-west to south-east */
            x = currentX + 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_WEST
            if (currentX < relativeSearchSize && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX + 2, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[currentX + 2, currentY] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south-east to north-west */
            x = currentX - 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_EAST
            if (currentX > 0 && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x, currentY + 2] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX, currentY + 2] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }

            /* south-west to north-east */
            x = currentX + 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_WEST
            if (currentX < relativeSearchSize && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, currentY + 2] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX + 2, currentY + 2] and CollisionFlag.BLOCK_NORTH_EAST) == 0
                && (clipFlags[currentX + 2, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                setAndIncrementWriterBuf(x, y)
                directions[x, y] = dirFlag
                distances[x, y] = nextDistance
            }
        }
        return false
    }

    private fun findPathN(
        clipFlags: IntArray,
        destX: Int,
        destY: Int,
        destWidth: Int,
        destHeight: Int,
        srcSize: Int,
        objRot: Int,
        objShape: Int,
        accessBitMask: Int
    ): Boolean {
        var x: Int
        var y: Int
        var dirFlag: Int
        val relativeSearchSize = searchMapSize - srcSize
        while (bufWriterIndex != bufReaderIndex) {
            currentX = ringBufferX[bufReaderIndex]
            currentY = ringBufferY[bufReaderIndex]
            bufReaderIndex = (bufReaderIndex + 1) and (ringBufferSize - 1)

            if (reached(
                    clipFlags,
                    currentX,
                    currentY,
                    destX,
                    destY,
                    destWidth,
                    destHeight,
                    srcSize,
                    objRot,
                    objShape,
                    accessBitMask
                )
            ) {
                return true
            }

            val nextDistance = distances[currentX, currentY] + 1

            /* east to west */
            x = currentX - 1
            y = currentY
            dirFlag = DirectionFlag.EAST
            if (currentX > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x, currentY + srcSize - 1] and CollisionFlag.BLOCK_NORTH_WEST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST
                val blocked = (1 until srcSize - 1).any { clipFlags[x, currentY + it] and clipFlag != 0 }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* west to east */
            x = currentX + 1
            y = currentY
            dirFlag = DirectionFlag.WEST
            if (currentX < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[currentX + srcSize, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[currentX + srcSize, currentY + srcSize - 1] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                val blocked = (1 until srcSize - 1).any { clipFlags[currentX + srcSize, currentY + it] and clipFlag != 0 }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* north to south  */
            x = currentX
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH
            if (currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX + srcSize - 1, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                val blocked = (1 until srcSize - 1).any { clipFlags[currentX + it, y] and clipFlag != 0 }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* south to north */
            x = currentX
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH
            if (currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX + srcSize - 1, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val blocked = (1 until srcSize - 1).any { clipFlags[x + it, currentY + srcSize] and clipFlag != 0 }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* north-east to south-west */
            x = currentX - 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_EAST
            if (currentX > 0 && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, currentY + srcSize - 2] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX + srcSize - 2, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                val blocked = (1 until srcSize - 1).any {
                    clipFlags[x, currentY + it - 1] and clipFlag1 != 0
                        || clipFlags[currentX + it - 1, y] and clipFlag2 != 0
                }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* north-west to south-east */
            x = currentX + 1
            y = currentY - 1
            dirFlag = DirectionFlag.NORTH_WEST
            if (currentX < relativeSearchSize && currentY > 0 && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[currentX + srcSize, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
                && (clipFlags[currentX + srcSize, currentY + srcSize - 2] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_EAST_AND_WEST
                val blocked = (1 until srcSize - 1).any {
                    clipFlags[currentX + srcSize, currentY + it - 1] and clipFlag1 != 0
                        || clipFlags[currentX + it + 1, y] and clipFlag2 != 0
                }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* south-east to north-west */
            x = currentX - 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_EAST
            if (currentX > 0 && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, y] and CollisionFlag.BLOCK_SOUTH_WEST) == 0
                && (clipFlags[x, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_EAST
                val clipFlag2 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val blocked = (1 until srcSize - 1).any {
                    clipFlags[x, currentY + it + 1] and clipFlag1 != 0
                        || clipFlags[currentX + it - 1, currentY + srcSize] and clipFlag2 != 0
                }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }

            /* south-west to north-east */
            x = currentX + 1
            y = currentY + 1
            dirFlag = DirectionFlag.SOUTH_WEST
            if (currentX < relativeSearchSize && currentY < relativeSearchSize && directions[x, y] == 0
                && (clipFlags[x, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_WEST) == 0
                && (clipFlags[currentX + srcSize, currentY + srcSize] and CollisionFlag.BLOCK_NORTH_EAST) == 0
                && (clipFlags[currentX + srcSize, y] and CollisionFlag.BLOCK_SOUTH_EAST) == 0
            ) {
                val clipFlag1 = CollisionFlag.BLOCK_SOUTH_EAST_AND_WEST
                val clipFlag2 = CollisionFlag.BLOCK_NORTH_AND_SOUTH_WEST
                val blocked = (1 until srcSize - 1).any {
                    clipFlags[currentX + it + 1, currentY + srcSize] and clipFlag1 != 0
                        || clipFlags[currentX + srcSize, currentY + it + 1] and clipFlag2 != 0
                }
                if (!blocked) {
                    setAndIncrementWriterBuf(x, y)
                    directions[x, y] = dirFlag
                    distances[x, y] = nextDistance
                }
            }
        }
        return false
    }

    private fun findClosestApproachPoint(
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
        destWidth: Int,
        destHeight: Int
    ): Boolean {
        var lowestCost = MAX_ALTERNATIVE_ROUTE_LOWEST_COST
        var maxAlternativePath = MAX_ALTERNATIVE_ROUTE_SEEK_RANGE
        val alternativeRouteRange = MAX_ALTERNATIVE_ROUTE_DISTANCE_FROM_DESTINATION
        val radiusX = destX - alternativeRouteRange..destX + alternativeRouteRange
        val radiusY = destY - alternativeRouteRange..destY + alternativeRouteRange
        for (x in radiusX) {
            for (y in radiusY) {
                if (x !in 0 until searchMapSize
                    || y !in 0 until searchMapSize
                    || distances[x, y] >= MAX_ALTERNATIVE_ROUTE_SEEK_RANGE
                ) {
                    continue
                }
                val dx = when {
                    destX > x -> destX - x
                    destWidth + destX - 1 < x -> x + 1 - destX - destWidth
                    else -> 0
                }
                val dy = when {
                    destY > y -> destY - y
                    destHeight + destY - 1 < y -> x + 1 - destY - destHeight
                    else -> 0
                }
                val cost = dx * dx + dy * dy
                if (cost < lowestCost || (cost == lowestCost && maxAlternativePath > distances[x, y])) {
                    currentX = x
                    currentY = y
                    lowestCost = cost
                    maxAlternativePath = distances[x, y]
                }
            }
        }
        return !(lowestCost == MAX_ALTERNATIVE_ROUTE_LOWEST_COST || (srcX == currentX && srcY == currentY))
    }

    private fun reached(
        clipFlags: IntArray,
        srcX: Int,
        srcY: Int,
        destX: Int,
        destY: Int,
        destWidth: Int,
        destHeight: Int,
        srcSize: Int,
        rotation: Int,
        shape: Int,
        accessBitMask: Int
    ): Boolean {
        if (srcX == destX && srcY == destY) {
            return true
        }
        if (shape != 0) {
            if ((shape < 5 || shape == 10)
                && reachWall(clipFlags, searchMapSize, srcX, srcY, destX, destY, srcSize, shape - 1, rotation)
            ) {
                return true
            }
            if (shape < 10
                && reachWallDeco(clipFlags, searchMapSize, srcX, srcY, destX, destY, srcSize, shape - 1, rotation)
            ) {
                return true
            }
        }
        return destWidth != 0 && destHeight != 0
            && reachRectangle(
            clipFlags,
            searchMapSize,
            accessBitMask,
            srcX,
            srcY,
            destX,
            destY,
            srcSize,
            destWidth,
            destHeight
        )
    }

    private fun reset() {
        Arrays.setAll(directions) { 0 }
        Arrays.setAll(distances) { DEFAULT_DISTANCE_VALUE }
        bufReaderIndex = 0
        bufWriterIndex = 0
    }

    private fun setAndIncrementWriterBuf(x: Int, y: Int) {
        ringBufferX[bufWriterIndex] = x
        ringBufferY[bufWriterIndex] = y
        bufWriterIndex = (bufWriterIndex + 1) and (ringBufferSize - 1)
    }

    private operator fun IntArray.get(x: Int, y: Int): Int {
        val index = (y * searchMapSize) + x
        return this[index]
    }

    private operator fun IntArray.set(x: Int, y: Int, value: Int) {
        val index = (y * searchMapSize) + x
        this[index] = value
    }
}
