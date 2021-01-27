package org.rsmod.pathfinder.collision

import org.rsmod.pathfinder.flag.CollisionFlag

public object CollisionStrategies {

    public val Normal: CollisionStrategy = NormalBlockFlagCollision()
    public val Swim: CollisionStrategy = InverseBlockFlagCollision(CollisionFlag.FLOOR)
}
