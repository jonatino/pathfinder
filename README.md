# pathfinder
[![license][license-badge]][isc]

An efficient [BFS][bfs] path finder.

## Installation

```
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.rsmod:pathfinder:1.0.9")
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
    val rangeX = centerX - half until center.x + half
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
* **OS**: Windows 10 Pro (64-bit)
* **CPU**: Intel Core i7-7700k @ 4.20GHz (4 cores)
* **RAM**: 4 x 16GB DDR4-2132 (1066 MHz)
* **JMH**: 1.25
* **VM**: JDK 11.0.6, Java HotSpot(TM) 64-Bit Server VM, 11.0.6+8-LTS

    ### Smart Path Finder (BFS)
    Each benchmark calculates 2000 paths from short to out-of-bound (beyond search distance) destinations.
    ```
    Benchmark                                                           Mode  Cnt    Score     Error  Units

    GameClickAltPath.clientPath                                         avgt    3  624.062 ± 111.069  ms/op
    GameClickAltPath.serverPathConstructOnIteration                     avgt    3  543.000 ±  35.677  ms/op
    GameClickAltPath.serverPathResetOnIteration                         avgt    3  536.212 ±  87.506  ms/op
    GameClickAltPath.serverPathCoroutineDispatcherConstruct             avgt    3  113.669 ±  50.609  ms/op
    GameClickAltPath.serverPathCoroutineDispatcherThreadLocal           avgt    3  110.522 ±  74.650  ms/op

    GameClickLongPath.clientPath                                        avgt    3  312.648 ±  97.070  ms/op
    GameClickLongPath.serverPathConstructOnIteration                    avgt    3  284.130 ±  16.656  ms/op
    GameClickLongPath.serverPathResetOnIteration                        avgt    3  274.803 ±   8.607  ms/op
    GameClickLongPath.serverPathCoroutineDispatcherConstruct            avgt    3   59.556 ±  32.583  ms/op
    GameClickLongPath.serverPathCoroutineDispatcherThreadLocal          avgt    3   61.593 ±  74.289  ms/op

    GameClickMedPath.clientPath                                         avgt    3  252.644 ±  39.104  ms/op
    GameClickMedPath.serverPathConstructOnIteration                     avgt    3  234.207 ±  10.811  ms/op
    GameClickMedPath.serverPathResetOnIteration                         avgt    3  224.111 ±  18.178  ms/op
    GameClickMedPath.serverPathCoroutineDispatcherConstruct             avgt    3   50.231 ±  13.068  ms/op
    GameClickMedPath.serverPathCoroutineDispatcherThreadLocal           avgt    3   49.616 ±  37.505  ms/op

    GameClickShortPath.clientPath                                       avgt    3   11.663 ±  15.309  ms/op
    GameClickShortPath.serverPathConstructOnIteration                   avgt    3   15.547 ±   1.233  ms/op
    GameClickShortPath.serverPathResetOnIteration                       avgt    3    6.223 ±   0.809  ms/op
    GameClickShortPath.serverPathCoroutineDispatcherConstruct           avgt    3   12.505 ±   3.163  ms/op
    GameClickShortPath.serverPathCoroutineDispatcherThreadLocal         avgt    3    2.065 ±   0.980  ms/op
    ```
    #### Glossary
    * **GameClickAltPath**: destination outside of valid search distance (path finder force to iterate the whole search area) (~72 tiles).
    * **GameClickLongPath**: destination near upper limit of `SmartPathFinder::searchMapSize` radius (~63 tiles).
    * **GameClickMedPath**: destination about half of `SmartPathFinder::searchMapSize` radius (~32 tiles).
    * **GameClickShortPath**: destination near lower limit of `SmartPathFinder::searchMapSize` radius (~8 tiles)
    * **clientPath**: simple zero-allocation third-party implementation.
    * **serverPathConstructOnIteration**: construct `SmartPathFinder` every iteration.
    * **serverPathResetOnIteration**: reset values on `SmartPathFinder` to re-use every iteration.
    * **serverPathCoroutineDispatcherConstruct**: similar to `serverPathConstructOnIteration`, but using coroutines for each iteration.
    * **serverPathCoroutineDispatcherThreadLocal**: similar to `serverPathConstructOnIteration`, but uses `ThreadLocal` instead of always constructing a new instance of `SmartPathFinder`.

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
