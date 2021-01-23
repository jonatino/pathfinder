package org.rsmod.pathfinder

internal data class PathParameter(
    val srcX: Int,
    val srcY: Int,
    val destX: Int,
    val destY: Int,
    val expectedX: Int,
    val expectedY: Int,
    val flags: IntArray
) {

    constructor() : this(0, 0, 0, 0, 0, 0, intArrayOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathParameter

        if (srcX != other.srcX) return false
        if (srcY != other.srcY) return false
        if (destX != other.destX) return false
        if (destY != other.destY) return false
        if (!flags.contentEquals(other.flags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = srcX
        result = 31 * result + srcY
        result = 31 * result + destX
        result = 31 * result + destY
        result = 31 * result + flags.contentHashCode()
        return result
    }
}
