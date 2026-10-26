package ganymedes01.etfuturum.world.structure.mansion;

import java.util.*;
import ganymedes01.etfuturum.world.structure.surface.SurfaceStructures;

/** Released mansion room-grid algorithm, adapted to saved native pieces and Java 8 collections. */
public final class WoodlandMansionPieces {
    public static void generate(Random random, int x, int y, int z, int rotation, List<SurfaceStructures.Piece> result) {
        List<SurfaceStructures.Piece> pieces = new ArrayList<>();
        generateMansion( new BlockPos(x, y, z), Rotation.values()[rotation & 3], pieces, random);
        result.addAll(pieces);
    }
    public static void generateMansion(
        BlockPos p_229987_,
        Rotation p_229988_,
        List<SurfaceStructures.Piece> p_229989_,
        Random p_229990_
    ) {
        WoodlandMansionPieces.MansionGrid woodlandmansionpieces$mansiongrid = new WoodlandMansionPieces.MansionGrid(p_229990_);
        WoodlandMansionPieces.MansionPiecePlacer woodlandmansionpieces$mansionpieceplacer = new WoodlandMansionPieces.MansionPiecePlacer(p_229990_);
        woodlandmansionpieces$mansionpieceplacer.createMansion(p_229987_, p_229988_, p_229989_, woodlandmansionpieces$mansiongrid);
    }

    static class FirstFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
        @Override
        public String get1x1(Random p_229995_) {
            return "1x1_a" + (p_229995_.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(Random p_230000_) {
            return "1x1_as" + (p_230000_.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(Random p_229997_, boolean p_229998_) {
            return "1x2_a" + (p_229997_.nextInt(9) + 1);
        }

        @Override
        public String get1x2FrontEntrance(Random p_230002_, boolean p_230003_) {
            return "1x2_b" + (p_230002_.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(Random p_230005_) {
            return "1x2_s" + (p_230005_.nextInt(2) + 1);
        }

        @Override
        public String get2x2(Random p_230007_) {
            return "2x2_a" + (p_230007_.nextInt(4) + 1);
        }

        @Override
        public String get2x2Secret(Random p_230009_) {
            return "2x2_s1";
        }
    }

    abstract static class FloorRoomCollection {
        public abstract String get1x1(Random p_230011_);

        public abstract String get1x1Secret(Random p_230014_);

        public abstract String get1x2SideEntrance(Random p_230012_, boolean p_230013_);

        public abstract String get1x2FrontEntrance(Random p_230015_, boolean p_230016_);

        public abstract String get1x2Secret(Random p_230017_);

        public abstract String get2x2(Random p_230018_);

        public abstract String get2x2Secret(Random p_230019_);
    }

    static class MansionGrid {
        private static final int DEFAULT_SIZE = 11;
        private static final int CLEAR = 0;
        private static final int CORRIDOR = 1;
        private static final int ROOM = 2;
        private static final int START_ROOM = 3;
        private static final int TEST_ROOM = 4;
        private static final int BLOCKED = 5;
        private static final int ROOM_1x1 = 65536;
        private static final int ROOM_1x2 = 131072;
        private static final int ROOM_2x2 = 262144;
        private static final int ROOM_ORIGIN_FLAG = 1048576;
        private static final int ROOM_DOOR_FLAG = 2097152;
        private static final int ROOM_STAIRS_FLAG = 4194304;
        private static final int ROOM_CORRIDOR_FLAG = 8388608;
        private static final int ROOM_TYPE_MASK = 983040;
        private static final int ROOM_ID_MASK = 65535;
        private final Random random;
        final WoodlandMansionPieces.SimpleGrid baseGrid;
        final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
        final WoodlandMansionPieces.SimpleGrid[] floorRooms;
        final int entranceX;
        final int entranceY;

        public MansionGrid(Random p_230043_) {
            this.random = p_230043_;
            int i = 11;
            this.entranceX = 7;
            this.entranceY = 4;
            this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

            while (this.cleanEdges(this.baseGrid)) {
            }

            this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
            this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
            this.identifyRooms(this.baseGrid, this.floorRooms[0]);
            this.identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
            this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
            this.setupThirdFloor();
            this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(WoodlandMansionPieces.SimpleGrid p_230048_, int p_230049_, int p_230050_) {
            int i = p_230048_.get(p_230049_, p_230050_);
            return i == 1 || i == 2 || i == 3 || i == 4;
        }

        public boolean isRoomId(WoodlandMansionPieces.SimpleGrid p_230052_, int p_230053_, int p_230054_, int p_230055_, int p_230056_) {
            return (this.floorRooms[p_230055_].get(p_230053_, p_230054_) & 65535) == p_230056_;
        }

        public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid p_230068_, int p_230069_, int p_230070_, int p_230071_, int p_230072_) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (this.isRoomId(p_230068_, p_230069_ + direction.getStepX(), p_230070_ + direction.getStepZ(), p_230071_, p_230072_)) {
                    return direction;
                }
            }

            return null;
        }

        private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid p_230058_, int p_230059_, int p_230060_, Direction p_230061_, int p_230062_) {
            if (p_230062_ > 0) {
                p_230058_.set(p_230059_, p_230060_, 1);
                p_230058_.setif(p_230059_ + p_230061_.getStepX(), p_230060_ + p_230061_.getStepZ(), 0, 1);

                for (int i = 0; i < 8; i++) {
                    Direction direction = Direction.from2DDataValue(this.random.nextInt(4));
                    if (direction != p_230061_.getOpposite() && (direction != Direction.EAST || !this.random.nextBoolean())) {
                        int j = p_230059_ + p_230061_.getStepX();
                        int k = p_230060_ + p_230061_.getStepZ();
                        if (p_230058_.get(j + direction.getStepX(), k + direction.getStepZ()) == 0
                            && p_230058_.get(j + direction.getStepX() * 2, k + direction.getStepZ() * 2) == 0) {
                            this.recursiveCorridor(
                                p_230058_,
                                p_230059_ + p_230061_.getStepX() + direction.getStepX(),
                                p_230060_ + p_230061_.getStepZ() + direction.getStepZ(),
                                direction,
                                p_230062_ - 1
                            );
                            break;
                        }
                    }
                }

                Direction direction1 = p_230061_.getClockWise();
                Direction direction2 = p_230061_.getCounterClockWise();
                p_230058_.setif(p_230059_ + direction1.getStepX(), p_230060_ + direction1.getStepZ(), 0, 2);
                p_230058_.setif(p_230059_ + direction2.getStepX(), p_230060_ + direction2.getStepZ(), 0, 2);
                p_230058_.setif(p_230059_ + p_230061_.getStepX() + direction1.getStepX(), p_230060_ + p_230061_.getStepZ() + direction1.getStepZ(), 0, 2);
                p_230058_.setif(p_230059_ + p_230061_.getStepX() + direction2.getStepX(), p_230060_ + p_230061_.getStepZ() + direction2.getStepZ(), 0, 2);
                p_230058_.setif(p_230059_ + p_230061_.getStepX() * 2, p_230060_ + p_230061_.getStepZ() * 2, 0, 2);
                p_230058_.setif(p_230059_ + direction1.getStepX() * 2, p_230060_ + direction1.getStepZ() * 2, 0, 2);
                p_230058_.setif(p_230059_ + direction2.getStepX() * 2, p_230060_ + direction2.getStepZ() * 2, 0, 2);
            }
        }

        private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid p_230046_) {
            boolean flag = false;

            for (int i = 0; i < p_230046_.height; i++) {
                for (int j = 0; j < p_230046_.width; j++) {
                    if (p_230046_.get(j, i) == 0) {
                        int k = 0;
                        k += isHouse(p_230046_, j + 1, i) ? 1 : 0;
                        k += isHouse(p_230046_, j - 1, i) ? 1 : 0;
                        k += isHouse(p_230046_, j, i + 1) ? 1 : 0;
                        k += isHouse(p_230046_, j, i - 1) ? 1 : 0;
                        if (k >= 3) {
                            p_230046_.set(j, i, 2);
                            flag = true;
                        } else if (k == 2) {
                            int l = 0;
                            l += isHouse(p_230046_, j + 1, i + 1) ? 1 : 0;
                            l += isHouse(p_230046_, j - 1, i + 1) ? 1 : 0;
                            l += isHouse(p_230046_, j + 1, i - 1) ? 1 : 0;
                            l += isHouse(p_230046_, j - 1, i - 1) ? 1 : 0;
                            if (l <= 1) {
                                p_230046_.set(j, i, 2);
                                flag = true;
                            }
                        }
                    }
                }
            }

            return flag;
        }

        private void setupThirdFloor() {
            List<Tuple<Integer, Integer>> list = new ArrayList<>();
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = this.floorRooms[1];

            for (int i = 0; i < this.thirdFloorGrid.height; i++) {
                for (int j = 0; j < this.thirdFloorGrid.width; j++) {
                    int k = woodlandmansionpieces$simplegrid.get(j, i);
                    int l = k & 983040;
                    if (l == 131072 && (k & 2097152) == 2097152) {
                        list.add(new Tuple<>(j, i));
                    }
                }
            }

            if (list.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
            } else {
                Tuple<Integer, Integer> tuple = list.get(this.random.nextInt(list.size()));
                int l1 = woodlandmansionpieces$simplegrid.get(tuple.getA(), tuple.getB());
                woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1 | 4194304);
                Direction direction1 = this.get1x2RoomDirection(this.baseGrid, tuple.getA(), tuple.getB(), 1, l1 & 65535);
                int i2 = tuple.getA() + direction1.getStepX();
                int i1 = tuple.getB() + direction1.getStepZ();

                for (int j1 = 0; j1 < this.thirdFloorGrid.height; j1++) {
                    for (int k1 = 0; k1 < this.thirdFloorGrid.width; k1++) {
                        if (!isHouse(this.baseGrid, k1, j1)) {
                            this.thirdFloorGrid.set(k1, j1, 5);
                        } else if (k1 == tuple.getA() && j1 == tuple.getB()) {
                            this.thirdFloorGrid.set(k1, j1, 3);
                        } else if (k1 == i2 && j1 == i1) {
                            this.thirdFloorGrid.set(k1, j1, 3);
                            this.floorRooms[2].set(k1, j1, 8388608);
                        }
                    }
                }

                List<Direction> list1 = new ArrayList<>();

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (this.thirdFloorGrid.get(i2 + direction.getStepX(), i1 + direction.getStepZ()) == 0) {
                        list1.add(direction);
                    }
                }

                if (list1.isEmpty()) {
                    this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                    woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1);
                } else {
                    Direction direction2 = list1.get(this.random.nextInt(list1.size()));
                    this.recursiveCorridor(this.thirdFloorGrid, i2 + direction2.getStepX(), i1 + direction2.getStepZ(), direction2, 4);

                    while (this.cleanEdges(this.thirdFloorGrid)) {
                    }
                }
            }
        }

        private void identifyRooms(WoodlandMansionPieces.SimpleGrid p_230064_, WoodlandMansionPieces.SimpleGrid p_230065_) {
            ArrayList<Tuple<Integer, Integer>> objectarraylist = new ArrayList<>();

            for (int i = 0; i < p_230064_.height; i++) {
                for (int j = 0; j < p_230064_.width; j++) {
                    if (p_230064_.get(j, i) == 2) {
                        objectarraylist.add(new Tuple<>(j, i));
                    }
                }
            }

            Collections.shuffle(objectarraylist, this.random);
            int k3 = 10;

            for (Tuple<Integer, Integer> tuple : objectarraylist) {
                int k = tuple.getA();
                int l = tuple.getB();
                if (p_230065_.get(k, l) == 0) {
                    int i1 = k;
                    int j1 = k;
                    int k1 = l;
                    int l1 = l;
                    int i2 = 65536;
                    if (p_230065_.get(k + 1, l) == 0
                        && p_230065_.get(k, l + 1) == 0
                        && p_230065_.get(k + 1, l + 1) == 0
                        && p_230064_.get(k + 1, l) == 2
                        && p_230064_.get(k, l + 1) == 2
                        && p_230064_.get(k + 1, l + 1) == 2) {
                        j1 = k + 1;
                        l1 = l + 1;
                        i2 = 262144;
                    } else if (p_230065_.get(k - 1, l) == 0
                        && p_230065_.get(k, l + 1) == 0
                        && p_230065_.get(k - 1, l + 1) == 0
                        && p_230064_.get(k - 1, l) == 2
                        && p_230064_.get(k, l + 1) == 2
                        && p_230064_.get(k - 1, l + 1) == 2) {
                        i1 = k - 1;
                        l1 = l + 1;
                        i2 = 262144;
                    } else if (p_230065_.get(k - 1, l) == 0
                        && p_230065_.get(k, l - 1) == 0
                        && p_230065_.get(k - 1, l - 1) == 0
                        && p_230064_.get(k - 1, l) == 2
                        && p_230064_.get(k, l - 1) == 2
                        && p_230064_.get(k - 1, l - 1) == 2) {
                        i1 = k - 1;
                        k1 = l - 1;
                        i2 = 262144;
                    } else if (p_230065_.get(k + 1, l) == 0 && p_230064_.get(k + 1, l) == 2) {
                        j1 = k + 1;
                        i2 = 131072;
                    } else if (p_230065_.get(k, l + 1) == 0 && p_230064_.get(k, l + 1) == 2) {
                        l1 = l + 1;
                        i2 = 131072;
                    } else if (p_230065_.get(k - 1, l) == 0 && p_230064_.get(k - 1, l) == 2) {
                        i1 = k - 1;
                        i2 = 131072;
                    } else if (p_230065_.get(k, l - 1) == 0 && p_230064_.get(k, l - 1) == 2) {
                        k1 = l - 1;
                        i2 = 131072;
                    }

                    int j2 = this.random.nextBoolean() ? i1 : j1;
                    int k2 = this.random.nextBoolean() ? k1 : l1;
                    int l2 = 2097152;
                    if (!p_230064_.edgesTo(j2, k2, 1)) {
                        j2 = j2 == i1 ? j1 : i1;
                        k2 = k2 == k1 ? l1 : k1;
                        if (!p_230064_.edgesTo(j2, k2, 1)) {
                            k2 = k2 == k1 ? l1 : k1;
                            if (!p_230064_.edgesTo(j2, k2, 1)) {
                                j2 = j2 == i1 ? j1 : i1;
                                k2 = k2 == k1 ? l1 : k1;
                                if (!p_230064_.edgesTo(j2, k2, 1)) {
                                    l2 = 0;
                                    j2 = i1;
                                    k2 = k1;
                                }
                            }
                        }
                    }

                    for (int i3 = k1; i3 <= l1; i3++) {
                        for (int j3 = i1; j3 <= j1; j3++) {
                            if (j3 == j2 && i3 == k2) {
                                p_230065_.set(j3, i3, 1048576 | l2 | i2 | k3);
                            } else {
                                p_230065_.set(j3, i3, i2 | k3);
                            }
                        }
                    }

                    k3++;
                }
            }
        }
    }

    static class MansionPiecePlacer {
        private final Random random;
        private int startX;
        private int startY;

        public MansionPiecePlacer(Random p_230079_) {
            this.random = p_230079_;
        }

        public void createMansion(
            BlockPos p_230081_, Rotation p_230082_, List<SurfaceStructures.Piece> p_230083_, WoodlandMansionPieces.MansionGrid p_230084_
        ) {
            WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata = new WoodlandMansionPieces.PlacementData();
            woodlandmansionpieces$placementdata.position = p_230081_;
            woodlandmansionpieces$placementdata.rotation = p_230082_;
            woodlandmansionpieces$placementdata.wallType = "wall_flat";
            WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata1 = new WoodlandMansionPieces.PlacementData();
            this.entrance(p_230083_, woodlandmansionpieces$placementdata);
            woodlandmansionpieces$placementdata1.position = woodlandmansionpieces$placementdata.position.above(8);
            woodlandmansionpieces$placementdata1.rotation = woodlandmansionpieces$placementdata.rotation;
            woodlandmansionpieces$placementdata1.wallType = "wall_window";
            if (!p_230083_.isEmpty()) {
            }

            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = p_230084_.baseGrid;
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid1 = p_230084_.thirdFloorGrid;
            this.startX = p_230084_.entranceX + 1;
            this.startY = p_230084_.entranceY + 1;
            int i = p_230084_.entranceX + 1;
            int j = p_230084_.entranceY;
            this.traverseOuterWalls(
                p_230083_, woodlandmansionpieces$placementdata, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j
            );
            this.traverseOuterWalls(
                p_230083_, woodlandmansionpieces$placementdata1, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j
            );
            WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata2 = new WoodlandMansionPieces.PlacementData();
            woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata.position.above(19);
            woodlandmansionpieces$placementdata2.rotation = woodlandmansionpieces$placementdata.rotation;
            woodlandmansionpieces$placementdata2.wallType = "wall_window";
            boolean flag = false;

            for (int k = 0; k < woodlandmansionpieces$simplegrid1.height && !flag; k++) {
                for (int l = woodlandmansionpieces$simplegrid1.width - 1; l >= 0 && !flag; l--) {
                    if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid1, l, k)) {
                        woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position
                            .relative(p_230082_.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                        woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position
                            .relative(p_230082_.rotate(Direction.EAST), (l - this.startX) * 8);
                        this.traverseWallPiece(p_230083_, woodlandmansionpieces$placementdata2);
                        this.traverseOuterWalls(p_230083_, woodlandmansionpieces$placementdata2, woodlandmansionpieces$simplegrid1, Direction.SOUTH, l, k, l, k);
                        flag = true;
                    }
                }
            }

            this.createRoof(p_230083_, p_230081_.above(16), p_230082_, woodlandmansionpieces$simplegrid, woodlandmansionpieces$simplegrid1);
            this.createRoof(p_230083_, p_230081_.above(27), p_230082_, woodlandmansionpieces$simplegrid1, null);
            if (!p_230083_.isEmpty()) {
            }

            WoodlandMansionPieces.FloorRoomCollection[] awoodlandmansionpieces$floorroomcollection = new WoodlandMansionPieces.FloorRoomCollection[]{
                new WoodlandMansionPieces.FirstFloorRoomCollection(),
                new WoodlandMansionPieces.SecondFloorRoomCollection(),
                new WoodlandMansionPieces.ThirdFloorRoomCollection()
            };

            for (int l2 = 0; l2 < 3; l2++) {
                BlockPos blockpos = p_230081_.above(8 * l2 + (l2 == 2 ? 3 : 0));
                WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid2 = p_230084_.floorRooms[l2];
                WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid3 = l2 == 2
                    ? woodlandmansionpieces$simplegrid1
                    : woodlandmansionpieces$simplegrid;
                String s = l2 == 0 ? "carpet_south_1" : "carpet_south_2";
                String s1 = l2 == 0 ? "carpet_west_1" : "carpet_west_2";

                for (int i1 = 0; i1 < woodlandmansionpieces$simplegrid3.height; i1++) {
                    for (int j1 = 0; j1 < woodlandmansionpieces$simplegrid3.width; j1++) {
                        if (woodlandmansionpieces$simplegrid3.get(j1, i1) == 1) {
                            BlockPos blockpos1 = blockpos.relative(p_230082_.rotate(Direction.SOUTH), 8 + (i1 - this.startY) * 8);
                            blockpos1 = blockpos1.relative(p_230082_.rotate(Direction.EAST), (j1 - this.startX) * 8);
                            p_230083_.add(mansionPiece( "corridor_floor", blockpos1, p_230082_));
                            if (woodlandmansionpieces$simplegrid3.get(j1, i1 - 1) == 1
                                || (woodlandmansionpieces$simplegrid2.get(j1, i1 - 1) & 8388608) == 8388608) {
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        "carpet_north",
                                        blockpos1.relative(p_230082_.rotate(Direction.EAST), 1).above(),
                                        p_230082_
                                    )
                                );
                            }

                            if (woodlandmansionpieces$simplegrid3.get(j1 + 1, i1) == 1
                                || (woodlandmansionpieces$simplegrid2.get(j1 + 1, i1) & 8388608) == 8388608) {
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        "carpet_east",
                                        blockpos1.relative(p_230082_.rotate(Direction.SOUTH), 1).relative(p_230082_.rotate(Direction.EAST), 5).above(),
                                        p_230082_
                                    )
                                );
                            }

                            if (woodlandmansionpieces$simplegrid3.get(j1, i1 + 1) == 1
                                || (woodlandmansionpieces$simplegrid2.get(j1, i1 + 1) & 8388608) == 8388608) {
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        s,
                                        blockpos1.relative(p_230082_.rotate(Direction.SOUTH), 5).relative(p_230082_.rotate(Direction.WEST), 1),
                                        p_230082_
                                    )
                                );
                            }

                            if (woodlandmansionpieces$simplegrid3.get(j1 - 1, i1) == 1
                                || (woodlandmansionpieces$simplegrid2.get(j1 - 1, i1) & 8388608) == 8388608) {
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        s1,
                                        blockpos1.relative(p_230082_.rotate(Direction.WEST), 1).relative(p_230082_.rotate(Direction.NORTH), 1),
                                        p_230082_
                                    )
                                );
                            }
                        }
                    }
                }

                String s2 = l2 == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String s3 = l2 == 0 ? "indoors_door_1" : "indoors_door_2";
                List<Direction> list = new ArrayList<>();

                for (int k1 = 0; k1 < woodlandmansionpieces$simplegrid3.height; k1++) {
                    for (int l1 = 0; l1 < woodlandmansionpieces$simplegrid3.width; l1++) {
                        boolean flag1 = l2 == 2 && woodlandmansionpieces$simplegrid3.get(l1, k1) == 3;
                        if (woodlandmansionpieces$simplegrid3.get(l1, k1) == 2 || flag1) {
                            int i2 = woodlandmansionpieces$simplegrid2.get(l1, k1);
                            int j2 = i2 & 983040;
                            int k2 = i2 & 65535;
                            flag1 = flag1 && (i2 & 8388608) == 8388608;
                            list.clear();
                            if ((i2 & 2097152) == 2097152) {
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    if (woodlandmansionpieces$simplegrid3.get(l1 + direction.getStepX(), k1 + direction.getStepZ()) == 1) {
                                        list.add(direction);
                                    }
                                }
                            }

                            Direction direction1 = null;
                            if (!list.isEmpty()) {
                                direction1 = list.get(this.random.nextInt(list.size()));
                            } else if ((i2 & 1048576) == 1048576) {
                                direction1 = Direction.UP;
                            }

                            BlockPos blockpos3 = blockpos.relative(p_230082_.rotate(Direction.SOUTH), 8 + (k1 - this.startY) * 8);
                            blockpos3 = blockpos3.relative(p_230082_.rotate(Direction.EAST), -1 + (l1 - this.startX) * 8);
                            if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid3, l1 - 1, k1)
                                && !p_230084_.isRoomId(woodlandmansionpieces$simplegrid3, l1 - 1, k1, l2, k2)) {
                                p_230083_.add(
                                    mansionPiece(
                                         direction1 == Direction.WEST ? s3 : s2, blockpos3, p_230082_
                                    )
                                );
                            }

                            if (woodlandmansionpieces$simplegrid3.get(l1 + 1, k1) == 1 && !flag1) {
                                BlockPos blockpos2 = blockpos3.relative(p_230082_.rotate(Direction.EAST), 8);
                                p_230083_.add(
                                    mansionPiece(
                                         direction1 == Direction.EAST ? s3 : s2, blockpos2, p_230082_
                                    )
                                );
                            }

                            if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces$simplegrid3, l1, k1 + 1)
                                && !p_230084_.isRoomId(woodlandmansionpieces$simplegrid3, l1, k1 + 1, l2, k2)) {
                                BlockPos blockpos4 = blockpos3.relative(p_230082_.rotate(Direction.SOUTH), 7);
                                blockpos4 = blockpos4.relative(p_230082_.rotate(Direction.EAST), 7);
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        direction1 == Direction.SOUTH ? s3 : s2,
                                        blockpos4,
                                        p_230082_.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (woodlandmansionpieces$simplegrid3.get(l1, k1 - 1) == 1 && !flag1) {
                                BlockPos blockpos5 = blockpos3.relative(p_230082_.rotate(Direction.NORTH), 1);
                                blockpos5 = blockpos5.relative(p_230082_.rotate(Direction.EAST), 7);
                                p_230083_.add(
                                    mansionPiece(
                                        
                                        direction1 == Direction.NORTH ? s3 : s2,
                                        blockpos5,
                                        p_230082_.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (j2 == 65536) {
                                this.addRoom1x1(p_230083_, blockpos3, p_230082_, direction1, awoodlandmansionpieces$floorroomcollection[l2]);
                            } else if (j2 == 131072 && direction1 != null) {
                                Direction direction3 = p_230084_.get1x2RoomDirection(woodlandmansionpieces$simplegrid3, l1, k1, l2, k2);
                                boolean flag2 = (i2 & 4194304) == 4194304;
                                this.addRoom1x2(p_230083_, blockpos3, p_230082_, direction3, direction1, awoodlandmansionpieces$floorroomcollection[l2], flag2);
                            } else if (j2 == 262144 && direction1 != null && direction1 != Direction.UP) {
                                Direction direction2 = direction1.getClockWise();
                                if (!p_230084_.isRoomId(woodlandmansionpieces$simplegrid3, l1 + direction2.getStepX(), k1 + direction2.getStepZ(), l2, k2)) {
                                    direction2 = direction2.getOpposite();
                                }

                                this.addRoom2x2(p_230083_, blockpos3, p_230082_, direction2, direction1, awoodlandmansionpieces$floorroomcollection[l2]);
                            } else if (j2 == 262144 && direction1 == Direction.UP) {
                                this.addRoom2x2Secret(p_230083_, blockpos3, p_230082_, awoodlandmansionpieces$floorroomcollection[l2]);
                            }
                        }
                    }
                }
            }
        }

        private void traverseOuterWalls(
            List<SurfaceStructures.Piece> p_230089_,
            WoodlandMansionPieces.PlacementData p_230090_,
            WoodlandMansionPieces.SimpleGrid p_230091_,
            Direction p_230092_,
            int p_230093_,
            int p_230094_,
            int p_230095_,
            int p_230096_
        ) {
            int i = p_230093_;
            int j = p_230094_;
            Direction direction = p_230092_;

            do {
                if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230091_, i + p_230092_.getStepX(), j + p_230092_.getStepZ())) {
                    this.traverseTurn(p_230089_, p_230090_);
                    p_230092_ = p_230092_.getClockWise();
                    if (i != p_230095_ || j != p_230096_ || direction != p_230092_) {
                        this.traverseWallPiece(p_230089_, p_230090_);
                    }
                } else if (WoodlandMansionPieces.MansionGrid.isHouse(p_230091_, i + p_230092_.getStepX(), j + p_230092_.getStepZ())
                    && WoodlandMansionPieces.MansionGrid.isHouse(
                        p_230091_,
                        i + p_230092_.getStepX() + p_230092_.getCounterClockWise().getStepX(),
                        j + p_230092_.getStepZ() + p_230092_.getCounterClockWise().getStepZ()
                    )) {
                    this.traverseInnerTurn(p_230089_, p_230090_);
                    i += p_230092_.getStepX();
                    j += p_230092_.getStepZ();
                    p_230092_ = p_230092_.getCounterClockWise();
                } else {
                    i += p_230092_.getStepX();
                    j += p_230092_.getStepZ();
                    if (i != p_230095_ || j != p_230096_ || direction != p_230092_) {
                        this.traverseWallPiece(p_230089_, p_230090_);
                    }
                }
            } while (i != p_230095_ || j != p_230096_ || direction != p_230092_);
        }

        private void createRoof(
            List<SurfaceStructures.Piece> p_230103_,
            BlockPos p_230104_,
            Rotation p_230105_,
            WoodlandMansionPieces.SimpleGrid p_230106_,
            WoodlandMansionPieces.SimpleGrid p_230107_
        ) {
            for (int i = 0; i < p_230106_.height; i++) {
                for (int j = 0; j < p_230106_.width; j++) {
                    BlockPos $$27 = p_230104_.relative(p_230105_.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                    $$27 = $$27.relative(p_230105_.rotate(Direction.EAST), (j - this.startX) * 8);
                    boolean flag = p_230107_ != null && WoodlandMansionPieces.MansionGrid.isHouse(p_230107_, j, i);
                    if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j, i) && !flag) {
                        p_230103_.add(mansionPiece( "roof", $$27.above(3), p_230105_));
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j + 1, i)) {
                            BlockPos blockpos1 = $$27.relative(p_230105_.rotate(Direction.EAST), 6);
                            p_230103_.add(mansionPiece( "roof_front", blockpos1, p_230105_));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j - 1, i)) {
                            BlockPos blockpos5 = $$27.relative(p_230105_.rotate(Direction.EAST), 0);
                            blockpos5 = blockpos5.relative(p_230105_.rotate(Direction.SOUTH), 7);
                            p_230103_.add(
                                mansionPiece(
                                     "roof_front", blockpos5, p_230105_.getRotated(Rotation.CLOCKWISE_180)
                                )
                            );
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j, i - 1)) {
                            BlockPos blockpos6 = $$27.relative(p_230105_.rotate(Direction.WEST), 1);
                            p_230103_.add(
                                mansionPiece(
                                     "roof_front", blockpos6, p_230105_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                )
                            );
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j, i + 1)) {
                            BlockPos blockpos7 = $$27.relative(p_230105_.rotate(Direction.EAST), 6);
                            blockpos7 = blockpos7.relative(p_230105_.rotate(Direction.SOUTH), 6);
                            p_230103_.add(
                                mansionPiece(
                                     "roof_front", blockpos7, p_230105_.getRotated(Rotation.CLOCKWISE_90)
                                )
                            );
                        }
                    }
                }
            }

            if (p_230107_ != null) {
                for (int k = 0; k < p_230106_.height; k++) {
                    for (int i1 = 0; i1 < p_230106_.width; i1++) {
                        BlockPos blockpos3 = p_230104_.relative(p_230105_.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                        blockpos3 = blockpos3.relative(p_230105_.rotate(Direction.EAST), (i1 - this.startX) * 8);
                        boolean flag1 = WoodlandMansionPieces.MansionGrid.isHouse(p_230107_, i1, k);
                        if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k) && flag1) {
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1 + 1, k)) {
                                BlockPos blockpos8 = blockpos3.relative(p_230105_.rotate(Direction.EAST), 7);
                                p_230103_.add(mansionPiece( "small_wall", blockpos8, p_230105_));
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1 - 1, k)) {
                                BlockPos blockpos9 = blockpos3.relative(p_230105_.rotate(Direction.WEST), 1);
                                blockpos9 = blockpos9.relative(p_230105_.rotate(Direction.SOUTH), 6);
                                p_230103_.add(
                                    mansionPiece(
                                         "small_wall", blockpos9, p_230105_.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k - 1)) {
                                BlockPos blockpos10 = blockpos3.relative(p_230105_.rotate(Direction.WEST), 0);
                                blockpos10 = blockpos10.relative(p_230105_.rotate(Direction.NORTH), 1);
                                p_230103_.add(
                                    mansionPiece(
                                         "small_wall", blockpos10, p_230105_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k + 1)) {
                                BlockPos blockpos11 = blockpos3.relative(p_230105_.rotate(Direction.EAST), 6);
                                blockpos11 = blockpos11.relative(p_230105_.rotate(Direction.SOUTH), 7);
                                p_230103_.add(
                                    mansionPiece(
                                         "small_wall", blockpos11, p_230105_.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1 + 1, k)) {
                                if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k - 1)) {
                                    BlockPos blockpos12 = blockpos3.relative(p_230105_.rotate(Direction.EAST), 7);
                                    blockpos12 = blockpos12.relative(p_230105_.rotate(Direction.NORTH), 2);
                                    p_230103_.add(
                                        mansionPiece(
                                             "small_wall_corner", blockpos12, p_230105_
                                        )
                                    );
                                }

                                if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k + 1)) {
                                    BlockPos blockpos13 = blockpos3.relative(p_230105_.rotate(Direction.EAST), 8);
                                    blockpos13 = blockpos13.relative(p_230105_.rotate(Direction.SOUTH), 7);
                                    p_230103_.add(
                                        mansionPiece(
                                             "small_wall_corner", blockpos13, p_230105_.getRotated(Rotation.CLOCKWISE_90)
                                        )
                                    );
                                }
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1 - 1, k)) {
                                if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k - 1)) {
                                    BlockPos blockpos14 = blockpos3.relative(p_230105_.rotate(Direction.WEST), 2);
                                    blockpos14 = blockpos14.relative(p_230105_.rotate(Direction.NORTH), 1);
                                    p_230103_.add(
                                        mansionPiece(
                                             "small_wall_corner", blockpos14, p_230105_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                        )
                                    );
                                }

                                if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, i1, k + 1)) {
                                    BlockPos blockpos15 = blockpos3.relative(p_230105_.rotate(Direction.WEST), 1);
                                    blockpos15 = blockpos15.relative(p_230105_.rotate(Direction.SOUTH), 8);
                                    p_230103_.add(
                                        mansionPiece(
                                             "small_wall_corner", blockpos15, p_230105_.getRotated(Rotation.CLOCKWISE_180)
                                        )
                                    );
                                }
                            }
                        }
                    }
                }
            }

            for (int l = 0; l < p_230106_.height; l++) {
                for (int j1 = 0; j1 < p_230106_.width; j1++) {
                    BlockPos blockpos4 = p_230104_.relative(p_230105_.rotate(Direction.SOUTH), 8 + (l - this.startY) * 8);
                    blockpos4 = blockpos4.relative(p_230105_.rotate(Direction.EAST), (j1 - this.startX) * 8);
                    boolean flag2 = p_230107_ != null && WoodlandMansionPieces.MansionGrid.isHouse(p_230107_, j1, l);
                    if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1, l) && !flag2) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 + 1, l)) {
                            BlockPos blockpos16 = blockpos4.relative(p_230105_.rotate(Direction.EAST), 6);
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1, l + 1)) {
                                BlockPos blockpos2 = blockpos16.relative(p_230105_.rotate(Direction.SOUTH), 6);
                                p_230103_.add(
                                    mansionPiece( "roof_corner", blockpos2, p_230105_)
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 + 1, l + 1)) {
                                BlockPos blockpos18 = blockpos16.relative(p_230105_.rotate(Direction.SOUTH), 5);
                                p_230103_.add(
                                    mansionPiece( "roof_inner_corner", blockpos18, p_230105_)
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1, l - 1)) {
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_corner", blockpos16, p_230105_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 + 1, l - 1)) {
                                BlockPos blockpos19 = blockpos4.relative(p_230105_.rotate(Direction.EAST), 9);
                                blockpos19 = blockpos19.relative(p_230105_.rotate(Direction.NORTH), 2);
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_inner_corner", blockpos19, p_230105_.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            }
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 - 1, l)) {
                            BlockPos blockpos17 = blockpos4.relative(p_230105_.rotate(Direction.EAST), 0);
                            blockpos17 = blockpos17.relative(p_230105_.rotate(Direction.SOUTH), 0);
                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1, l + 1)) {
                                BlockPos blockpos20 = blockpos17.relative(p_230105_.rotate(Direction.SOUTH), 6);
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_corner", blockpos20, p_230105_.getRotated(Rotation.CLOCKWISE_90)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 - 1, l + 1)) {
                                BlockPos blockpos21 = blockpos17.relative(p_230105_.rotate(Direction.SOUTH), 8);
                                blockpos21 = blockpos21.relative(p_230105_.rotate(Direction.WEST), 3);
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_inner_corner", blockpos21, p_230105_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                                    )
                                );
                            }

                            if (!WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1, l - 1)) {
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_corner", blockpos17, p_230105_.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            } else if (WoodlandMansionPieces.MansionGrid.isHouse(p_230106_, j1 - 1, l - 1)) {
                                BlockPos blockpos22 = blockpos17.relative(p_230105_.rotate(Direction.SOUTH), 1);
                                p_230103_.add(
                                    mansionPiece(
                                         "roof_inner_corner", blockpos22, p_230105_.getRotated(Rotation.CLOCKWISE_180)
                                    )
                                );
                            }
                        }
                    }
                }
            }
        }

        private void entrance(List<SurfaceStructures.Piece> p_230086_, WoodlandMansionPieces.PlacementData p_230087_) {
            Direction direction = p_230087_.rotation.rotate(Direction.WEST);
            p_230086_.add(
                mansionPiece(
                     "entrance", p_230087_.position.relative(direction, 9), p_230087_.rotation
                )
            );
            p_230087_.position = p_230087_.position.relative(p_230087_.rotation.rotate(Direction.SOUTH), 16);
        }

        private void traverseWallPiece(List<SurfaceStructures.Piece> p_230130_, WoodlandMansionPieces.PlacementData p_230131_) {
            p_230130_.add(
                mansionPiece(
                    
                    p_230131_.wallType,
                    p_230131_.position.relative(p_230131_.rotation.rotate(Direction.EAST), 7),
                    p_230131_.rotation
                )
            );
            p_230131_.position = p_230131_.position.relative(p_230131_.rotation.rotate(Direction.SOUTH), 8);
        }

        private void traverseTurn(List<SurfaceStructures.Piece> p_230133_, WoodlandMansionPieces.PlacementData p_230134_) {
            p_230134_.position = p_230134_.position.relative(p_230134_.rotation.rotate(Direction.SOUTH), -1);
            p_230133_.add(mansionPiece( "wall_corner", p_230134_.position, p_230134_.rotation));
            p_230134_.position = p_230134_.position.relative(p_230134_.rotation.rotate(Direction.SOUTH), -7);
            p_230134_.position = p_230134_.position.relative(p_230134_.rotation.rotate(Direction.WEST), -6);
            p_230134_.rotation = p_230134_.rotation.getRotated(Rotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<SurfaceStructures.Piece> p_230136_, WoodlandMansionPieces.PlacementData p_230137_) {
            p_230137_.position = p_230137_.position.relative(p_230137_.rotation.rotate(Direction.SOUTH), 6);
            p_230137_.position = p_230137_.position.relative(p_230137_.rotation.rotate(Direction.EAST), 8);
            p_230137_.rotation = p_230137_.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(
            List<SurfaceStructures.Piece> p_230109_,
            BlockPos p_230110_,
            Rotation p_230111_,
            Direction p_230112_,
            WoodlandMansionPieces.FloorRoomCollection p_230113_
        ) {
            Rotation rotation = Rotation.NONE;
            String s = p_230113_.get1x1(this.random);
            if (p_230112_ != Direction.EAST) {
                if (p_230112_ == Direction.NORTH) {
                    rotation = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
                } else if (p_230112_ == Direction.WEST) {
                    rotation = rotation.getRotated(Rotation.CLOCKWISE_180);
                } else if (p_230112_ == Direction.SOUTH) {
                    rotation = rotation.getRotated(Rotation.CLOCKWISE_90);
                } else {
                    s = p_230113_.get1x1Secret(this.random);
                }
            }

            BlockPos blockpos = BlockPos.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation, 7, 7);
            rotation = rotation.getRotated(p_230111_);
            blockpos = blockpos.rotate(p_230111_);
            BlockPos blockpos1 = p_230110_.offset(blockpos.getX(), 0, blockpos.getZ());
            p_230109_.add(mansionPiece( s, blockpos1, rotation));
        }

        private void addRoom1x2(
            List<SurfaceStructures.Piece> p_230122_,
            BlockPos p_230123_,
            Rotation p_230124_,
            Direction p_230125_,
            Direction p_230126_,
            WoodlandMansionPieces.FloorRoomCollection p_230127_,
            boolean p_230128_
        ) {
            if (p_230126_ == Direction.EAST && p_230125_ == Direction.SOUTH) {
                BlockPos blockpos13 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                p_230122_.add(
                    mansionPiece(
                         p_230127_.get1x2SideEntrance(this.random, p_230128_), blockpos13, p_230124_
                    )
                );
            } else if (p_230126_ == Direction.EAST && p_230125_ == Direction.NORTH) {
                BlockPos blockpos12 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                blockpos12 = blockpos12.relative(p_230124_.rotate(Direction.SOUTH), 6);
                p_230122_.add(
                    mansionPiece(
                         p_230127_.get1x2SideEntrance(this.random, p_230128_), blockpos12, p_230124_, Mirror.LEFT_RIGHT
                    )
                );
            } else if (p_230126_ == Direction.WEST && p_230125_ == Direction.NORTH) {
                BlockPos blockpos11 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 7);
                blockpos11 = blockpos11.relative(p_230124_.rotate(Direction.SOUTH), 6);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2SideEntrance(this.random, p_230128_),
                        blockpos11,
                        p_230124_.getRotated(Rotation.CLOCKWISE_180)
                    )
                );
            } else if (p_230126_ == Direction.WEST && p_230125_ == Direction.SOUTH) {
                BlockPos blockpos10 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 7);
                p_230122_.add(
                    mansionPiece(
                         p_230127_.get1x2SideEntrance(this.random, p_230128_), blockpos10, p_230124_, Mirror.FRONT_BACK
                    )
                );
            } else if (p_230126_ == Direction.SOUTH && p_230125_ == Direction.EAST) {
                BlockPos blockpos9 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2SideEntrance(this.random, p_230128_),
                        blockpos9,
                        p_230124_.getRotated(Rotation.CLOCKWISE_90),
                        Mirror.LEFT_RIGHT
                    )
                );
            } else if (p_230126_ == Direction.SOUTH && p_230125_ == Direction.WEST) {
                BlockPos blockpos8 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 7);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2SideEntrance(this.random, p_230128_),
                        blockpos8,
                        p_230124_.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (p_230126_ == Direction.NORTH && p_230125_ == Direction.WEST) {
                BlockPos blockpos7 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 7);
                blockpos7 = blockpos7.relative(p_230124_.rotate(Direction.SOUTH), 6);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2SideEntrance(this.random, p_230128_),
                        blockpos7,
                        p_230124_.getRotated(Rotation.CLOCKWISE_90),
                        Mirror.FRONT_BACK
                    )
                );
            } else if (p_230126_ == Direction.NORTH && p_230125_ == Direction.EAST) {
                BlockPos blockpos6 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                blockpos6 = blockpos6.relative(p_230124_.rotate(Direction.SOUTH), 6);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2SideEntrance(this.random, p_230128_),
                        blockpos6,
                        p_230124_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                    )
                );
            } else if (p_230126_ == Direction.SOUTH && p_230125_ == Direction.NORTH) {
                BlockPos blockpos5 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                blockpos5 = blockpos5.relative(p_230124_.rotate(Direction.NORTH), 8);
                p_230122_.add(
                    mansionPiece(
                         p_230127_.get1x2FrontEntrance(this.random, p_230128_), blockpos5, p_230124_
                    )
                );
            } else if (p_230126_ == Direction.NORTH && p_230125_ == Direction.SOUTH) {
                BlockPos blockpos4 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 7);
                blockpos4 = blockpos4.relative(p_230124_.rotate(Direction.SOUTH), 14);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2FrontEntrance(this.random, p_230128_),
                        blockpos4,
                        p_230124_.getRotated(Rotation.CLOCKWISE_180)
                    )
                );
            } else if (p_230126_ == Direction.WEST && p_230125_ == Direction.EAST) {
                BlockPos blockpos3 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 15);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2FrontEntrance(this.random, p_230128_),
                        blockpos3,
                        p_230124_.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (p_230126_ == Direction.EAST && p_230125_ == Direction.WEST) {
                BlockPos blockpos2 = p_230123_.relative(p_230124_.rotate(Direction.WEST), 7);
                blockpos2 = blockpos2.relative(p_230124_.rotate(Direction.SOUTH), 6);
                p_230122_.add(
                    mansionPiece(
                        
                        p_230127_.get1x2FrontEntrance(this.random, p_230128_),
                        blockpos2,
                        p_230124_.getRotated(Rotation.COUNTERCLOCKWISE_90)
                    )
                );
            } else if (p_230126_ == Direction.UP && p_230125_ == Direction.EAST) {
                BlockPos blockpos1 = p_230123_.relative(p_230124_.rotate(Direction.EAST), 15);
                p_230122_.add(
                    mansionPiece(
                         p_230127_.get1x2Secret(this.random), blockpos1, p_230124_.getRotated(Rotation.CLOCKWISE_90)
                    )
                );
            } else if (p_230126_ == Direction.UP && p_230125_ == Direction.SOUTH) {
                BlockPos blockpos = p_230123_.relative(p_230124_.rotate(Direction.EAST), 1);
                blockpos = blockpos.relative(p_230124_.rotate(Direction.NORTH), 0);
                p_230122_.add(
                    mansionPiece( p_230127_.get1x2Secret(this.random), blockpos, p_230124_)
                );
            }
        }

        private void addRoom2x2(
            List<SurfaceStructures.Piece> p_230115_,
            BlockPos p_230116_,
            Rotation p_230117_,
            Direction p_230118_,
            Direction p_230119_,
            WoodlandMansionPieces.FloorRoomCollection p_230120_
        ) {
            int i = 0;
            int j = 0;
            Rotation rotation = p_230117_;
            Mirror mirror = Mirror.NONE;
            if (p_230119_ == Direction.EAST && p_230118_ == Direction.SOUTH) {
                i = -7;
            } else if (p_230119_ == Direction.EAST && p_230118_ == Direction.NORTH) {
                i = -7;
                j = 6;
                mirror = Mirror.LEFT_RIGHT;
            } else if (p_230119_ == Direction.NORTH && p_230118_ == Direction.EAST) {
                i = 1;
                j = 14;
                rotation = p_230117_.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (p_230119_ == Direction.NORTH && p_230118_ == Direction.WEST) {
                i = 7;
                j = 14;
                rotation = p_230117_.getRotated(Rotation.COUNTERCLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (p_230119_ == Direction.SOUTH && p_230118_ == Direction.WEST) {
                i = 7;
                j = -8;
                rotation = p_230117_.getRotated(Rotation.CLOCKWISE_90);
            } else if (p_230119_ == Direction.SOUTH && p_230118_ == Direction.EAST) {
                i = 1;
                j = -8;
                rotation = p_230117_.getRotated(Rotation.CLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (p_230119_ == Direction.WEST && p_230118_ == Direction.NORTH) {
                i = 15;
                j = 6;
                rotation = p_230117_.getRotated(Rotation.CLOCKWISE_180);
            } else if (p_230119_ == Direction.WEST && p_230118_ == Direction.SOUTH) {
                i = 15;
                mirror = Mirror.FRONT_BACK;
            }

            BlockPos blockpos = p_230116_.relative(p_230117_.rotate(Direction.EAST), i);
            blockpos = blockpos.relative(p_230117_.rotate(Direction.SOUTH), j);
            p_230115_.add(
                mansionPiece( p_230120_.get2x2(this.random), blockpos, rotation, mirror)
            );
        }

        private void addRoom2x2Secret(
            List<SurfaceStructures.Piece> p_230098_,
            BlockPos p_230099_,
            Rotation p_230100_,
            WoodlandMansionPieces.FloorRoomCollection p_230101_
        ) {
            BlockPos blockpos = p_230099_.relative(p_230100_.rotate(Direction.EAST), 1);
            p_230098_.add(
                mansionPiece(
                     p_230101_.get2x2Secret(this.random), blockpos, p_230100_, Mirror.NONE
                )
            );
        }
    }

    static class PlacementData {
        public Rotation rotation;
        public BlockPos position;
        public String wallType;
    }

    static class SecondFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
        @Override
        public String get1x1(Random p_230144_) {
            return "1x1_b" + (p_230144_.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(Random p_230149_) {
            return "1x1_as" + (p_230149_.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(Random p_230146_, boolean p_230147_) {
            return p_230147_ ? "1x2_c_stairs" : "1x2_c" + (p_230146_.nextInt(4) + 1);
        }

        @Override
        public String get1x2FrontEntrance(Random p_230151_, boolean p_230152_) {
            return p_230152_ ? "1x2_d_stairs" : "1x2_d" + (p_230151_.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(Random p_230154_) {
            return "1x2_se" + (p_230154_.nextInt(1) + 1);
        }

        @Override
        public String get2x2(Random p_230156_) {
            return "2x2_b" + (p_230156_.nextInt(5) + 1);
        }

        @Override
        public String get2x2Secret(Random p_230158_) {
            return "2x2_s1";
        }
    }

    static class SimpleGrid {
        private final int[][] grid;
        final int width;
        final int height;
        private final int valueIfOutside;

        public SimpleGrid(int p_230164_, int p_230165_, int p_230166_) {
            this.width = p_230164_;
            this.height = p_230165_;
            this.valueIfOutside = p_230166_;
            this.grid = new int[p_230164_][p_230165_];
        }

        public void set(int p_230171_, int p_230172_, int p_230173_) {
            if (p_230171_ >= 0 && p_230171_ < this.width && p_230172_ >= 0 && p_230172_ < this.height) {
                this.grid[p_230171_][p_230172_] = p_230173_;
            }
        }

        public void set(int p_230180_, int p_230181_, int p_230182_, int p_230183_, int p_230184_) {
            for (int i = p_230181_; i <= p_230183_; i++) {
                for (int j = p_230180_; j <= p_230182_; j++) {
                    this.set(j, i, p_230184_);
                }
            }
        }

        public int get(int p_230168_, int p_230169_) {
            return p_230168_ >= 0 && p_230168_ < this.width && p_230169_ >= 0 && p_230169_ < this.height
                ? this.grid[p_230168_][p_230169_]
                : this.valueIfOutside;
        }

        public void setif(int p_230175_, int p_230176_, int p_230177_, int p_230178_) {
            if (this.get(p_230175_, p_230176_) == p_230177_) {
                this.set(p_230175_, p_230176_, p_230178_);
            }
        }

        public boolean edgesTo(int p_230186_, int p_230187_, int p_230188_) {
            return this.get(p_230186_ - 1, p_230187_) == p_230188_
                || this.get(p_230186_ + 1, p_230187_) == p_230188_
                || this.get(p_230186_, p_230187_ + 1) == p_230188_
                || this.get(p_230186_, p_230187_ - 1) == p_230188_;
        }
    }

    static class ThirdFloorRoomCollection extends WoodlandMansionPieces.SecondFloorRoomCollection {
    }

    private static SurfaceStructures.Piece mansionPiece(String name, BlockPos pos, Rotation rotation) { return mansionPiece(name, pos, rotation, Mirror.NONE); }
    private static SurfaceStructures.Piece mansionPiece(String name, BlockPos pos, Rotation rotation, Mirror mirror) {
        return new SurfaceStructures.Piece("woodland_mansion/" + name, pos.x, pos.y, pos.z, rotation.ordinal(), mirror.ordinal(), 0);
    }
    enum Mirror { NONE, LEFT_RIGHT, FRONT_BACK }
    enum Rotation {
        NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90;
        Rotation getRotated(Rotation other) { return values()[(ordinal() + other.ordinal()) & 3]; }
        Direction rotate(Direction d) { if (d == Direction.UP || d == Direction.DOWN) return d; for (int i = 0; i < ordinal(); i++) d = d.getClockWise(); return d; }
    }
    enum Direction {
        DOWN(0,0), UP(0,0), NORTH(0,-1), SOUTH(0,1), WEST(-1,0), EAST(1,0);
        final int x,z;
        Direction(int x,int z) { this.x=x; this.z=z; }
        int getStepX() { return x; } int getStepZ() { return z; }
        Direction getOpposite() { return this==UP?DOWN:this==DOWN?UP:this==NORTH?SOUTH:this==SOUTH?NORTH:this==EAST?WEST:EAST; }
        Direction getClockWise() { return this==NORTH?EAST:this==EAST?SOUTH:this==SOUTH?WEST:NORTH; }
        Direction getCounterClockWise() { return getClockWise().getOpposite(); }
        static Direction from2DDataValue(int n) { return new Direction[]{SOUTH,WEST,NORTH,EAST}[n&3]; }
        static final class Plane { static final Direction[] HORIZONTAL = {NORTH,EAST,SOUTH,WEST}; }
    }
    static final class Tuple<A,B> {
        final A a; final B b; Tuple(A a,B b) { this.a=a; this.b=b; } A getA() { return a; } B getB() { return b; }
    }
    static final class BlockPos {
        final int x,y,z;
        BlockPos(int x,int y,int z) { this.x=x; this.y=y; this.z=z; }
        int getX() { return x; } int getZ() { return z; }
        BlockPos relative(Direction d) { return relative(d,1); }
        BlockPos relative(Direction d,int n) { return offset(d.x*n, d==Direction.UP?n:d==Direction.DOWN?-n:0, d.z*n); }
        BlockPos above() { return above(1); } BlockPos above(int n) { return offset(0,n,0); }
        BlockPos offset(int a,int b,int c) { return new BlockPos(x+a,y+b,z+c); }
        BlockPos rotate(Rotation r) { switch(r) { case CLOCKWISE_90:return new BlockPos(-z,y,x); case CLOCKWISE_180:return new BlockPos(-x,y,-z); case COUNTERCLOCKWISE_90:return new BlockPos(z,y,-x); default:return this; } }
        static BlockPos getZeroPositionWithTransform(BlockPos p, Mirror m, Rotation r, int width, int depth) {
            int x=m==Mirror.FRONT_BACK?width-1:0, z=m==Mirror.LEFT_RIGHT?depth-1:0;
            switch(r) { case CLOCKWISE_90:return p.offset(depth-1-z,0,x); case CLOCKWISE_180:return p.offset(width-1-x,0,depth-1-z); case COUNTERCLOCKWISE_90:return p.offset(z,0,width-1-x); default:return p.offset(x,0,z); }
        }
    }
}
