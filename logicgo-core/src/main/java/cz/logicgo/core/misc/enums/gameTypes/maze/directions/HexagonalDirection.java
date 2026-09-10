package cz.logicgo.core.misc.enums.gameTypes.maze.directions;

import java.util.List;

public enum HexagonalDirection implements MazeDirection {
    NORTH_EAST(0, 1, -1),
    NORTH(1, 1, 0),
    SOUTH_EAST(2, 0, 1),
    SOUTH_WEST(3, -1, 1),
    SOUTH(4, -1, 0),
    NORTH_WEST(5, 0, -1);

    private final int id;
    private final int dx;
    private final int dy;
    private final byte mask;

    HexagonalDirection(int id, int dx, int dy) {
        this.id = id;
        this.dx = dx;
        this.dy = dy;
        this.mask = MazeDirection.maskOf(id);
    }

    public static MazeDirection getById(int id) {
        for (MazeDirection d : values()
        ) {
            if (d.getId() == id)
                return d;
        }
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public byte getMask() {
        return mask;
    }

    @Override
    public List<? extends MazeDirection> instances() {
        return List.of(HexagonalDirection.values());
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
