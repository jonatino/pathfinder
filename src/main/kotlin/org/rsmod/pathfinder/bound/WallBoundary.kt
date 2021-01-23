package org.rsmod.pathfinder.bound

internal fun reachWall(
    clipFlags: IntArray,
    mapSize: Int,
    srcX: Int,
    srcY: Int,
    destX: Int,
    destY: Int,
    srcSize: Int,
    shape: Int,
    rot: Int
): Boolean = when {
    srcSize == 1 && srcX == destX && srcY == destY -> true
    srcSize != 1 && destX >= srcX && srcSize + srcX - 1 >= destX && srcSize + destY - 1 >= destY -> true
    srcSize == 1 -> reachWall1(clipFlags, mapSize, srcX, srcY, destX, destY, shape, rot)
    else -> reachWallN(clipFlags, mapSize, srcX, srcY, destX, destY, srcSize, shape, rot)
}

private fun reachWall1(
    clipFlags: IntArray,
    mapSize: Int,
    srcX: Int,
    srcY: Int,
    destX: Int,
    destY: Int,
    shape: Int,
    rot: Int
): Boolean {
    when (shape) {
        0 -> {
            when (rot) {
                0 -> {
                    if (srcX == destX - 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY + 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX && srcY == destY - 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0102) == 0
                    ) return true
                }
                1 -> {
                    if (srcX == destX && srcY == destY + 1)
                        return true
                    if (srcX == destX - 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0108) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0180) == 0
                    ) return true
                }
                2 -> {
                    if (srcX == destX + 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY + 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX && srcY == destY - 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0102) == 0
                    ) return true
                }
                3 -> {
                    if (srcX == destX && srcY == destY - 1)
                        return true
                    if (srcX == destX - 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0102) == 0
                    ) return true
                }
            }
        }
        2 -> {
            when (rot) {
                0 -> {
                    if (srcX == destX - 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY + 1)
                        return true
                    if (srcX == destX + 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0180) == 0
                    ) return true
                    if (srcX == destX && srcY == destY - 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0102) == 0
                    ) return true
                }
                1 -> {
                    if (srcX == destX - 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0108) == 0
                    ) return true
                    if (srcX == destX && srcY == destY + 1)
                        return true
                    if (srcX == destX + 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY - 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0102) == 0
                    ) return true
                }
                2 -> {
                    if (srcX == destX - 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0108) == 0
                    ) return true
                    if (srcX == destX && srcY == destY + 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY - 1)
                        return true
                }
                3 -> {
                    if (srcX == destX - 1 && srcY == destY)
                        return true
                    if (srcX == destX && srcY == destY + 1
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY == destY
                        && (flag(clipFlags, mapSize, srcX, srcY) and 0x12c0180) == 0
                    ) return true
                    if (srcX == destX && srcY == destY - 1)
                        return true
                }
            }
        }
        9 -> {
            if (srcX == destX && srcY == destY + 1
                && (flag(clipFlags, mapSize, srcX, srcY) and 0x20) == 0
            ) return true
            if (srcX == destX && srcY == destY - 1
                && (flag(clipFlags, mapSize, srcX, srcY) and 0x2) == 0
            ) return true
            if (srcX == destX - 1 && srcY == destY
                && (flag(clipFlags, mapSize, srcX, srcY) and 0x8) == 0
            ) return true

            return srcX == destX + 1 && srcY == destY
                && (flag(clipFlags, mapSize, srcX, srcY) and 0x80) == 0
        }
    }
    return false
}

private fun reachWallN(
    clipFlags: IntArray,
    mapSize: Int,
    srcX: Int,
    srcY: Int,
    destX: Int,
    destY: Int,
    srcSize: Int,
    shape: Int,
    rot: Int
): Boolean {
    val east = srcX + srcSize - 1
    val north = srcY + srcSize - 1
    when (shape) {
        0 -> {
            when (rot) {
                0 -> {
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY + 1
                        && (flag(clipFlags, mapSize, destX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY - srcSize
                        && (flag(clipFlags, mapSize, destX, north) and 0x12c0102) == 0
                    ) return true
                }
                1 -> {
                    if (destX in srcX..east && srcY == destY + 1)
                        return true
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, east, destY) and 0x12c0108) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, srcX, destY) and 0x12c0180) == 0
                    ) return true
                }
                2 -> {
                    if (srcX == destX + 1 && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY + 1
                        && (flag(clipFlags, mapSize, destX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY - srcSize
                        && (flag(clipFlags, mapSize, destX, north) and 0x12c0102) == 0
                    ) return true
                }
                3 -> {
                    if (destX in srcX..east && srcY == destY - srcSize)
                        return true
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, east, destY) and 0x12c0108) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, srcX, destY) and 0x12c0180) == 0
                    ) return true
                }
            }
        }
        2 -> {
            when (rot) {
                0 -> {
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY + 1)
                        return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, srcX, destY) and 0x12c0180) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY - srcSize
                        && (flag(clipFlags, mapSize, destX, north) and 0x12c0102) == 0
                    ) return true
                }
                1 -> {
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, east, destY) and 0x12c0108) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY + 1)
                        return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY - srcSize
                        && (flag(clipFlags, mapSize, destX, north) and 0x12c0102) == 0
                    ) return true
                }
                2 -> {
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, east, destY) and 0x12c0108) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY + 1
                        && (flag(clipFlags, mapSize, destX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY - srcSize)
                        return true
                }
                3 -> {
                    if (srcX == destX - srcSize && srcY <= destY && north >= destY)
                        return true
                    if (destX in srcX..east && srcY == destY + 1
                        && (flag(clipFlags, mapSize, destX, srcY) and 0x12c0120) == 0
                    ) return true
                    if (srcX == destX + 1 && srcY <= destY && north >= destY
                        && (flag(clipFlags, mapSize, srcX, destY) and 0x12c0180) == 0
                    ) return true
                    if (destX in srcX..east && srcY == destY - srcSize)
                        return true
                }
            }
        }
        9 -> {
            if (destX in srcX..east && srcY == destY + 1
                && (flag(clipFlags, mapSize, destX, srcY) and 0x12c0120) == 0
            ) return true
            if (destX in srcX..east && srcY == destY - srcSize
                && (flag(clipFlags, mapSize, destX, north) and 0x12c0102) == 0
            ) return true
            if (srcX == destX - srcSize && srcY <= destY && north >= destY
                && (flag(clipFlags, mapSize, east, destY) and 0x12c0108) == 0
            ) return true

            return srcX == destX + 1 && srcY <= destY && north >= destY
                && (flag(clipFlags, mapSize, srcX, destY) and 0x12c0180) == 0
        }
    }
    return false
}

private fun flag(flags: IntArray, width: Int, x: Int, y: Int): Int {
    return flags[(y * width) + x]
}
