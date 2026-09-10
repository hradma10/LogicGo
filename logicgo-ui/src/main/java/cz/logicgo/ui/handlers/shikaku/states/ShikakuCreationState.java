package cz.logicgo.ui.handlers.shikaku.states;


import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;

public class ShikakuCreationState {

    private ShikakuCell startCell;
    private ShikakuCell currentEndCell;

    public ShikakuCreationState() {
        this.startCell = null;
        this.currentEndCell = null;
    }

    public ShikakuCell getStartCell() {
        return startCell;
    }

    public void setStartCell(ShikakuCell startCell) {
        this.startCell = startCell;
    }

    public ShikakuCell getCurrentEndCell() {
        return currentEndCell;
    }

    public void setCurrentEndCell(ShikakuCell currentEndCell) {
        this.currentEndCell = currentEndCell;
    }

    public void unset() {
        this.startCell = null;
        this.currentEndCell = null;
    }
}
