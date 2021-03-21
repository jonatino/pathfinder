package org.rsmod.pathfinder.benchmarks

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
import org.rsmod.pathfinder.DumbPathFinder
import org.rsmod.pathfinder.RouteCoordinates
import java.util.concurrent.TimeUnit

private const val SEARCH_SIZE = 128

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
@Fork(value = 1)
open class DumbPathFinderBenchmark(
    private val pathRequests: Int = Short.MAX_VALUE.toInt()
) {

    private lateinit var flags: IntArray

    @Setup
    fun setup() {
        flags = IntArray(SEARCH_SIZE * SEARCH_SIZE)
    }

    @Benchmark
    fun uninterruptedMaxDistance() {
        val pf = DumbPathFinder(SEARCH_SIZE)
        val src = RouteCoordinates(3200, 3200)
        val dest = src.translate(63, 63)
        repeat(pathRequests) {
            pf.findPath(flags, src.x, src.y, dest.x, dest.y)
        }
    }
}
