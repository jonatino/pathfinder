# pathfinder
[![license][license-badge]][isc]

An efficient [BFS][bfs] path finder.

## Installation

```
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.rsmod:pathfinder:1.1.1")
}
```

## Example

```kotlin
fun smartRoute(srcX: Int, srcY: Int, destX: Int, destY: Int, level: Int): Route {
    val pf = SmartPathFinder()
    val flags = clipFlags(srcX, srcY, level, pf.searchMapSize)
    return pf.findPath(flags, srcX, srcY, destX, destY)
}

fun clipFlags(centerX: Int, centerY: Int, level: Int, size: Int): IntArray {
    val half = size / 2
    val flags = IntArray(size * size)
    val rangeX = centerX - half until centerX + half
    val rangeY = centerY - half until centerY + half
    for (y in rangeY) {
        for (x in rangeX) {
            val coords = Coordinates(x, y, level)
            /*
             * collision map stores tile collision flags for all
             * tiles in the world.
             */
            val flag = collisionMap.get(coords)
            val index = (y * size) + x
            flags[index] = flag
        }
    }
    return flags
}
```

## Performance
**Benchmark sources:** [org/rsmod/pathfinder/benchmarks/][benchmark]

**Machine Specs**
- **OS:** Windows 10 Pro (64-bit)
- **CPU:** Intel Core i7-7700k @ 4.20GHz (4 cores)
- **RAM:** 4 x 16GB DDR4-2132 (1066 MHz)
- **JMH:** 1.25
- **VM:** JDK 11.0.6, Java HotSpot(TM) 64-Bit Server VM, 11.0.6+8-LTS

    ### SmartPathFinder (BFS)
    Each benchmark calculates 2000 paths from short to out-of-bound (beyond search distance) destinations.

    *(These times do not include fetching the collision flags you have to feed into `SmartPathFinder::findPath` as that is up to the end-user to calculate)*
    ```
    Benchmark                                                           Mode  Cnt    Score     Error  Units

    GameClickAltPath.clientPath                                         avgt    3  594.107 ± 179.884  ms/op
    GameClickAltPath.serverPathConstructOnIteration                     avgt    3  561.331 ±   6.033  ms/op
    GameClickAltPath.serverPathResetOnIteration                         avgt    3  555.649 ±  20.237  ms/op
    GameClickAltPath.serverPathCoroutineDispatcherConstruct             avgt    3  114.787 ±  84.132  ms/op
    GameClickAltPath.serverPathCoroutineDispatcherThreadLocal           avgt    3  105.676 ±   0.590  ms/op

    GameClickLongPath.clientPath                                        avgt    3  308.005 ± 126.878  ms/op
    GameClickLongPath.serverPathConstructOnIteration                    avgt    3  294.406 ±  34.127  ms/op
    GameClickLongPath.serverPathResetOnIteration                        avgt    3  285.443 ± 119.789  ms/op
    GameClickLongPath.serverPathCoroutineDispatcherConstruct            avgt    3   58.291 ±  12.354  ms/op
    GameClickLongPath.serverPathCoroutineDispatcherThreadLocal          avgt    3   59.541 ±  41.977  ms/op

    GameClickMedPath.clientPath                                         avgt    3  255.790 ±  44.855  ms/op
    GameClickMedPath.serverPathConstructOnIteration                     avgt    3  241.232 ±  42.122  ms/op
    GameClickMedPath.serverPathResetOnIteration                         avgt    3  231.529 ±   8.699  ms/op
    GameClickMedPath.serverPathCoroutineDispatcherConstruct             avgt    3   50.996 ±  55.673  ms/op
    GameClickMedPath.serverPathCoroutineDispatcherThreadLocal           avgt    3   47.031 ±   5.266  ms/op

    GameClickShortPath.clientPath                                       avgt    3   11.667 ±  11.809  ms/op
    GameClickShortPath.serverPathConstructOnIteration                   avgt    3   15.909 ±   2.177  ms/op
    GameClickShortPath.serverPathResetOnIteration                       avgt    3    6.262 ±   0.170  ms/op
    GameClickShortPath.serverPathCoroutineDispatcherConstruct           avgt    3   12.035 ±  10.571  ms/op
    GameClickShortPath.serverPathCoroutineDispatcherThreadLocal         avgt    3    1.922 ±   0.116  ms/op
    ```
    #### Glossary
    - **GameClickAltPath**: destination outside of valid search distance (path finder forced to iterate the whole search area) (~72 tiles).
    - **GameClickLongPath**: destination near upper limit of `SmartPathFinder::searchMapSize` radius (~63 tiles).
    - **GameClickMedPath**: destination about half of `SmartPathFinder::searchMapSize` radius (~32 tiles).
    - **GameClickShortPath**: destination near lower limit of `SmartPathFinder::searchMapSize` radius (~8 tiles).
    - **clientPath**: simple zero-allocation third-party implementation.
    - **serverPathConstructOnIteration**: construct a new `SmartPathFinder` for every iteration.
    - **serverPathResetOnIteration**: reset values on same `SmartPathFinder` instance to re-use every iteration.
    - **serverPathCoroutineDispatcherConstruct**: similar to `serverPathConstructOnIteration`, but using coroutines for each iteration.
    - **serverPathCoroutineDispatcherThreadLocal**: similar to `serverPathCoroutineDispatcherConstruct`, but uses `ThreadLocal` instead of always constructing a new `SmartPathFinder` instance per iteration.

    ### DumbPathFinder
    Each benchmark calculates 32767 (`Short.MAX_VALUE`) paths without any interruptions (empty collision flags used).
    Though this data is not as useful, it provides a baseline of the cost for each steps' directional collision check.

    *(As with the previous benchmark, these times do not include fetching the collision flags you have to feed into `DumbPathFinder::findPath`)*
    ```
    Benchmark                                                Mode  Cnt   Score    Error  Units

    DumbPathFinderBenchmark.uninterruptedMaxDistance         avgt    3  30.196 ± 23.126  ms/op
    ```
    #### Glossary
    - **uninterruptedMaxDistance**: destination reaches limit of `DumbPathFinder::searchMapSize` radius (~63 tiles).

## Contributing
Pull requests are welcome on [GitHub][github].

## License
This project is available under the terms of the ISC license, which is similar to the 2-clause BSD license. The full copyright notice and terms are available in the [LICENSE][license] file.

[isc]: https://opensource.org/licenses/ISC
[license]: https://github.com/rsmod/pathfinder/blob/master/LICENSE.md
[license-badge]: https://img.shields.io/badge/license-ISC-informational
[bfs]: https://en.wikipedia.org/wiki/Breadth-first_search
[github]: https://github.com/rsmod/pathfinder
[benchmark]: https://github.com/rsmod/pathfinder/blob/master/src/jmh/kotlin/org/rsmod/pathfinder/benchmarks
