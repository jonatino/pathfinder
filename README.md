# pathfinder
[![license][license-badge]][isc]

An efficient [BFS][bfs] path finder.

## Installation

```
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.rsmod:pathfinder:1.0.8")
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

## Contributing
Pull requests are welcome on [GitHub](https://github.com/rsmod/pathfinder).

## License
This project is available under the terms of the ISC license, which is similar to the 2-clause BSD license. The full copyright notice and terms are available in the [LICENSE][license] file.

[isc]: https://opensource.org/licenses/ISC
[license]: https://github.com/rsmod/pathfinder/blob/master/LICENSE.md
[license-badge]: https://img.shields.io/badge/license-ISC-informational
[bfs]: https://en.wikipedia.org/wiki/Breadth-first_search
