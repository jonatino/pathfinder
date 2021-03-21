package org.rsmod.pathfinder.bound

internal fun reachWallDeco(
    flags: IntArray,
    mapSize: Int,
    srcX: Int,
    srcY: Int,
    destX: Int,
    destY: Int,
    srcSize: Int,
    shape: Int,
    rot: Int
): Boolean = when {
    srcSize == 1 && srcX == destX && destY == srcY -> true
    srcSize != 1 && destX >= srcX && srcSize + srcX + -1 >= destX && srcSize + destY + -1 >= destY -> true
    srcSize == 1 -> reachWallDeco1(flags, mapSize, srcX, srcY, destX, destY, shape, rot)
    else -> reachWallDecoN(flags, mapSize, srcX, srcY, destX, destY, srcSize, shape, rot)
}

private fun reachWallDeco1(
    flags: IntArray,
    mapSize: Int,
    srcX: Int,
    srcY: Int,
    destX: Int,
    destY: Int,
    shape: Int,
    rot: Int
): Boolean {
    if (shape in 6..7) {
        when (rot.alteredRotation(shape)) {
            0 -> {
                if (srcX == destX + 1 && srcY == destY &&
                    (flag(flags, mapSize, srcX, srcY) and 0x80) == 0
                ) return true
                if (srcX == destX && srcY == destY - 1 &&
                    (flag(flags, mapSize, srcX, srcY) and 0x2) == 0
                ) return true
            }
            1 -> {
                if (srcX == destX - 1 && srcY == destY &&
                    (flag(flags, mapSize, srcX, srcY) and 0x8) == 0
                ) return true
                if (srcX == destX && srcY == destY - 1 &&
                    (flag(flags, mapSize, srcX, srcY) and 0x2) == 0
                ) return true
            }
            2 -> {
                if (srcX == destX - 1 && srcY == destY &&
                    (flag(flags, mapSize, srcX, srcY) and 0x8) == 0
                ) return true
                if (srcX == destX && srcY == destY + 1 &&
                    (flag(flags, mapSize, srcX, srcY) and 0x20) == 0
                ) return true
            }
            3 -> {
                if (srcX == destX + 1 && srcY == destY &&
                    (flag(flags, mapSize, srcX, srcY) and 0x80) == 0
                ) return true
                if (srcX == destX && srcY == destY + 1 &&
                    (flag(flags, mapSize, srcX, srcY) and 0x20) == 0
                ) return true
            }
        }
    } else if (shape == 8) {
        if (srcX == destX && srcY == destY + 1 &&
            (flag(flags, mapSize, srcX, srcY) and 0x20) == 0
        ) return true
        if (srcX == destX && srcY == destY - 1 &&
            (flag(flags, mapSize, srcX, srcY) and 0x2) == 0
        ) return true
        if (srcX == destX - 1 && srcY == destY &&
            (flag(flags, mapSize, srcX, srcY) and 0x8) == 0
        ) return true

        return srcX == destX + 1 && srcY == destY &&
            (flag(flags, mapSize, srcX, srcY) and 0x80) == 0
    }
    return false
}

private fun reachWallDecoN(
    flags: IntArray,
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
    if (shape in 6..7) {
        when (rot.alteredRotation(shape)) {
            0 -> {
                if (srcX == destX + 1 && srcY <= destY && north >= destY &&
                    (flag(flags, mapSize, srcX, destY) and 0x80) == 0
                ) return true
                if (srcX <= destX && srcY == destY - srcSize && east >= destX &&
                    (flag(flags, mapSize, destX, north) and 0x2) == 0
                ) return true
            }
            1 -> {
                if (srcX == destX - srcSize && srcY <= destY && north >= destY &&
                    (flag(flags, mapSize, east, destY) and 0x8) == 0
                ) return true
                if (srcX <= destX && srcY == destY - srcSize && east >= destX &&
                    (flag(flags, mapSize, destX, north) and 0x2) == 0
                ) return true
            }
            2 -> {
                if (srcX == destX - srcSize && srcY <= destY && north >= destY &&
                    (flag(flags, mapSize, east, destY) and 0x8) == 0
                ) return true
                if (srcX <= destX && srcY == destY + 1 && east >= destX &&
                    (flag(flags, mapSize, destX, srcY) and 0x20) == 0
                ) return true
            }
            3 -> {
                if (srcX == destX + 1 && srcY <= destY && north >= destY &&
                    (flag(flags, mapSize, srcX, destY) and 0x80) == 0
                ) return true
                if (srcX <= destX && srcY == destY + 1 && east >= destX &&
                    (flag(flags, mapSize, destX, srcY) and 0x20) == 0
                ) return true
            }
        }
    } else if (shape == 8) {
        if (srcX <= destX && srcY == destY + 1 && east >= destX &&
            (flag(flags, mapSize, destX, srcY) and 0x20) == 0
        ) return true
        if (srcX <= destX && srcY == destY - srcSize && east >= destX &&
            (flag(flags, mapSize, destX, north) and 0x2) == 0
        ) return true
        if (srcX == destX - srcSize && srcY <= destY && north >= destY &&
            (flag(flags, mapSize, east, destY) and 0x8) == 0
        ) return true

        return srcX == destX + 1 && srcY <= destY && north >= destY &&
            (flag(flags, mapSize, srcX, destY) and 0x80) == 0
    }
    return false
}

private fun Int.alteredRotation(shape: Int): Int {
    return if (shape == 7) (this + 2) and 0x3 else this
}

private fun flag(flags: IntArray, width: Int, x: Int, y: Int): Int {
    return flags[(y * width) + x]
}
