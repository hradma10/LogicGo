package cz.logicgo.core.misc.enums.settings;


import cz.logicgo.core.misc.enums.NodeType;
import cz.logicgo.core.misc.enums.settings.modes.CellNotesMode;
import cz.logicgo.core.misc.enums.settings.modes.HighlightMode;

public enum SudokuSettings implements SettingKey {
    HINTS_ON("hintsOn", Boolean.class, true, NodeType.CHECKBOX),
    CELL_NOTES_MODE("cellNotes", CellNotesMode.class, CellNotesMode.NONE, NodeType.SEGMENTED_BUTTON),
    HIGHLIGHT_MODE("highlight", HighlightMode.class, HighlightMode.REGIONS, NodeType.SEGMENTED_BUTTON),
    HIGHLIGHT_CONFLICTS("highlight.conflicts", Boolean.class, true, NodeType.CHECKBOX),
    TIMER("timer", Boolean.class, true, NodeType.CHECKBOX),
    ;

    final String name;
    final NodeType nodeType;
    final private Class<?> clazz;
    final private Object defaultValue;
    SudokuSettings(String name, Class<?> clazz, Object defaultValue, NodeType nodeType) {
        this.name = name;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.nodeType = nodeType;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getNamespace() {
        return "sudoku";
    }

    @Override
    public String getQualifiedName() {
        return String.format("%s:%s", getNamespace(), getName());
    }

    @Override
    public String getTranslationBase() {
        return "sudokuSettings";
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }
}
