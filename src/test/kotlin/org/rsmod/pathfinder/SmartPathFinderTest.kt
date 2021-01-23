package org.rsmod.pathfinder

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartPathFinderTest {

    @Test
    fun reachEmptyTile() {
        val pf = SmartPathFinder(resetOnSearch = false)
        val flags = IntArray(DEFAULT_SEARCH_MAP_SIZE * DEFAULT_SEARCH_MAP_SIZE)
        val src = RouteCoordinates(0, 0)
        val dest = RouteCoordinates(1, 0)
        val route = pf.findPath(flags, src.x, src.y, dest.x, dest.y)
        Assertions.assertEquals(1, route.size)
        Assertions.assertEquals(dest.x, route.last().x)
        Assertions.assertEquals(dest.y, route.last().y)
    }

    @Test
    fun failOccupiedTile() {
        val pf = SmartPathFinder(resetOnSearch = false)
        val flags = IntArray(DEFAULT_SEARCH_MAP_SIZE * DEFAULT_SEARCH_MAP_SIZE)
        val src = RouteCoordinates(0, 0)
        val dest = RouteCoordinates(1, 0)

        /* set flag mask to block path */
        val flagX = dest.x + (DEFAULT_SEARCH_MAP_SIZE / 2)
        val flagY = dest.y + (DEFAULT_SEARCH_MAP_SIZE / 2)
        flags[(flagY * DEFAULT_SEARCH_MAP_SIZE) + flagX] = CollisionFlag.FLOOR

        val route = pf.findPath(flags, src.x, src.y, dest.x, dest.y)
        Assertions.assertTrue(route.isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(ParameterFileNameProvider::class)
    fun reachDestination(resourceFile: String) {
        val params = loadParameters(resourceFile)
        val pf = SmartPathFinder(resetOnSearch = false)
        val route = pf.findPath(params.flags, params.srcX, params.srcY, params.destX, params.destY)
        Assertions.assertEquals(params.expectedX, route.last().x)
        Assertions.assertEquals(params.expectedY, route.last().y)
    }

    private fun loadParameters(resourceFile: String): PathParameter {
        val mapper = ObjectMapper(JsonFactory())
        val input = Route::class.java.getResourceAsStream(resourceFile)
        return input.use { mapper.readValue(it, PathParameter::class.java) }
    }
}

private object ParameterFileNameProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        return Stream.of(
            Arguments.of("lumbridge.json"),
            Arguments.of("barb-village.json"),
            Arguments.of("gnome-maze.json") /* stops after 24 turns */
        )
    }
}
