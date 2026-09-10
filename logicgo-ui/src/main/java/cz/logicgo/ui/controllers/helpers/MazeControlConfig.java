package cz.logicgo.ui.controllers.helpers;

import cz.logicgo.core.misc.enums.gameTypes.maze.MazeAlgorithm;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeShape;
import cz.logicgo.core.misc.enums.gameTypes.maze.MazeType;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceBox;

public class MazeControlConfig {
    private Canvas canvas;
    private ChoiceBox<MazeType> typeChoiceBox;
    private ChoiceBox<MazeShape> shapeChoiceBox;
    private ChoiceBox<MazeAlgorithm> algorithmChoiceBox;
    private MazeType defType;

    public Canvas getCanvas() {
        return canvas;
    }

    public MazeControlConfig setCanvas(Canvas canvas) {
        this.canvas = canvas;
        return this;
    }

    public ChoiceBox<MazeShape> getShapeChoiceBox() {
        return shapeChoiceBox;
    }

    public MazeControlConfig setShapeChoiceBox(ChoiceBox<MazeShape> shapeChoiceBox) {
        this.shapeChoiceBox = shapeChoiceBox;
        return this;
    }

    public ChoiceBox<MazeAlgorithm> getAlgorithmChoiceBox() {
        return algorithmChoiceBox;
    }

    public MazeControlConfig setAlgorithmChoiceBox(ChoiceBox<MazeAlgorithm> algorithmChoiceBox) {
        this.algorithmChoiceBox = algorithmChoiceBox;
        return this;
    }

    public ChoiceBox<MazeType> getTypeChoiceBox() {
        return typeChoiceBox;
    }

    public MazeControlConfig setTypeChoiceBox(ChoiceBox<MazeType> typeChoiceBox) {
        this.typeChoiceBox = typeChoiceBox;
        return this;
    }

    public MazeType getDefType() {
        return defType;
    }

    public MazeControlConfig setDefType(MazeType defType) {
        this.defType = defType;
        return this;
    }
}
