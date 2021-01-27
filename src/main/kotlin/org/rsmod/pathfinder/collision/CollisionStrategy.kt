package org.rsmod.pathfinder.collision

public interface CollisionStrategy {

    public fun canMove(tileFlag: Int, blockFlag: Int): Boolean
}

public class NormalBlockFlagCollision : CollisionStrategy {

    override fun canMove(tileFlag: Int, blockFlag: Int): Boolean {
        return (tileFlag and blockFlag) == 0
    }
}

public class InverseBlockFlagCollision(
    private val inverseFlag: Int
) : CollisionStrategy {

    override fun canMove(tileFlag: Int, blockFlag: Int): Boolean {
        if (inverseFlag == 0) {
            return (tileFlag and blockFlag) == 0
        }
        val flag = blockFlag and inverseFlag.inv()
        return (tileFlag and flag) == 0 && (tileFlag and inverseFlag) != 0
    }
}
