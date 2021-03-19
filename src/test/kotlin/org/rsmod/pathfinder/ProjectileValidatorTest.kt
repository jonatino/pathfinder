package org.rsmod.pathfinder

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.rsmod.pathfinder.flag.CollisionFlag
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProjectileValidatorTest {

    private val validator = ProjectileValidator(DEFAULT_SEARCH_MAP_SIZE)

    private val flags = IntArray(validator.searchMapSize * validator.searchMapSize)

    private val halfMap: Int
        get() = validator.searchMapSize / 2

    @Test
    fun validateEmptyPath() {
        val src = RouteCoordinates(3200, 3200)
        val dest = src.translate(3, 0)
        val validPath = validator.isValid(
            flags,
            src.x,
            src.y,
            dest.x,
            dest.y
        )
        Assertions.assertTrue(validPath)
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidDirectionPath::class)
    internal fun invalidateBlockedPath(dir: Direction, flag: Int) {
        val src = RouteCoordinates(3200, 3200)
        val dest = src.translate(dir.offX * 6, dir.offY * 6)

        val flagX = halfMap + dir.offX
        val flagY = halfMap + dir.offY
        flags[(flagY * validator.searchMapSize) + flagX] = flag

        val validPath = validator.isValid(
            flags,
            src.x,
            src.y,
            dest.x,
            dest.y
        )
        Assertions.assertFalse(validPath)
    }

    private object InvalidDirectionPath : ArgumentsProvider {

        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(
                    North,
                    CollisionFlag.WALL_SOUTH_PROJECTILE_BLOCKER
                ),
                Arguments.of(
                    South,
                    CollisionFlag.WALL_NORTH_PROJECTILE_BLOCKER
                ),
                Arguments.of(
                    East,
                    CollisionFlag.WALL_WEST_PROJECTILE_BLOCKER
                ),
                Arguments.of(
                    West,
                    CollisionFlag.WALL_EAST_PROJECTILE_BLOCKER
                )
            )
        }
    }
}
