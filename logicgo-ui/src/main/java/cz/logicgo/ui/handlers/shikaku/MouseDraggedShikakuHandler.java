package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.MouseEvent;

import static cz.logicgo.core.gameClasses.shikaku.ShikakuUtils.calculateValidEndCell;


public class MouseDraggedShikakuHandler extends ShikakuHandlerBase {

    public MouseDraggedShikakuHandler(Shikaku game, ShikakuGameController controller) {
        super(game, controller);
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        ShikakuGameController con = getShikakuGameController();
        var cState = con.getCreationState();

        con.updateMousePosition(mouseEvent.getX(), mouseEvent.getY());
        Shikaku game = con.getGameInstance();

        ShikakuCell startCell = cState.getStartCell();
        if (startCell == null) return;

        getCellAt(con, game, mouseEvent.getX(), mouseEvent.getY()).ifPresent(targetCell -> {
            ShikakuCell validEndCell = calculateValidEndCell(startCell, targetCell);
            if (!validEndCell.equals(cState.getCurrentEndCell())) {
                cState.setCurrentEndCell(validEndCell);
                con.redrawCanvases();
            }
        });

        con.redrawCanvases();
    }
}
