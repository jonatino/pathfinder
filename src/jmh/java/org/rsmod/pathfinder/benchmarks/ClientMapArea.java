package org.rsmod.pathfinder.benchmarks;

/**
 * @author Kris | 08/01/2021
 */
//Class3
@SuppressWarnings("unused")
public class ClientMapArea {
    public int sceneSizeY;
    public int sceneSizeX;
    public int[][] flags;

    public ClientMapArea(int sizeX, int sizeY) {
        sceneSizeX = sizeX;
        sceneSizeY = sizeY;
        flags = new int[sceneSizeX][sceneSizeY];
        setDefaultFlags();
    }

    public static int include(int existingFlag, int newFlag) {
        return existingFlag | newFlag;
    }

    public static int exclude(int existingFlag, int newFlag) {
        return existingFlag & newFlag;
    }

    public boolean canInteractWithObject(int destAccessFlag, int graphSrcX, int graphSrcY, int destHeight, int graphDestSWCornerY,
                                         int srcWidth, int destWidth, int srcHeight, int graphDestSWCornerX) {
        int graphSrcEastX = graphSrcX + srcWidth;
        int graphSrcNorthY = srcHeight + graphSrcY;
        int graphDestEastX = destWidth + graphDestSWCornerX;
        int graphDestNorthY = destHeight + graphDestSWCornerY;
        if (graphDestSWCornerX <= graphSrcX && graphDestEastX > graphSrcX) {
            if (graphDestSWCornerY == graphSrcNorthY && (0x4 & destAccessFlag) == 0) {
                int minEastX = Math.min(graphSrcEastX, graphDestEastX);
                for (int x = graphSrcX; x < minEastX; x++) {
                    if ((0x2 & (flags[x][-1 + graphSrcNorthY])) == 0) {
                        return true;
                    }
                }
            } else if (graphDestNorthY == graphSrcY && (destAccessFlag & 0x1) == 0) {
                int minEastX = Math.min(graphSrcEastX, graphDestEastX);
                for (int x = graphSrcX; x < minEastX; x++) {
                    if ((0x20 & flags[x][graphSrcY]) == 0) {
                        return true;
                    }
                }
            }
        } else if (graphDestSWCornerX < graphSrcEastX && graphDestEastX >= graphSrcEastX) {
            if (graphDestSWCornerY == graphSrcNorthY && (0x4 & destAccessFlag) == 0) {
                for (int x = graphDestSWCornerX; x < graphSrcEastX; x++) {
                    if (((flags[x][(graphSrcNorthY - 1)]) & 0x2) == 0) {
                        return true;
                    }
                }
            } else if (graphSrcY == graphDestNorthY && (0x1 & destAccessFlag) == 0) {
                for (int x = graphDestSWCornerX; x < graphSrcEastX; x++) {
                    if (((flags[x][graphSrcY]) & 0x20) == 0) {
                        return true;
                    }
                }
            }
        } else if (graphSrcY >= graphDestSWCornerY && graphSrcY < graphDestNorthY) {
            if (graphSrcEastX == graphDestSWCornerX && (destAccessFlag & 0x8) == 0) {
                int minNorthY = Math.min(graphSrcNorthY, graphDestNorthY);
                for (int y = graphSrcY; y < minNorthY; y++) {
                    if ((0x8 & (flags[-1 + graphSrcEastX][y])) == 0) {
                        return true;
                    }
                }
            } else if (graphDestEastX == graphSrcX && (0x2 & destAccessFlag) == 0) {
                int minNorthY = Math.min(graphSrcNorthY, graphDestNorthY);
                for (int y = graphSrcY; y < minNorthY; y++) {
                    if (((flags[graphSrcX][y]) & 0x80) == 0) {
                        return true;
                    }
                }
            }
        } else if (graphSrcNorthY > graphDestSWCornerY && graphSrcNorthY <= graphDestNorthY) {
            if (graphDestSWCornerX == graphSrcEastX && (0x8 & destAccessFlag) == 0) {
                for (int y = graphDestSWCornerY; y < graphSrcNorthY; y++) {
                    if ((0x8 & (flags[(graphSrcEastX + -1)][y])) == 0) {
                        return true;
                    }
                }
            } else if (graphDestEastX == graphSrcX && (destAccessFlag & 0x2) == 0) {
                for (int y = graphDestSWCornerY; y < graphSrcNorthY; y++) {
                    if ((0x80 & (flags[graphSrcX][y])) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //AddFloor, matrix does it wrong by adding floor deco instead.
    public void addFloor(int sceneX, int sceneY) {
        flags[sceneX][sceneY] = include(flags[sceneX][sceneY], 0x200000);
    }

    public void setDefaultFlags() {
        for (int x = 0; x < sceneSizeX; x++) {
            for (int y = 0; y < sceneSizeY; y++) {
                if (x == 0 || y == 0 || x >= sceneSizeX - 5 || y >= sceneSizeY - 5) {
                    flags[x][y] = 0xffffff;
                } else {
                    flags[x][y] = 0x1000000;
                }
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void removeObject(boolean blockProjectile, int sizeX, int y, int rotation, int sizeY, int x) {
        if (rotation == 1 || rotation == 3) {
            int oldSizeX = sizeX;
            sizeX = sizeY;
            sizeY = oldSizeX;
        }
        int flag = 0x100;
        if (blockProjectile) {
            flag += 0x20000;
        }
        for (int tileX = x; tileX < sizeX + x; tileX++) {
            if (tileX >= 0 && tileX < sceneSizeX) {
                for (int tileY = y; tileY < sizeY + y; tileY++) {
                    if (tileY >= 0 && sceneSizeY > tileY) {
                        removeFlag(tileX, tileY, flag);
                    }
                }
            }
        }
    }

    //method85
    public void addFloorDecoration(int x, int y) {
        flags[x][y] = include(flags[x][y], 0x40000);
    }

    public static boolean checkWallInteract(int[][] flags, int srcSize, int graphDestY, int locType, int graphSrcX, int locRotation, boolean checkFlags, int graphSrcY, int graphDestX) {
        if (!checkFlags) return true;
        if (srcSize == 1) {
            if (graphDestX == graphSrcX && graphDestY == graphSrcY) {
                return true;
            }
        } else if (graphSrcX <= graphDestX && graphSrcX + srcSize - 1 >= graphDestX && graphDestY + srcSize - 1 >= graphDestY) {
            return true;
        }
        if (srcSize == 1) {
            if (locType == 0) {
                if (locRotation == 0) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x12c0102) == 0) return true;
                } else if (locRotation == 1) {
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0180) == 0) return true;
                } else if (locRotation == 2) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x12c0102) == 0) return true;
                } else if (locRotation == 3) {
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1) return true;
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0180) == 0) return true;
                }
            }
            if (locType == 2) {
                if (locRotation == 0) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0180) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x12c0102) == 0) return true;
                } else if (locRotation == 1) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x12c0102) == 0) return true;
                } else if (locRotation == 2) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1) return true;
                } else if (locRotation == 3) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x12c0180) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1) return true;
                }
            }
            if (locType == 9) {
                if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x20) == 0) return true;
                if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x2) == 0) return true;
                if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x8) == 0) return true;
                return graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x80) == 0;
            }
        } else {
            int easternXEdge = graphSrcX + srcSize - 1;
            int northernYEdge = graphSrcY + srcSize - 1;
            if (locType == 0) {
                if (locRotation == 0) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1 && (flags[graphDestX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize && (flags[graphDestX][northernYEdge] & 0x12c0102) == 0) return true;
                } else if (locRotation == 1) {
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x12c0180) == 0) return true;
                } else if (locRotation == 2) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1 && (flags[graphDestX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize && (flags[graphDestX][northernYEdge] & 0x12c0102) == 0) return true;
                } else if (locRotation == 3) {
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize) return true;
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x12c0108) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x12c0180) == 0) return true;
                }
            }
            if (locType == 2) {
                if (locRotation == 0) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x12c0180) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize && (flags[graphDestX][northernYEdge] & 0x12c0102) == 0) return true;
                } else if (locRotation == 1) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x12c0108) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize && (flags[graphDestX][northernYEdge] & 0x12c0102) == 0) return true;
                } else if (locRotation == 2) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x12c0108) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1 && (flags[graphDestX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && graphDestX <= easternXEdge && graphSrcY == graphDestY - srcSize) return true;
                } else if (locRotation == 3) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY) return true;
                    if (graphSrcX <= graphDestX && graphDestX <= easternXEdge && graphSrcY == graphDestY + 1 && (flags[graphDestX][graphSrcY] & 0x12c0120) == 0) return true;
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x12c0180) == 0) return true;
                    if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize) return true;
                }
            }
            if (locType == 9) {
                if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY + 1 && (flags[graphDestX][graphSrcY] & 0x12c0120) == 0) return true;
                if (graphSrcX <= graphDestX && easternXEdge >= graphDestX && graphSrcY == graphDestY - srcSize && (flags[graphDestX][northernYEdge] & 0x12c0102) == 0) return true;
                if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x12c0108) == 0) return true;
                return graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x12c0180) == 0;
            }
        }
        return false;
    }

    //method88
    public void addObject(boolean blockProjectile, int y, int x, int sizeY, int sizeX) {
        int flag = 0x100;
        if (blockProjectile) {
            flag += 0x20000;
        }
        for (int tileX = x; tileX < x + sizeX; tileX++) {
            if (tileX >= 0 && sceneSizeX > tileX) {
                for (int tileY = y; sizeY + y > tileY; tileY++) {
                    if (tileY >= 0 && sceneSizeY > tileY) {
                        addFlag(tileX, tileY, flag);
                    }
                }
            }
        }
    }

    //method89
    public void removeDecoration(int x, int y) {
        flags[x][y] = exclude(flags[x][y], ~0x40000);
    }

    public void addWall(int rotation, boolean blockProjectile, int x, int type, int y) {
        if (type == 0) {
            if (rotation == 0) {
                addFlag(x, y, 0x80);
                addFlag(-1 + x, y, 0x8);
            }
            if (rotation == 1) {
                addFlag(x, y, 0x2);
                addFlag(x, 1 + y, 0x20);
            }
            if (rotation == 2) {
                addFlag(x, y, 0x8);
                addFlag(1 + x, y, 0x80);
            }
            if (rotation == 3) {
                addFlag(x, y, 0x20);
                addFlag(x, -1 + y, 0x2);
            }
        }
        if (type == 1 || type == 3) {
            if (rotation == 0) {
                addFlag(x, y, 0x1);
                addFlag(x - 1, 1 + y, 0x10);
            }
            if (rotation == 1) {
                addFlag(x, y, 0x4);
                addFlag(x - -1, 1 + y, 0x40);
            }
            if (rotation == 2) {
                addFlag(x, y, 0x10);
                addFlag(1 + x, -1 + y, 0x1);
            }
            if (rotation == 3) {
                addFlag(x, y, 0x40);
                addFlag(-1 + x, -1 + y, 0x4);
            }
        }
        if (type == 2) {
            if (rotation == 0) {
                addFlag(x, y, 0x82);
                addFlag(-1 + x, y, 0x8);
                addFlag(x, y + 1, 0x20);
            }
            if (rotation == 1) {
                addFlag(x, y, 0xa);
                addFlag(x, y + 1, 0x20);
                addFlag(x + 1, y, 0x80);
            }
            if (rotation == 2) {
                addFlag(x, y, 0x28);
                addFlag(x + 1, y, 0x80);
                addFlag(x, -1 + y, 0x2);
            }
            if (rotation == 3) {
                addFlag(x, y, 0xa0);
                addFlag(x, y - 1, 0x2);
                addFlag(-1 + x, y, 0x8);
            }
        }
        if (blockProjectile) {
            if (type == 0) {
                if (rotation == 0) {
                    addFlag(x, y, 0x10000);
                    addFlag(-1 + x, y, 0x1000);
                }
                if (rotation == 1) {
                    addFlag(x, y, 0x400);
                    addFlag(x, y + 1, 0x4000);
                }
                if (rotation == 2) {
                    addFlag(x, y, 0x1000);
                    addFlag(1 + x, y, 0x10000);
                }
                if (rotation == 3) {
                    addFlag(x, y, 0x4000);
                    addFlag(x, -1 + y, 0x400);
                }
            }
            if (type == 1 || type == 3) {
                if (rotation == 0) {
                    addFlag(x, y, 0x200);
                    addFlag(x - 1, 1 + y, 0x2000);
                }
                if (rotation == 1) {
                    addFlag(x, y, 0x800);
                    addFlag(x + 1, y + 1, 0x8000);
                }
                if (rotation == 2) {
                    addFlag(x, y, 0x2000);
                    addFlag(1 + x, -1 + y, 0x200);
                }
                if (rotation == 3) {
                    addFlag(x, y, 0x8000);
                    addFlag(-1 + x, y - 1, 0x800);
                }
            }
            if (type == 2) {
                if (rotation == 0) {
                    addFlag(x, y, 0x10400);
                    addFlag(x + -1, y, 0x1000);
                    addFlag(x, y - -1, 0x4000);
                }
                if (rotation == 1) {
                    addFlag(x, y, 0x1400);
                    addFlag(x, 1 + y, 0x4000);
                    addFlag(1 + x, y, 0x10000);
                }
                if (rotation == 2) {
                    addFlag(x, y, 0x5000);
                    addFlag(1 + x, y, 0x10000);
                    addFlag(x, -1 + y, 0x400);
                }
                if (rotation == 3) {
                    addFlag(x, y, 0x14000);
                    addFlag(x, y + -1, 0x400);
                    addFlag(x + -1, y, 0x1000);
                }
            }
        }
    }

    public static boolean checkWallDecorationInteract(int[][] flags, int graphSrcX, int srcSize, int graphSrcY, int locRotation, int graphDestY, int graphDestX, int locType, boolean checkFlags) {
        if (srcSize == 1) {
            if (graphSrcX == graphDestX && graphDestY == graphSrcY) return true;
        } else if (graphDestX >= graphSrcX && srcSize + graphSrcX + -1 >= graphDestX && srcSize + graphDestY + -1 >= graphDestY) return true;
        if (!checkFlags) return true;
        if (srcSize == 1) {
            if (locType == 6 || locType == 7) {
                int rotation = locType == 7 ? (locRotation + 2 & 0x3) : locRotation;
                if (rotation == 0) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x80) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x2) == 0) return true;
                } else if (rotation == 1) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x8) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x2) == 0) return true;
                } else if (rotation == 2) {
                    if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x8) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x20) == 0) return true;
                } else if (rotation == 3) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x80) == 0) return true;
                    if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x20) == 0) return true;
                }
            }
            if (locType == 8) {
                if (graphSrcX == graphDestX && graphSrcY == graphDestY + 1 && (flags[graphSrcX][graphSrcY] & 0x20) == 0) return true;
                if (graphSrcX == graphDestX && graphSrcY == graphDestY - 1 && (flags[graphSrcX][graphSrcY] & 0x2) == 0) return true;
                if (graphSrcX == graphDestX - 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x8) == 0) return true;
                return graphSrcX == graphDestX + 1 && graphSrcY == graphDestY && (flags[graphSrcX][graphSrcY] & 0x80) == 0;
            }
        } else {
            int easternXEdge = graphSrcX + srcSize - 1;
            int northernYEdge = graphSrcY + srcSize - 1;
            if (locType == 6 || locType == 7) {
                int rotation = locType == 7 ? (locRotation + 2 & 0x3) : locRotation;
                if (rotation == 0) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x80) == 0) return true;
                    if (graphSrcX <= graphDestX && graphSrcY == graphDestY - srcSize && easternXEdge >= graphDestX && (flags[graphDestX][northernYEdge] & 0x2) == 0) return true;
                } else if (rotation == 1) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x8) == 0) return true;
                    if (graphSrcX <= graphDestX && graphSrcY == graphDestY - srcSize && easternXEdge >= graphDestX && (flags[graphDestX][northernYEdge] & 0x2) == 0) return true;
                } else if (rotation == 2) {
                    if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x8) == 0) return true;
                    if (graphSrcX <= graphDestX && graphSrcY == graphDestY + 1 && easternXEdge >= graphDestX && (flags[graphDestX][graphSrcY] & 0x20) == 0) return true;
                } else if (rotation == 3) {
                    if (graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x80) == 0) return true;
                    if (graphSrcX <= graphDestX && graphSrcY == graphDestY + 1 && easternXEdge >= graphDestX && (flags[graphDestX][graphSrcY] & 0x20) == 0) return true;
                }
            }
            if (locType == 8) {
                if (graphSrcX <= graphDestX && graphSrcY == graphDestY + 1 && easternXEdge >= graphDestX && (flags[graphDestX][graphSrcY] & 0x20) == 0) return true;
                if (graphSrcX <= graphDestX && graphSrcY == graphDestY - srcSize && easternXEdge >= graphDestX && (flags[graphDestX][northernYEdge] & 0x2) == 0) return true;
                if (graphSrcX == graphDestX - srcSize && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[easternXEdge][graphDestY] & 0x8) == 0) return true;
                return graphSrcX == graphDestX + 1 && graphSrcY <= graphDestY && northernYEdge >= graphDestY && (flags[graphSrcX][graphDestY] & 0x80) == 0;
            }
        }
        return false;
    }

    public boolean collides(int destWidth, int graphDestSWCornerX, int destHeight, int graphSrcY, int graphDestSWCornerY, int graphSrcX, int srcHeight, int srcWidth) {
        if (graphSrcX >= graphDestSWCornerX + destWidth || graphSrcX + srcWidth <= graphDestSWCornerX) {
            return false;
        }
        return graphSrcY < graphDestSWCornerY + destHeight && graphDestSWCornerY < srcHeight + graphSrcY;
    }

    public boolean canExit(int destHeight, int graphSrcX, int destAccessFlag, int graphDestSWCornerY, int graphDestSWCornerX, int srcSize, int destWidth, int graphSrcY) {
        if (srcSize > 1) {
            if (collides(destWidth, graphDestSWCornerX, destHeight, graphSrcY, graphDestSWCornerY, graphSrcX, srcSize, srcSize)) {
                return true;
            }
            return canInteractWithObject(destAccessFlag, graphSrcX, graphSrcY, destHeight, graphDestSWCornerY, srcSize, destWidth, srcSize, graphDestSWCornerX);
        }
        int easternXEdge = graphDestSWCornerX + destWidth - 1;
        int northernYEdge = graphDestSWCornerY + destHeight - 1;
        if (graphSrcX >= graphDestSWCornerX && graphSrcX <= easternXEdge && graphSrcY >= graphDestSWCornerY && graphSrcY <= northernYEdge) return true;
        if (graphSrcX == graphDestSWCornerX - 1 && graphSrcY >= graphDestSWCornerY && graphSrcY <= northernYEdge && (flags[graphSrcX][graphSrcY] & 0x8) == 0 && (destAccessFlag & 0x8) == 0) return true;
        if (graphSrcX == easternXEdge + 1 && graphSrcY >= graphDestSWCornerY && graphSrcY <= northernYEdge && (flags[graphSrcX][graphSrcY] & 0x80) == 0 && (destAccessFlag & 0x2) == 0) return true;
        if (graphSrcY + 1 == graphDestSWCornerY && graphSrcX >= graphDestSWCornerX && graphSrcX <= easternXEdge && (flags[graphSrcX][graphSrcY] & 0x2) == 0 && (destAccessFlag & 0x4) == 0) return true;
        return graphSrcY == northernYEdge + 1 && graphSrcX >= graphDestSWCornerX && graphSrcX <= easternXEdge && (flags[graphSrcX][graphSrcY] & 0x20) == 0 && (destAccessFlag & 0x1) == 0;
    }

    public void removeFlag(int x, int y, int excludedFlag) {
        flags[x][y] = exclude(flags[x][y], ~excludedFlag);
    }

    public void addFlag(int x, int y, int includedFlag) {
        flags[x][y] = include(flags[x][y], includedFlag);
    }

    public void removeWall(int rotation, int type, int x, int y, boolean blockProjectile) {
        if (type == 0) {
            if (rotation == 0) {
                removeFlag(x, y, 0x80);
                removeFlag(-1 + x, y, 0x8);
            }
            if (rotation == 1) {
                removeFlag(x, y, 0x2);
                removeFlag(x, y - -1, 0x20);
            }
            if (rotation == 2) {
                removeFlag(x, y, 0x8);
                removeFlag(1 + x, y, 0x80);
            }
            if (rotation == 3) {
                removeFlag(x, y, 0x20);
                removeFlag(x, y - 1, 0x2);
            }
        }
        if (type == 1 || type == 3) {
            if (rotation == 0) {
                removeFlag(x, y, 0x1);
                removeFlag(x - 1, y - -1, 0x10);
            }
            if (rotation == 1) {
                removeFlag(x, y, 0x4);
                removeFlag(1 + x, y - -1, 0x40);
            }
            if (rotation == 2) {
                removeFlag(x, y, 0x10);
                removeFlag(x + 1, -1 + y, 0x1);
            }
            if (rotation == 3) {
                removeFlag(x, y, 0x40);
                removeFlag(x + -1, y - 1, 0x4);
            }
        }
        if (type == 2) {
            if (rotation == 0) {
                removeFlag(x, y, 0x82);
                removeFlag(x - 1, y, 0x8);
                removeFlag(x, y + 1, 0x20);
            }
            if (rotation == 1) {
                removeFlag(x, y, 0xa);
                removeFlag(x, 1 + y, 0x20);
                removeFlag(1 + x, y, 0x80);
            }
            if (rotation == 2) {
                removeFlag(x, y, 0x28);
                removeFlag(x - -1, y, 0x80);
                removeFlag(x, y + -1, 0x2);
            }
            if (rotation == 3) {
                removeFlag(x, y, 0xa0);
                removeFlag(x, -1 + y, 0x2);
                removeFlag(x + -1, y, 0x8);
            }
        }
        if (blockProjectile) {
            if (type == 0) {
                if (rotation == 0) {
                    removeFlag(x, y, 0x10000);
                    removeFlag(x - 1, y, 0x1000);
                }
                if (rotation == 1) {
                    removeFlag(x, y, 0x400);
                    removeFlag(x, 1 + y, 0x4000);
                }
                if (rotation == 2) {
                    removeFlag(x, y, 0x1000);
                    removeFlag(x + 1, y, 0x10000);
                }
                if (rotation == 3) {
                    removeFlag(x, y, 0x4000);
                    removeFlag(x, y - 1, 0x400);
                }
            }
            if (type == 1 || type == 3) {
                if (rotation == 0) {
                    removeFlag(x, y, 0x200);
                    removeFlag(x - 1, 1 + y, 0x2000);
                }
                if (rotation == 1) {
                    removeFlag(x, y, 0x800);
                    removeFlag(x - -1, y - -1, 0x8000);
                }
                if (rotation == 2) {
                    removeFlag(x, y, 0x2000);
                    removeFlag(1 + x, -1 + y, 0x200);
                }
                if (rotation == 3) {
                    removeFlag(x, y, 0x8000);
                    removeFlag(x - 1, -1 + y, 0x800);
                }
            }
            if (type == 2) {
                if (rotation == 0) {
                    removeFlag(x, y, 0x10400);
                    removeFlag(-1 + x, y, 0x1000);
                    removeFlag(x, y + 1, 0x4000);
                }
                if (rotation == 1) {
                    removeFlag(x, y, 0x1400);
                    removeFlag(x, y + 1, 0x4000);
                    removeFlag(1 + x, y, 0x10000);
                }
                if (rotation == 2) {
                    removeFlag(x, y, 0x5000);
                    removeFlag(x + 1, y, 0x10000);
                    removeFlag(x, y - 1, 0x400);
                }
                if (rotation == 3) {
                    removeFlag(x, y, 0x14000);
                    removeFlag(x, -1 + y, 0x400);
                    removeFlag(-1 + x, y, 0x1000);
                }
            }
        }
    }
}
