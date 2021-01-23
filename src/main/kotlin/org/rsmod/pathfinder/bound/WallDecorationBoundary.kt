package org.rsmod.pathfinder.bound

internal fun reachWallDeco(
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
    srcSize == 1 && srcX == destX && destY == srcY -> true
    destX >= srcX && srcSize + srcX + -1 >= destX && srcSize + destY + -1 >= destY -> true
    srcSize == 1 -> reachWallDeco1(clipFlags, mapSize, srcX, srcY, destX, destY, shape, rot)
    else -> reachWallDecoN(clipFlags, mapSize, srcX, srcY, destX, destY, srcSize, shape, rot)
}

private fun reachWallDeco1(
    clipFlags: IntArray,
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
                if (srcX == destX + 1 && srcY == destY
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x80) == 0
                ) return true
                if (srcX == destX && srcY == destY - 1
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x2) == 0
                ) return true
            }
            1 -> {
                if (srcX == destX - 1 && srcY == destY
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x8) == 0
                ) return true
                if (srcX == destX && srcY == destY - 1
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x2) == 0
                ) return true
            }
            2 -> {
                if (srcX == destX - 1 && srcY == destY
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x8) == 0
                ) return true
                if (srcX == destX && srcY == destY + 1
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x20) == 0
                ) return true
            }
            3 -> {
                if (srcX == destX + 1 && srcY == destY
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x80) == 0
                ) return true
                if (srcX == destX && srcY == destY + 1
                    && (flag(clipFlags, mapSize, srcX, srcY) and 0x20) == 0
                ) return true
            }
        }
    } else if (shape == 8) {
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
    return false
}

private fun reachWallDecoN(
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
    if (shape in 6..7) {
        when (rot.alteredRotation(shape)) {
            0 -> {
                if (srcX == destX + 1 && srcY <= destY && north >= destY
                    && (flag(clipFlags, mapSize, srcX, destY) and 0x80) == 0
                ) return true
                if (srcX <= destX && srcY == destY - srcSize && east >= destX
                    && (flag(clipFlags, mapSize, destX, north) and 0x2) == 0
                ) return true
            }
            1 -> {
                if (srcX == destX - srcSize && srcY <= destY && north >= destY
                    && (flag(clipFlags, mapSize, east, destY) and 0x8) == 0
                ) return true
                if (srcX <= destX && srcY == destY - srcSize && east >= destX
                    && (flag(clipFlags, mapSize, destX, north) and 0x2) == 0
                ) return true
            }
            2 -> {
                if (srcX == destX - srcSize && srcY <= destY && north >= destY
                    && (flag(clipFlags, mapSize, east, destY) and 0x8) == 0
                ) return true
                if (srcX <= destX && srcY == destY + 1 && east >= destX
                    && (flag(clipFlags, mapSize, destX, srcY) and 0x20) == 0
                ) return true
            }
            3 -> {
                if (srcX == destX + 1 && srcY <= destY && north >= destY
                    && (flag(clipFlags, mapSize, srcX, destY) and 0x80) == 0
                ) return true
                if (srcX <= destX && srcY == destY + 1 && east >= destX
                    && (flag(clipFlags, mapSize, destX, srcY) and 0x20) == 0
                ) return true
            }
        }
    } else if (shape == 8) {
        if (srcX <= destX && srcY == destY + 1 && east >= destX
            && (flag(clipFlags, mapSize, destX, srcY) and 0x20) == 0
        ) return true
        if (srcX <= destX && srcY == destY - srcSize && east >= destX
            && (flag(clipFlags, mapSize, destX, north) and 0x2) == 0
        ) return true
        if (srcX == destX - srcSize && srcY <= destY && north >= destY
            && (flag(clipFlags, mapSize, east, destY) and 0x8) == 0
        ) return true

        return srcX == destX + 1 && srcY <= destY && north >= destY
            && (flag(clipFlags, mapSize, srcX, destY) and 0x80) == 0
    }
    return false
}

private fun Int.alteredRotation(shape: Int): Int {
    return if (shape == 7) (this + 2) and 0x3 else this
}

private fun flag(flags: IntArray, width: Int, x: Int, y: Int): Int {
    return flags[(y * width) + x]
}
