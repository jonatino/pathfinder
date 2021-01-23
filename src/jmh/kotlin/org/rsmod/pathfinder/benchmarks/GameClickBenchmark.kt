package org.rsmod.pathfinder.benchmarks

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.rsmod.pathfinder.Route
import org.rsmod.pathfinder.SmartPathFinder
import java.util.concurrent.TimeUnit

open class GameClickShortPath : GameClickBenchmark("short-path.json")
open class GameClickMedPath : GameClickBenchmark("med-path.json")
open class GameClickLongPath : GameClickBenchmark("long-path.json")

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(value = 1)
abstract class GameClickBenchmark(private val parameterResourceName: String) {

    private lateinit var params: PathFinderParameter

    @Setup
    fun setup() {
        val stream = Route::class.java.getResourceAsStream(parameterResourceName)
        val mapper = ObjectMapper(JsonFactory())
        params = stream.use { mapper.readValue(it, PathFinderParameter::class.java) }

        val mapSize = ClientPathfinder.SIZE;
        for (i in ClientPathfinder.scene.indices) {
            ClientPathfinder.scene[i] = ClientMapArea(mapSize, mapSize)
        }
        for (y in 0 until mapSize) {
            for (x in 0 until mapSize) {
                val index = (y * mapSize) + x
                val flag = params.flags[index]
                ClientPathfinder.scene[0].flags[x][y] = flag
            }
        }
    }

    @Benchmark
    fun clientPath() {
        val (iterations, srcX, srcY, destX, destY) = params
        repeat(iterations) {
            ClientPathfinder.findPath(
                0, true, destY, 0, srcY, 2, 0, 0, 0, true, srcX, destX, 1
            )
        }
    }

    @Benchmark
    fun serverPathConstructOnIteration() {
        val (iterations, srcX, srcY, destX, destY, clipFlags) = params
        repeat(iterations) {
            val pf = SmartPathFinder(resetOnSearch = false)
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }
    }

    @Benchmark
    fun serverPathResetOnIteration() {
        val (iterations, srcX, srcY, destX, destY, clipFlags) = params
        val pf = SmartPathFinder(resetOnSearch = true)
        repeat(iterations) {
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }
    }
}
