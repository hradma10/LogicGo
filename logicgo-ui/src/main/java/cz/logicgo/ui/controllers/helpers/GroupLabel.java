package cz.logicgo.ui.controllers.helpers;

public record GroupLabel(String name) {
    @Override
    public String toString() {
        return name;
    }
}
