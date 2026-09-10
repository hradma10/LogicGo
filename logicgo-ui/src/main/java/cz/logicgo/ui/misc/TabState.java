package cz.logicgo.ui.misc;

import cz.logicgo.ui.controllers.ControllerClosable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Tab;

import java.util.UUID;

public class TabState {
    private final BooleanProperty changeProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty doneProperty = new SimpleBooleanProperty(true);
    private final Tab associatedTab;
    private Long idOfGame = null;
    private ControllerClosable associatedController = null;
    private final UUID id = UUID.randomUUID();
    private Task<?> activeTask;
    private String taskDescription;
    private String seed = null;

    public void setActiveTask(Task<?> task, String description) {
        this.activeTask = task;
        this.taskDescription = description;
    }

    public Task<?> getActiveTask() {
        return activeTask;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public TabState(Tab associatedTab) {
        this.associatedTab = associatedTab;
    }

    public void terminateActions() {
        if (associatedController != null) {
            associatedController.terminateAllActiveActions();
        }
    }

    public BooleanProperty changeProperty() {
        return changeProperty;
    }

    public void setChangePending() {
        changeProperty.set(true);
    }

    public void unsetChangePending() {
        changeProperty.set(false);
    }

    public BooleanProperty doneProperty() {
        return doneProperty;
    }

    public boolean isChangePending() {
        return changeProperty.get();
    }

    public boolean isDone() {
        return doneProperty.get();
    }

    public void markSavingStarted() {
        doneProperty.set(false);
    }

    public void markSavingFinished() {
        changeProperty.set(false);
        doneProperty.set(true);
    }

    public Tab getAssociatedTab() {
        return associatedTab;
    }

    public Long getIdOfGame() {
        return idOfGame;
    }

    public void setIdOfGame(Long idOfGame) {
        this.idOfGame = idOfGame;
    }

    public ControllerClosable getAssociatedController() {
        return associatedController;
    }

    public TabState setAssociatedController(ControllerClosable associatedController) {
        this.associatedController = associatedController;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public String getSeed() {
        return seed;
    }

    public TabState setSeed(String seed) {
        this.seed = seed;
        return this;
    }
}
