package cz.logicgo.core.misc.enums.keys;

public enum GeneralEvent implements HotkeyEvent {
    ESCAPE("escape");

    final String name;

    GeneralEvent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return "general";
    }

    @Override
    public String getSectionTranslationKey() {
        return "settings.tab.general_event_hotkeys";
    }
}
