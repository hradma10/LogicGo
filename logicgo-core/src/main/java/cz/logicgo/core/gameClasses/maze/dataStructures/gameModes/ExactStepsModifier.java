package cz.logicgo.core.gameClasses.maze.dataStructures.gameModes;


import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import cz.logicgo.core.gameClasses.maze.dataStructures.cell.MazeCell;
import cz.logicgo.core.gameClasses.maze.dataStructures.grid.MazeGrid;

import java.io.*;

public record ExactStepsModifier(int targetSteps) implements MazeModifier {
    final private static MazeType mazeType = MazeType.EXACT_STEPS;

    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeInt(targetSteps);

            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    @Override
    public int getId() {
        return mazeType.getId();
    }

    @Override
    public MazeModifier copy(MazeCell[][] grid) {
        return new ExactStepsModifier(this.targetSteps);
    }

    public static ExactStepsModifier deserialize(byte[] bytes, MazeGrid grid) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (dis.available() <= 0) throw new IOException("Empty Data");

            int targetSteps = dis.readInt();
            return new ExactStepsModifier(targetSteps);
        } catch (Exception e) {
            return null;
        }
    }
}
