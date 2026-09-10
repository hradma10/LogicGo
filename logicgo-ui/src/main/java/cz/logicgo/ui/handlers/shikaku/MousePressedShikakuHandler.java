package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.MouseEvent;

public class MousePressedShikakuHandler extends ShikakuHandlerBase {

    public MousePressedShikakuHandler(Shikaku game, ShikakuGameController controller) {
        super(game, controller);
    }

    public void onMousePressed(MouseEvent mouseEvent) {
        if (!mouseEvent.isPrimaryButtonDown()) return;
        ShikakuGameController con = getShikakuGameController();

        if (con.isHintChoice()) {
            return;
        }

        var cState = con.getCreationState();
        getCellAt(con, getShikakuGame(), mouseEvent.getX(), mouseEvent.getY()).ifPresent(cell -> {
            cState.setStartCell(cell);
            cState.setCurrentEndCell(cell);
        });

        con.redrawCanvases();
    }
}
