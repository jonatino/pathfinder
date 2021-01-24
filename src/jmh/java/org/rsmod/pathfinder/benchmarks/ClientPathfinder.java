package org.rsmod.pathfinder.benchmarks;

/**
 * @author Kris | 08/01/2021
 */
@SuppressWarnings("unused")
public class ClientPathfinder {

    public static final int SIZE = 128;

    public static int[][] directions = new int[SIZE][SIZE];
    public static int[][] distances = new int[SIZE][SIZE];
    public static int[] writeBufferX = new int[4096];
    public static int[] writeBufferY = new int[4096];
    public static ClientMapArea[] scene = new ClientMapArea[4];
    public static int level;
    public static boolean alternativeRoute;

    public static boolean findPath(
            int rotation,
            boolean findClosestApproachPoint,
            int approxDestY,
            int approxDestinationSizeY,
            int srcY,
            int returnTypeConditionalInt,
            int accessMask,
            int approxDestinationSizeX,
            int type,
            boolean check,
            int srcX,
            int approxDestX,
            final int size
    ) {
        //anInt3527++;
        if (!check) return true;
        if (size == 1) {
            //Order to: SrcX, SrcY, DestX, DestY, DestSizeX, DestSizeY, Rotation, Type, AccessMask, findClosestApproachPoint, ReturnTypeConditionalInt
            return findPath1(srcX, srcY, approxDestX, approxDestY, approxDestinationSizeX, approxDestinationSizeY, rotation, type, accessMask, findClosestApproachPoint, returnTypeConditionalInt);
        }
        if (size == 2) {
            return findPath2(srcX, srcY, approxDestX, approxDestY, approxDestinationSizeX, approxDestinationSizeY, rotation, type, accessMask, findClosestApproachPoint, returnTypeConditionalInt);
        }
        return findPathN(srcX, srcY, approxDestX, approxDestY, approxDestinationSizeX, approxDestinationSizeY, rotation, type, accessMask, findClosestApproachPoint, returnTypeConditionalInt, size);
    }

    public static boolean findPath1(
            int srcX, int srcY,
            int destX, int destY,
            int approxDestinationSizeX, int approxDestinationSizeY,
            int rotation,
            int type,
            int accessMask,
            boolean findClosestApproachPoint,
            int returnTypeConditionalInt
    ) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                directions[x][y] = 0;
                distances[x][y] = 99999999;
            }
        }
        final int graphBaseX = srcX - (SIZE / 2);
        final int graphBaseY = srcY - (SIZE / 2);
        srcX -= graphBaseX;
        srcY -= graphBaseY;
        destX -= graphBaseX;
        destY -= graphBaseY;
        //if (i_5_ >= -100) method717(-69);
        directions[srcX][srcY] = 99;
        distances[srcX][srcY] = 0;
        int write = 0;
        int read = 0;
        int currentY = srcY;
        writeBufferX[write] = srcX;
        int currentX = srcX;
        boolean found = false;
        writeBufferY[write++] = srcY;
        int[][] clipMasks = scene[level].flags;
        int relativeSize = SIZE - 1;
        while (write != read) {
            currentY = writeBufferY[read];
            currentX = writeBufferX[read];
            read = 0xfff & 1 + read;
            if (currentX == destX && destY == currentY) {
                found = true;
                break;
            }

            if (type != 0) {
                if (type >= 5 && type != 10 || !(ClientMapArea.checkWallInteract(scene[level].flags, 1, destY, type + -1, currentX, rotation, true, currentY, destX))) {
                    if (type < 10 && (ClientMapArea.checkWallDecorationInteract(scene[level].flags, currentX, 1, currentY, rotation, destY, destX, -1 + type, true))) {
                        found = true;
                        break;
                    }
                } else {
                    found = true;
                    break;
                }
            }
            if (approxDestinationSizeX != 0 && approxDestinationSizeY != 0 && (scene[level].canExit(approxDestinationSizeY, currentX, accessMask, destY, destX, 1, approxDestinationSizeX, currentY))) {
                found = true;
                break;
            }
            int nextDistance = 1 + distances[currentX][currentY];
            if (currentX > 0 && directions[-1 + currentX][currentY] == 0 && (clipMasks[currentX - 1][currentY] & 0x12c0108) == 0) {
                writeBufferX[write] = currentX + -1;
                writeBufferY[write] = currentY;
                write = write + 1 & 0xfff;
                directions[-1 + currentX][currentY] = 2;
                distances[-1 + currentX][currentY] = nextDistance;
            }
            if (currentX < relativeSize && directions[currentX + 1][currentY] == 0
                    && (0x12c0180 & clipMasks[1 + currentX][currentY]) == 0) {
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = currentY;
                directions[currentX - -1][currentY] = 8;
                write = 0xfff & write + 1;
                distances[1 + currentX][currentY] = nextDistance;
            }
            if (currentY > 0 && directions[currentX][currentY - 1] == 0 && (clipMasks[currentX][currentY - 1] & 0x12c0102) == 0) {
                writeBufferX[write] = currentX;
                writeBufferY[write] = currentY + -1;
                directions[currentX][currentY + -1] = 1;
                distances[currentX][-1 + currentY] = nextDistance;
                write = 0xfff & write - -1;
            }
            if (currentY < relativeSize && directions[currentX][currentY - -1] == 0 && (0x12c0120 & clipMasks[currentX][1 + currentY]) == 0) {
                writeBufferX[write] = currentX;
                writeBufferY[write] = 1 + currentY;
                write = 0xfff & write - -1;
                directions[currentX][1 + currentY] = 4;
                distances[currentX][1 + currentY] = nextDistance;
            }
            if (currentX > 0 && currentY > 0 && directions[-1 + currentX][-1 + currentY] == 0 && (0x12c010e & clipMasks[-1 + currentX][-1 + currentY]) == 0
                    && (clipMasks[currentX + -1][currentY] & 0x12c0108) == 0 && (0x12c0102 & clipMasks[currentX][-1 + currentY]) == 0) {
                writeBufferX[write] = currentX + -1;
                writeBufferY[write] = -1 + currentY;
                directions[currentX - 1][-1 + currentY] = 3;
                distances[currentX - 1][currentY + -1] = nextDistance;
                write = 0xfff & 1 + write;
            }
            if (currentX < relativeSize && currentY > 0 && directions[currentX + 1][-1 + currentY] == 0 && (clipMasks[1 + currentX][-1 + currentY] & 0x12c0183) == 0
                    && (0x12c0180 & clipMasks[currentX - -1][currentY]) == 0 && (0x12c0102 & clipMasks[currentX][-1 + currentY]) == 0) {
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = currentY - 1;
                write = write - -1 & 0xfff;
                directions[currentX - -1][currentY + -1] = 9;
                distances[currentX + 1][currentY + -1] = nextDistance;
            }

            if (currentX > 0 && currentY < relativeSize && directions[currentX - 1][currentY + 1] == 0 && (0x12c0138 & clipMasks[-1 + currentX][currentY - -1]) == 0
                    && (0x12c0108 & clipMasks[currentX + -1][currentY]) == 0 && (0x12c0120 & clipMasks[currentX][1 + currentY]) == 0) {
                writeBufferX[write] = -1 + currentX;
                writeBufferY[write] = 1 + currentY;
                write = write - -1 & 0xfff;
                directions[-1 + currentX][1 + currentY] = 6;
                distances[-1 + currentX][1 + currentY] = nextDistance;
            }

            if (currentX < relativeSize && currentY < relativeSize && directions[currentX - -1][currentY - -1] == 0 && (clipMasks[currentX - -1][currentY - -1] & 0x12c01e0) == 0
                    && (clipMasks[1 + currentX][currentY] & 0x12c0180) == 0 && (0x12c0120 & clipMasks[currentX][1 + currentY]) == 0) {
                writeBufferX[write] = currentX - -1;
                writeBufferY[write] = 1 + currentY;
                directions[1 + currentX][currentY - -1] = 12;
                distances[1 + currentX][currentY + 1] = nextDistance;
                write = 1 + write & 0xfff;
            }
        }
        alternativeRoute = false;
        if (!found) {
            if (findClosestApproachPoint) {
                int lowestCost = 1000;
                int maxAlternativePath = 100;
                int alternativeRouteRange = 10;
                for (int x = destX - alternativeRouteRange; x <= destX + alternativeRouteRange; x++) {
                    for (int y = destY - alternativeRouteRange; y <= destY + alternativeRouteRange; y++) {
                        if (x >= 0 && y >= 0 && x < SIZE && y < SIZE && distances[x][y] < 100) {
                            int dx = 0;
                            int dy = 0;
                            if (x >= destX) {
                                if (approxDestinationSizeX + (destX - 1) < x) dy = x - (-1 + approxDestinationSizeX) - destX;
                            } else dy = -x + destX;
                            if (destY > y) dx = -y + destY;
                            else if (approxDestinationSizeY + (destY + -1) < y) dx = 1 + -destY - approxDestinationSizeY + y;
                            int cost = dx * dx + dy * dy;
                            if (cost < lowestCost || (cost == lowestCost && (maxAlternativePath > distances[x][y]))) {
                                currentX = x;
                                lowestCost = cost;
                                maxAlternativePath = (distances[x][y]);
                                currentY = y;
                            }
                        }
                    }
                }
                if (lowestCost == 1000) return false;
                if (srcX == currentX && currentY == srcY) return false;
                alternativeRoute = true;
            } else return false;
        }
        read = 0;
        writeBufferX[read] = currentX + graphBaseX;
        writeBufferY[read++] = currentY + graphBaseY;
        int currentDirection;
        int nextDirection = currentDirection = directions[currentX][currentY];
        while (srcX != currentX || currentY != srcY) {
            if (currentDirection != nextDirection) {
                writeBufferX[read] = currentX + graphBaseX;
                currentDirection = nextDirection;
                writeBufferY[read++] = currentY + graphBaseY;
            }
            if ((0x2 & nextDirection) == 0) {
                if ((0x8 & nextDirection) != 0)
                    currentX--;
            } else
                currentX++;
            if ((0x1 & nextDirection) != 0)
                currentY++;
            else if ((nextDirection & 0x4) != 0)
                currentY--;

            nextDirection = directions[currentX][currentY];
        }
        if (read > 0) {
            //Class100.method1570(i_13_, i, (byte) 55);
            return true;
        }
        return returnTypeConditionalInt != 1;
    }

    public static boolean findPath2(
            int srcX, int srcY,
            int approxDestX, int approxDestY,
            int approxDestinationSizeX, int approxDestinationSizeY,
            int rotation,
            int type,
            int accessMask,
            boolean findClosestApproachPoint,
            int returnTypeConditionalInt
    ) {
        for (int i_11_ = 0; i_11_ < SIZE; i_11_++) {
            for (int i_12_ = 0; i_12_ < SIZE; i_12_++) {
                directions[i_11_][i_12_] = 0;
                distances[i_11_][i_12_] = 99999999;
            }
        }
        directions[srcX][srcY] = 99;
        int currentY = srcY;
        distances[srcX][srcY] = 0;
        int write = 0;
        writeBufferX[write] = srcX;
        int i_15_ = 0;
        writeBufferY[write++] = srcY;
        int currentX = srcX;
        boolean bool_17_ = false;
        int[][] flags = scene[level].flags;
        int relativeSize = SIZE - 2;
        while (write != i_15_) {
            currentX = writeBufferX[i_15_];
            currentY = writeBufferY[i_15_];
            i_15_ = 1 + i_15_ & 0xfff;
            if (approxDestX == currentX && approxDestY == currentY) {
                bool_17_ = true;
                break;
            }
            if (type != 0) {
                if ((type < 5 || type == 10) && (ClientMapArea.checkWallInteract(scene[level].flags, 2, approxDestY, type + -1, currentX, rotation, true, currentY, approxDestX))) {
                    bool_17_ = true;
                    break;
                }
                if (type < 10 && (ClientMapArea.checkWallDecorationInteract(scene[level].flags, currentX, 2, currentY, rotation, approxDestY, approxDestX, -1 + type, true))) {
                    bool_17_ = true;
                    break;
                }
            }
            if (approxDestinationSizeX != 0 && approxDestinationSizeY != 0 && (scene[level].canExit(approxDestinationSizeY, currentX, accessMask, approxDestY, approxDestX, 2, approxDestinationSizeX, currentY))) {
                bool_17_ = true;
                break;
            }
            int nextDistance = distances[currentX][currentY] - -1;

            if (currentX > 0 && directions[-1 + currentX][currentY] == 0 && (0x12c010e & flags[-1 + currentX][currentY]) == 0 && (0x12c0138 & flags[currentX - 1][1 + currentY]) == 0) {
                writeBufferX[write] = -1 + currentX;
                writeBufferY[write] = currentY;
                write = write + 1 & 0xfff;
                directions[-1 + currentX][currentY] = 2;
                distances[currentX + -1][currentY] = nextDistance;
            }
            if (currentX < relativeSize && directions[currentX - -1][currentY] == 0 && (flags[currentX - -2][currentY] & 0x12c0183) == 0 && ((0x12c01e0 & flags[currentX - -2][currentY - -1]) == 0)) {
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = currentY;
                write = 1 + write & 0xfff;
                directions[currentX + 1][currentY] = 8;
                distances[currentX - -1][currentY] = nextDistance;
            }
            if (currentY > 0 && directions[currentX][currentY - 1] == 0 && (0x12c010e & flags[currentX][-1 + currentY]) == 0 && ((flags[1 + currentX][-1 + currentY] & 0x12c0183) == 0)) {
                writeBufferX[write] = currentX;
                writeBufferY[write] = -1 + currentY;
                directions[currentX][-1 + currentY] = 1;
                distances[currentX][-1 + currentY] = nextDistance;
                write = 0xfff & write - -1;
            }
            if (currentY < relativeSize && directions[currentX][1 + currentY] == 0 && (0x12c0138 & flags[currentX][currentY + 2]) == 0 && (0x12c01e0 & flags[1 + currentX][currentY + 2]) == 0) {
                writeBufferX[write] = currentX;
                writeBufferY[write] = currentY - -1;
                write = write + 1 & 0xfff;
                directions[currentX][1 + currentY] = 4;
                distances[currentX][1 + currentY] = nextDistance;
            }
            if (currentX > 0 && currentY > 0 && directions[-1 + currentX][currentY + -1] == 0
                    && (0x12c0138 & flags[currentX - 1][currentY]) == 0 && (flags[-1 + currentX][-1 + currentY] & 0x12c010e) == 0 && (flags[currentX][currentY - 1] & 0x12c0183) == 0) {
                writeBufferX[write] = currentX - 1;
                writeBufferY[write] = currentY + -1;
                write = 0xfff & 1 + write;
                directions[currentX - 1][currentY - 1] = 3;
                distances[-1 + currentX][currentY + -1] = nextDistance;
            }
            if (currentX < relativeSize && currentY > 0 && directions[1 + currentX][-1 + currentY] == 0 && (flags[1 + currentX][currentY - 1] & 0x12c010e) == 0
                    && (0x12c0183 & flags[2 + currentX][-1 + currentY]) == 0 && (flags[currentX - -2][currentY] & 0x12c01e0) == 0) {
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = currentY + -1;
                write = 0xfff & write + 1;
                directions[currentX - -1][currentY - 1] = 9;
                distances[currentX + 1][-1 + currentY] = nextDistance;
            }
            if (currentX > 0 && currentY < relativeSize && directions[-1 + currentX][currentY + 1] == 0 && (flags[-1 + currentX][1 + currentY] & 0x12c010e) == 0
                    && (0x12c0138 & flags[-1 + currentX][currentY - -2]) == 0 && (0x12c01e0 & flags[currentX][2 + currentY]) == 0) {
                writeBufferX[write] = currentX - 1;
                writeBufferY[write] = 1 + currentY;
                write = 1 + write & 0xfff;
                directions[-1 + currentX][currentY - -1] = 6;
                distances[-1 + currentX][1 + currentY] = nextDistance;
            }
            if (currentX < relativeSize && currentY < relativeSize && directions[1 + currentX][currentY + 1] == 0 && (0x12c0138 & flags[currentX + 1][currentY - -2]) == 0
                    && (flags[currentX + 2][currentY - -2] & 0x12c01e0) == 0 && (flags[2 + currentX][1 + currentY] & 0x12c0183) == 0) {
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = 1 + currentY;
                write = 0xfff & 1 + write;
                directions[currentX + 1][1 + currentY] = 12;
                distances[currentX + 1][1 + currentY] = nextDistance;
            }
        }
        alternativeRoute = false;
        if (!bool_17_) {
            if (!findClosestApproachPoint) return false;
            int i_19_ = 1000;
            int i_20_ = 100;
            int i_21_ = 10;
            for (int i_22_ = -i_21_ + approxDestX; approxDestX + i_21_ >= i_22_; i_22_++) {
                for (int i_23_ = approxDestY + -i_21_; i_23_ <= i_21_ + approxDestY; i_23_++) {
                    if (i_22_ >= 0 && i_23_ >= 0 && i_22_ < SIZE && i_23_ < SIZE && (distances[i_22_][i_23_] < 100)) {
                        int i_24_ = 0;
                        if (i_22_ >= approxDestX) {
                            if (approxDestX + (approxDestinationSizeX + -1) < i_22_) i_24_ = i_22_ - (-1 + (approxDestinationSizeX + approxDestX));
                        } else i_24_ = approxDestX + -i_22_;
                        int i_25_ = 0;
                        if (i_23_ < approxDestY) i_25_ = -i_23_ + approxDestY;
                        else if (-1 + approxDestinationSizeY + approxDestY < i_23_) i_25_ = -approxDestinationSizeY - approxDestY - (-1 - i_23_);
                        int i_26_ = i_24_ * i_24_ - -(i_25_ * i_25_);
                        if (i_26_ < i_19_ || (i_19_ == i_26_ && i_20_ > distances[i_22_][i_23_])) {
                            i_19_ = i_26_;
                            i_20_ = (distances[i_22_][i_23_]);
                            currentY = i_23_;
                            currentX = i_22_;
                        }
                    }
                }
            }
            if (i_19_ == 1000) return false;
            if (srcX == currentX && currentY == srcY) return false;
            alternativeRoute = true;
        }
        i_15_ = 0;
        writeBufferX[i_15_] = currentX;
        writeBufferY[i_15_++] = currentY;
        int i_28_;
        int i_27_ = i_28_ = directions[currentX][currentY];
        while (srcX != currentX || srcY != currentY) {
            if (i_27_ != i_28_) {
                i_28_ = i_27_;
                writeBufferX[i_15_] = currentX;
                writeBufferY[i_15_++] = currentY;
            }
            if ((i_27_ & 0x2) == 0) {
                if ((i_27_ & 0x8) != 0) currentX--;
            } else currentX++;
            if ((0x1 & i_27_) != 0) currentY++;
            else if ((i_27_ & 0x4) != 0) currentY--;
            i_27_ = directions[currentX][currentY];
        }
        if (i_15_ > 0) {
            //Class100.method1570(i_15_, i_4_, (byte) 55);
            return true;
        }
        return returnTypeConditionalInt != 1;
    }

    public static boolean findPathN(int srcX, int srcY, int approxDestX, int approxDestY, int approxDestinationSizeX, int approxDestinationSizeY, int rotation, int type, int accessMask, boolean findClosestApproachPoint, int returnTypeConditionalInt, int size) {
        for (int i_92_ = 0; i_92_ < SIZE; i_92_++) {
            for (int i_93_ = 0; i_93_ < SIZE; i_93_++) {
                directions[i_92_][i_93_] = 0;
                distances[i_92_][i_93_] = 99999999;
            }
        }
        directions[srcX][srcY] = 99;
        int currentX = srcX;
        distances[srcX][srcY] = 0;
        int currentY = srcY;
        int write = 0;
        int read = 0;
        boolean bool_98_ = false;
        writeBufferX[write] = srcX;
        writeBufferY[write++] = srcY;
        int[][] flags = scene[level].flags;
        int relativeSize = SIZE - size;
        while_36_:
        while (write != read) {
            currentY = writeBufferY[read];
            currentX = writeBufferX[read];
            read = 1 + read & 0xfff;
            if (currentX == approxDestX && currentY == approxDestY) {
                bool_98_ = true;
                break;
            }
            if (type != 0) {
                if ((type < 5 || type == 10) && (ClientMapArea.checkWallInteract(scene[level].flags, size, approxDestY, type + -1, currentX, rotation, true, currentY, approxDestX))) {
                    bool_98_ = true;
                    break;
                }
                if (type < 10 && (ClientMapArea.checkWallDecorationInteract(scene[level].flags, currentX, size, currentY, rotation, approxDestY, approxDestX, type - 1, true))) {
                    bool_98_ = true;
                    break;
                }
            }
            if (approxDestinationSizeX != 0 && approxDestinationSizeY != 0 && (scene[level].canExit(approxDestinationSizeY, currentX, accessMask, approxDestY, approxDestX, size, approxDestinationSizeX, currentY))) {
                bool_98_ = true;
                break;
            }
            int nextDistance = distances[currentX][currentY] + 1;
            while_29_:
            do {
                if (currentX > 0 && directions[-1 + currentX][currentY] == 0
                        && (flags[-1 + currentX][currentY] & 0x12c010e) == 0
                        && ((flags[-1 + currentX][-1 + (currentY - -size)] & 0x12c0138) == 0)) {
                    for (int i_100_ = 1; i_100_ < size - 1; i_100_++) {
                        if ((0x12c013e & flags[currentX + -1][currentY - -i_100_]) != 0) break while_29_;
                    }
                    writeBufferX[write] = -1 + currentX;
                    writeBufferY[write] = currentY;
                    directions[-1 + currentX][currentY] = 2;
                    write = write - -1 & 0xfff;
                    distances[-1 + currentX][currentY] = nextDistance;
                }
            } while (false);
            while_30_:
            do {
                if (currentX < relativeSize && directions[currentX + 1][currentY] == 0
                        && (0x12c0183 & flags[size + currentX][currentY]) == 0 && ((0x12c01e0 & flags[currentX + size][-1 + currentY + size]) == 0)) {
                    for (int i_101_ = 1; i_101_ < -1 + size; i_101_++) {
                        if ((0x12c01e3 & flags[currentX - -size][currentY + i_101_]) != 0) break while_30_;
                    }
                    writeBufferX[write] = 1 + currentX;
                    writeBufferY[write] = currentY;
                    directions[currentX + 1][currentY] = 8;
                    distances[currentX + 1][currentY] = nextDistance;
                    write = write + 1 & 0xfff;
                }
            } while (false);
            while_31_:
            do {
                if (currentY > 0 && directions[currentX][-1 + currentY] == 0
                        && (flags[currentX][currentY - 1] & 0x12c010e) == 0
                        && (0x12c0183 & flags[currentX - (-size - -1)][currentY - 1]) == 0) {
                    for (int i_102_ = 1; i_102_ < size + -1; i_102_++) {
                        if ((flags[currentX - -i_102_][-1 + currentY] & 0x12c018f) != 0) break while_31_;
                    }
                    writeBufferX[write] = currentX;
                    writeBufferY[write] = currentY + -1;
                    directions[currentX][-1 + currentY] = 1;
                    write = 0xfff & write + 1;
                    distances[currentX][currentY - 1] = nextDistance;
                }
            } while (false);
            while_32_:
            do {
                if (currentY < relativeSize && directions[currentX][1 + currentY] == 0
                        && (0x12c0138 & flags[currentX][size + currentY]) == 0
                        && ((flags[size + currentX + -1][size + currentY] & 0x12c01e0) == 0)) {
                    for (int i_103_ = 1; -1 + size > i_103_; i_103_++) {
                        if ((flags[currentX - -i_103_][size + currentY] & 0x12c01f8) != 0) break while_32_;
                    }
                    writeBufferX[write] = currentX;
                    writeBufferY[write] = 1 + currentY;
                    write = write + 1 & 0xfff;
                    directions[currentX][currentY + 1] = 4;
                    distances[currentX][1 + currentY] = nextDistance;
                }
            } while (false);
            while_33_:
            do {
                if (currentX > 0 && currentY > 0 && directions[currentX - 1][-1 + currentY] == 0
                        && (0x12c0138 & flags[-1 + currentX][currentY + size - 1 - 1]) == 0
                        && (0x12c010e & flags[-1 + currentX][-1 + currentY]) == 0
                        && (0x12c0183 & flags[size + currentX - 1 - 1][-1 + currentY]) == 0) {
                    for (int i_104_ = 1; i_104_ < size + -1; i_104_++) {
                        if (((flags[currentX + -1][currentY - (1 - i_104_)] & 0x12c013e) != 0) || (flags[i_104_ + -1 + currentX][currentY - 1] & 0x12c018f) != 0) break while_33_;
                    }
                    writeBufferX[write] = -1 + currentX;
                    writeBufferY[write] = -1 + currentY;
                    directions[currentX + -1][-1 + currentY] = 3;
                    distances[-1 + currentX][currentY + -1] = nextDistance;
                    write = 0xfff & write - -1;
                }
            } while (false);
            while_34_:
            do {
                if (currentX < relativeSize && currentY > 0 && directions[currentX + 1][currentY - 1] == 0
                        && ((0x12c010e & flags[1 + currentX][currentY - 1]) == 0)
                        && (flags[size + currentX][-1 + currentY] & 0x12c0183) == 0
                        && ((flags[size + currentX][currentY - 1 + size - 1] & 0x12c01e0) == 0)) {
                    for (int i_105_ = 1; i_105_ < -1 + size; i_105_++) {
                        if ((flags[currentX + size][i_105_ + -1 + currentY] & 0x12c01e3) != 0 || (0x12c018f & flags[currentX + 1 + i_105_][-1 + currentY]) != 0) break while_34_;
                    }
                    writeBufferX[write] = currentX + 1;
                    writeBufferY[write] = currentY - 1;
                    write = 0xfff & 1 + write;
                    directions[1 + currentX][currentY + -1] = 9;
                    distances[1 + currentX][-1 + currentY] = nextDistance;
                }
            } while (false);
            while_35_:
            do {
                if (currentX > 0 && currentY < relativeSize && directions[currentX + -1][1 + currentY] == 0
                        && (0x12c010e & flags[currentX + -1][currentY + 1]) == 0
                        && ((0x12c0138 & flags[currentX - 1][currentY + size]) == 0)
                        && (0x12c01e0 & flags[currentX][currentY + size]) == 0) {
                    for (int i_106_ = 1; i_106_ < size + -1; i_106_++) {
                        if ((flags[currentX - 1][i_106_ + (1 + currentY)] & 0x12c013e) != 0 || ((0x12c01f8 & flags[i_106_ + (currentX + -1)][currentY - -size]) != 0)) break while_35_;
                    }
                    writeBufferX[write] = currentX + -1;
                    writeBufferY[write] = currentY + 1;
                    directions[currentX - 1][currentY - -1] = 6;
                    write = 0xfff & 1 + write;
                    distances[-1 + currentX][currentY + 1] = nextDistance;
                }
            } while (false);
            if (currentX < relativeSize && currentY < relativeSize && directions[1 + currentX][currentY + 1] == 0
                    && (0x12c0138 & flags[currentX - -1][size + currentY]) == 0
                    && (flags[currentX - -size][size + currentY] & 0x12c01e0) == 0
                    && ((flags[currentX - -size][1 + currentY] & 0x12c0183) == 0)) {
                for (int i_107_ = 1; i_107_ < size + -1; i_107_++) {
                    if ((flags[i_107_ + (1 + currentX)][currentY - -size] & 0x12c01f8) != 0 || (flags[size + currentX][i_107_ + (1 + currentY)] & 0x12c01e3) != 0) continue while_36_;
                }
                writeBufferX[write] = 1 + currentX;
                writeBufferY[write] = 1 + currentY;
                write = write - -1 & 0xfff;
                directions[1 + currentX][currentY - -1] = 12;
                distances[1 + currentX][currentY + 1] = nextDistance;
            }
        }
        alternativeRoute = false;
        if (!bool_98_) {
            if (!findClosestApproachPoint) return false;
            int i_108_ = 1000;
            int i_109_ = 100;
            int i_110_ = 10;
            for (int i_111_ = -i_110_ + approxDestX; i_111_ <= approxDestX + i_110_; i_111_++) {
                for (int i_112_ = -i_110_ + approxDestY; approxDestY + i_110_ >= i_112_; i_112_++) {
                    if (i_111_ >= 0 && i_112_ >= 0 && i_111_ < SIZE && i_112_ < SIZE && (distances[i_111_][i_112_] < 100)) {
                        int i_113_ = 0;
                        int i_114_ = 0;
                        if (i_111_ >= approxDestX) {
                            if (-1 + approxDestinationSizeX + approxDestX < i_111_) i_113_ = i_111_ + -approxDestX - approxDestinationSizeX - -1;
                        } else i_113_ = approxDestX + -i_111_;
                        if (approxDestY <= i_112_) {
                            if (i_112_ > approxDestY + (approxDestinationSizeY + -1)) i_114_ = i_112_ + -approxDestY - (approxDestinationSizeY + -1);
                        } else i_114_ = approxDestY + -i_112_;
                        int i_115_ = i_113_ * i_113_ + i_114_ * i_114_;
                        if (i_108_ > i_115_ || (i_115_ == i_108_ && distances[i_111_][i_112_] < i_109_)) {
                            i_108_ = i_115_;
                            currentY = i_112_;
                            i_109_ = (distances[i_111_][i_112_]);
                            currentX = i_111_;
                        }
                    }
                }
            }
            if (i_108_ == 1000) return false;
            if (currentX == srcX && srcY == currentY) return false;
            alternativeRoute = true;
        }
        read = 0;
        writeBufferX[read] = currentX;
        writeBufferY[read++] = currentY;
        int i_117_;
        int i_116_ = i_117_ = directions[currentX][currentY];
        //int i_118_ = 4 % ((-4 - i_87_) / 43);
        while (currentX != srcX || currentY != srcY) {
            if (i_117_ != i_116_) {
                i_117_ = i_116_;
                writeBufferX[read] = currentX;
                writeBufferY[read++] = currentY;
            }
            if ((i_116_ & 0x1) != 0) currentY++;
            else if ((0x4 & i_116_) != 0) currentY--;
            if ((i_116_ & 0x2) != 0) currentX++;
            else if ((i_116_ & 0x8) != 0) currentX--;
            i_116_ = directions[currentX][currentY];
        }
        if (read > 0) {
            //Class100.method1570(i_97_, i_88_, (byte) 55);
            return true;
        }
        return returnTypeConditionalInt != 1;
    }

}
