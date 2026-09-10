package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.gameClasses.shikaku.ShikakuUtils;
import cz.logicgo.ui.commands.shikakuCommands.AddRectangleCommand;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.MouseEvent;

import java.util.List;

public class MouseReleasedShikakuHandler extends ShikakuHandlerBase {

    public MouseReleasedShikakuHandler(Shikaku game, ShikakuGameController controller) {
        super(game, controller);
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        ShikakuGameController con = getShikakuGameController();

        if (con.isHintChoice()) {
            return;
        }

        var game = getShikakuGame();
        var cState = con.getCreationState();
        if (cState.getStartCell() == null) return;

        ShikakuCell startCell = cState.getStartCell();
        ShikakuCell endCell = cState.getCurrentEndCell();

        cState.unset();

        int id = game.generateNextRectangleId();
        var rectangle = new ShikakuRectangle(id, startCell, endCell);
        List<ShikakuRectangle> rectangles = game.getRectangles().stream().filter(r -> r.intersects(rectangle)).toList();
        AddRectangleCommand cmd = new AddRectangleCommand(game, rectangle, rectangles);


        con.getCommandExecutor().execute(cmd);
        con.getTabState().setChangePending();
        if (ShikakuUtils.isGameFinished(getShikakuGame())) {
            con.gameFinished();
        }

        con.getTabState().setChangePending();
        con.redrawCanvases();
    }
}
