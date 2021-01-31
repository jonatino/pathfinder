package org.rsmod.pathfinder.benchmarks

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

open class GameClickShortPath : SmartPathFinderBenchmark("short-path.json")
open class GameClickMedPath : SmartPathFinderBenchmark("med-path.json")
open class GameClickLongPath : SmartPathFinderBenchmark("long-path.json")
open class GameClickAltPath : SmartPathFinderBenchmark("outofbound-path.json")

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(value = 1)
abstract class SmartPathFinderBenchmark(
    private val parameterResourceName: String,
    private val pathRequests: Int = 2000
) {

    private lateinit var params: PathFinderParameter
    private lateinit var scope: CoroutineScope

    @Setup
    fun setup() {
        val stream = Route::class.java.getResourceAsStream(parameterResourceName)
        val mapper = ObjectMapper(JsonFactory())
        params = stream.use { mapper.readValue(it, PathFinderParameter::class.java) }

        val executor = ForkJoinPool(Runtime.getRuntime().availableProcessors())
        val dispatcher = executor.asCoroutineDispatcher()
        scope = CoroutineScope(dispatcher)

        val mapSize = ClientPathfinder.SIZE
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
        val (srcX, srcY, destX, destY) = params
        repeat(pathRequests) {
            ClientPathfinder.findPath(
                0, true, destY, 0, srcY, 2, 0, 0, 0, true, srcX, destX, 1
            )
        }
    }

    @Benchmark
    fun serverPathConstructOnIteration() {
        val (srcX, srcY, destX, destY, clipFlags) = params
        repeat(pathRequests) {
            val pf = SmartPathFinder(resetOnSearch = false)
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }
    }

    @Benchmark
    fun serverPathResetOnIteration() {
        val (srcX, srcY, destX, destY, clipFlags) = params
        val pf = SmartPathFinder(resetOnSearch = true)
        repeat(pathRequests) {
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }
    }

    @Benchmark
    fun serverPathCoroutineDispatcherThreadLocal() = runBlocking {
        val (srcX, srcY, destX, destY, clipFlags) = params
        val threadLocal = ThreadLocal.withInitial { SmartPathFinder(resetOnSearch = true) }

        fun CoroutineScope.findPath() = launch {
            val pf = threadLocal.get()
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }

        launch(scope.coroutineContext) {
            repeat(pathRequests) {
                findPath()
            }
        }.join()
    }

    @Benchmark
    fun serverPathCoroutineDispatcherConstruct() = runBlocking {
        val (srcX, srcY, destX, destY, clipFlags) = params

        fun CoroutineScope.findPath() = launch {
            val pf = SmartPathFinder(resetOnSearch = false)
            pf.findPath(clipFlags, srcX, srcY, destX, destY)
        }

        launch(scope.coroutineContext) {
            repeat(pathRequests) {
                findPath()
            }
        }.join()
    }
}

private data class PathFinderParameter(
    val srcX: Int,
    val srcY: Int,
    val destX: Int,
    val destY: Int,
    val flags: IntArray
) {

    constructor() : this(0, 0, 0, 0, intArrayOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathFinderParameter

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
