package cz.logicgo.ui.controllers.gameControllers.choosers;

public class ChosenMask implements IChooser {
    boolean[][] mask = null;

    public boolean[][] getMask() {
        return mask;
    }

    public ChosenMask setMask(boolean[][] mask) {
        this.mask = mask;
        return this;
    }
}
