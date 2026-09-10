package cz.logicgo.core.misc.enums.gameTypes.maze.directions;

import java.util.List;

public enum RectangularDirection implements MazeDirection {
    NORTH(0, 0, -1),
    EAST(1, 1, 0),
    SOUTH(2, 0, 1),
    WEST(3, -1, 0);

    private final int id;
    private final int dx;
    private final int dy;
    private final byte mask;

    RectangularDirection(int id, int dx, int dy) {
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

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    @Override
    public List<? extends MazeDirection> instances() {
        return List.of(RectangularDirection.values());
    }
}
