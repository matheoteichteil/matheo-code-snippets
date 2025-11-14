package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * World
 *
 * Generates a small region of a dungeon-style world.
 * - Randomly creates rectangular rooms on a shared global board.
 * - Connects rooms with L-shaped hallways.
 * - Uses a Prim-like algorithm to connect rooms with short paths
 *   (spanning tree over room centers).
 *
 * This class operates on a shared TETile[][] board so that multiple
 * World instances can be stitched together into a larger map.
 */

public class World {
    
    private static int SIZE = 10;
    private int originX;
    private int originY;
    private TETile[][] board;
    private Random rand;
    private ArrayList<Room> rooms;

    /**
     * Constructs a new world chunk.
     *
     * @param originX     x-coordinate of the chunk origin
     * @param originY     y-coordinate of the chunk origin
     * @param globalBoard shared tile board
     * @param rand        random generator for reproducibility
     */
  
    public World(int originX, int originY, TETile[][] globalBoard, Random rand) {
        this.originX = originX;
        this.originY = originY;
        this.board = globalBoard;
        this.rand = rand;
        this.rooms = new ArrayList<>();
        int numRooms = 1; 
        generateRooms(numRooms);
    }

    /**
     * Returns an arbitrary "anchor" room for this chunk.
     * Useful when other chunks want a connection point.
     */
  
    public Room getAnchorRoom() {
        if (rooms.isEmpty()) {
            return null;
        }
        return rooms.get(0);
    }

    /**
     * Attempts to generate the requested number of rooms.
     * If several attempts fail due to overlap, it stops after
     * a bounded number of tries. Optionally connects rooms
     * with hallways once created.
     */
  
    private void generateRooms(int numRooms) {
        int attempts = 0;
        while (rooms.size() < numRooms && attempts < numRooms * 5) {
            Room r = createRoom();
            if (r != null) {
                rooms.add(r);
            }
            attempts++;
        }
        if (rooms.size() >= 2) {
            connectWithPrims(rooms);
        }
    }

    /**
     * Tries to create a single rectangular room.
     * Returns null if the room would overlap existing tiles.
     */
  
    public Room createRoom() {
        int sizeWidth = RandomUtils.uniform(rand, 2, 4);
        int sizeHeight = RandomUtils.uniform(rand, 2, 4);
        int startX = RandomUtils.uniform(rand,
                originX + 1, originX + SIZE - sizeWidth - 1);
        int startY = RandomUtils.uniform(rand,
                originY + 1, originY + SIZE - sizeHeight - 1);
      
        for (int x = startX - 1; x <= startX + sizeWidth; x++) {
            for (int y = startY - 1; y <= startY + sizeHeight; y++) {
                if (board[x][y] != Tileset.NOTHING) {
                    return null;
                }
            }
        }
      
        for (int x = startX - 1; x <= startX + sizeWidth; x++) {
            for (int y = startY - 1; y <= startY + sizeHeight; y++) {
                boolean border = (x == startX - 1
                        || x == startX + sizeWidth
                        || y == startY - 1
                        || y == startY + sizeHeight);

                if (border) {
                    if (board[x][y] == Tileset.NOTHING) {
                        board[x][y] = Tileset.WALL;
                    }
                } else {
                    board[x][y] = Tileset.FLOOR;
                }
            }
        }
        return new Room(startX, startY, sizeWidth, sizeHeight);
    }

    /**
     * Simple immutable room descriptor, used for world connectivity.
     */
  
    public class Room {
        public int startX;
        public int startY;
        public int width;
        public int height;
        public int centerX;
        public int centerY;

        public Room(int startX, int startY, int width, int height) {
            this.startX = startX;
            this.startY = startY;
            this.width = width;
            this.height = height;
            this.centerX = startX + width / 2;
            this.centerY = startY + height / 2;
        }

        /**
         * Manhattan distance between this room and another room,
         * based on their centers. Used as the edge weight.
         */
      
        public int distanceTo(Room r1) {
            int x1 = this.centerX;
            int y1 = this.centerY;
            int x2 = r1.centerX;
            int y2 = r1.centerY;
            return Math.abs(x1 - x2) + Math.abs(y1 - y2);
        }
    }

    /**
     * Creates an L-shaped hallway between two rooms by picking
     * appropriate exit points on their borders and then drawing
     * an orthogonal path between them.
     */
  
    public void createPath(Room r1, Room r2, TETile[][] board) {
        int x1, y1, x2, y2;

        // Choose doorway positions depending on relative orientation
        if (r2.centerX > r1.centerX) {
            // r2 is to the right of r1
            x1 = r1.startX + r1.width;
            y1 = r1.startY + r1.height / 2;
            x2 = r2.startX - 1;
            y2 = r2.startY + r2.height / 2;
        } else if (r1.centerX > r2.centerX) {
            // r2 is to the left of r1
            x1 = r1.startX - 1;
            y1 = r1.startY + r1.height / 2;
            x2 = r2.startX + r2.width;
            y2 = r2.startY + r2.height / 2;
        } else if (r2.centerY > r1.centerY) {
            // r2 is above r1
            x1 = r1.startX + r1.width / 2;
            y1 = r1.startY + r1.height;
            x2 = r2.startX + r2.width / 2;
            y2 = r2.startY - 1;
        } else {
            // r2 is below r1
            x1 = r1.startX + r1.width / 2;
            y1 = r1.startY - 1;
            x2 = r2.startX + r2.width / 2;
            y2 = r2.startY + r2.height;
        }

        drawLHallway(x1, y1, x2, y2);
    }

    /**
     * Draws an L-shaped hallway by first carving horizontally,
     * then vertically (or vice versa, depending on coordinates).
     */
  
    private void drawLHallway(int x1, int y1, int x2, int y2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            if (board[x][y1] == Tileset.NOTHING) {
                board[x][y1] = Tileset.FLOOR;
            }
        }
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
            if (board[x2][y] == Tileset.NOTHING) {
                board[x2][y] = Tileset.FLOOR;
            }
        }
    }

    /**
     * Edge between two rooms, weighted by Manhattan distance between centers.
     * Used in a priority queue for the Prim-like algorithm.
     */
  
    private class Edge implements Comparable<Edge> {
        Room r1;
        Room r2;
        int weight;

        private Edge(Room r1, Room r2) {
            this.r1 = r1;
            this.r2 = r2;
            this.weight = r1.distanceTo(r2);
        }

        @Override
        public int compareTo(Edge o) {
            return Integer.compare(this.weight, o.weight);
        }
    }

    /**
     * Connects all rooms using a Prim-style minimum spanning tree.
     * - Starts from an arbitrary room.
     * - Repeatedly picks the shortest edge from the connected
     *   component to an unconnected room.
     * - For each chosen edge, creates a hallway between the rooms.
     */
  
    public void connectWithPrims(ArrayList<Room> room) {
        HashSet<Room> connected = new HashSet<>();
        PriorityQueue<Edge> edges = new PriorityQueue<>();

        Room first = room.get(0);
        connected.add(first);

        for (Room r : room) {
            if (!connected.contains(r)) {
                edges.add(new Edge(first, r));
            }
        }
      
        while (connected.size() < room.size()) {
            Edge e = edges.poll();
            if (!connected.contains(e.r2)) {
                connected.add(e.r2);
                createPath(e.r1, e.r2, board);
            }
            for (Room r : room) {
                if (!connected.contains(r)) {
                    edges.add(new Edge(e.r2, r));
                }
            }
        }
    }
}
