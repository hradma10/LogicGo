package cz.logicgo.ui.controllers;

public interface ControllerClosable {
    void terminateAllActiveActions();

    default void refreshContent() {

    }
}
