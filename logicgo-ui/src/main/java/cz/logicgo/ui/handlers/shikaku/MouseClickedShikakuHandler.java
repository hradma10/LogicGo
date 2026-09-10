package cz.logicgo.ui.handlers.shikaku;

import cz.logicgo.core.entity.games.shikaku.Shikaku;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.gameClasses.shikaku.ShikakuRectangle;
import cz.logicgo.core.misc.enums.hints.ShikakuHintType;
import cz.logicgo.ui.commands.Command;
import cz.logicgo.ui.commands.shikakuCommands.AddRectangleCommand;
import cz.logicgo.ui.commands.shikakuCommands.RemoveRectangleCommand;
import cz.logicgo.ui.controllers.gameControllers.gameControllers.ShikakuGameController;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Optional;

public class MouseClickedShikakuHandler extends ShikakuHandlerBase {

    public MouseClickedShikakuHandler(Shikaku game, ShikakuGameController controller) {
        super(game, controller);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        var con = getShikakuGameController();
        con.stopHintFeedbackTimer();

        if (!con.isHintChoice()) {
            con.clearHintVisuals();
        }

        boolean skipGlobalRedraw = false;

        switch (mouseEvent.getButton()) {
            case PRIMARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    skipGlobalRedraw = onSingleClickPrimary(mouseEvent);
                }
            }
            case SECONDARY -> {
                if (mouseEvent.getClickCount() == 1) {
                    onSingleClickSecondary(mouseEvent);
                }
            }
            default -> {
            }
        }

        if (!skipGlobalRedraw) {
            con.redrawCanvases();
        }
    }

    private boolean onSingleClickPrimary(MouseEvent mouseEvent) {
        ShikakuGameController con = getShikakuGameController();
        Optional<ShikakuCell> cellAt = getCellAt(con, getShikakuGame(), mouseEvent.getX(), mouseEvent.getY());

        if (con.isHintChoice()) {
            if (cellAt.isPresent()) {
                handleHint(cellAt.get());
                return false;
            }
            return false;
        }

        return false;
    }

    private void onSingleClickSecondary(MouseEvent mouseEvent) {
        ShikakuGameController con = getShikakuGameController();

        if (con.isHintChoice()) {
            con.setHintChoice(false);
            con.setActiveHint(null);
            con.clearHintVisuals();
            return;
        }

        getCellAt(con, getShikakuGame(), mouseEvent.getX(), mouseEvent.getY()).flatMap(this::getRectangleAt).ifPresent(rect -> {
            RemoveRectangleCommand cmd = new RemoveRectangleCommand(getShikakuGame(), rect);
            con.getCommandExecutor().execute(cmd);
            con.getTabState().setChangePending();
        });
        con.redrawCanvases();
    }

    private ShikakuHintType handleHint(ShikakuCell selectedCell) {
        ShikakuGameController con = getShikakuGameController();
        Shikaku game = getShikakuGame();
        List<ShikakuRectangle> solutionRects = game.getSolutionRectangles();

        if (con.getActiveHint() == null) return null;

        boolean hintFinished = true;
        ShikakuHintType hintType = con.getActiveHint();

        switch (hintType) {
            case CHECK_VALIDITY -> {
                Optional<ShikakuRectangle> clickedRect = getRectangleAt(selectedCell);
                if (clickedRect.isPresent()) {
                    ShikakuRectangle rect = clickedRect.get();

                    boolean isCorrect = solutionRects.stream().anyMatch(sol ->
                            sol.getMinRow() == rect.getMinRow() &&
                                    sol.getMinCol() == rect.getMinCol() &&
                                    sol.getMaxRow() == rect.getMaxRow() &&
                                    sol.getMaxCol() == rect.getMaxCol());

                    if (!isCorrect) {
                        rect.setHintWrong(true);
                        con.startHintFeedbackTimer(2.5, () -> {
                            rect.setHintWrong(false);
                            con.redrawCanvases();
                        });
                    } else {
                        con.startHintFeedbackTimer(2.5, con::clearHintVisuals);
                    }
                } else {
                    hintFinished = false;
                }
            }
            case INSERT_RECTANGLE_TO_NUMBER -> {
                if (selectedCell.getClue() > 0) {
                    Optional<ShikakuRectangle> correctRectOpt = solutionRects.stream()
                            .filter(r -> selectedCell.getRow() >= r.getMinRow() &&
                                    selectedCell.getRow() <= r.getMaxRow() &&
                                    selectedCell.getCol() >= r.getMinCol() &&
                                    selectedCell.getCol() <= r.getMaxCol())
                            .findFirst();

                    if (correctRectOpt.isPresent()) {
                        ShikakuRectangle solRect = correctRectOpt.get();
                        int newId = game.generateNextRectangleId();

                        var startCell = game.getCell(solRect.getMinRow(), solRect.getMinCol());
                        var endCell = game.getCell(solRect.getMaxRow(), solRect.getMaxCol());

                        ShikakuRectangle rectToInsert = new ShikakuRectangle(newId, startCell, endCell);
                        List<ShikakuRectangle> intersectingRectangles = game.getRectangles().stream()
                                .filter(r -> r.intersects(rectToInsert))
                                .toList();

                        Command cmd = new AddRectangleCommand(game, rectToInsert, intersectingRectangles);
                        con.getCommandExecutor().execute(cmd);
                        con.getTabState().setChangePending();
                    } else {
                        hintFinished = false;
                    }
                } else {
                    hintFinished = false;
                }
            }
            default -> hintFinished = false;
        }

        if (hintFinished) {
            con.getCreationState().unset();
            con.setHintChoice(false);
            con.setActiveHint(null);

            con.redrawCanvases();

            return hintType;
        }

        return null;
    }
}
