package cz.logicgo.core.gameClasses.keys;

import cz.logicgo.core.misc.enums.keys.HotkeyEvent;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class KeyEventDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String codeName;
    private final String character;
    private final String text;
    private final boolean shiftDown;
    private final boolean controlDown;
    private final boolean altDown;
    private final boolean metaDown;
    private HotkeyEvent hotkeyEvent;
    private String eventType;

    public KeyEventDTO(String codeName, String character, String text,
                       boolean shiftDown, boolean controlDown, boolean altDown, boolean metaDown,
                       HotkeyEvent hotkeyEvent, String eventType) {
        this.codeName = codeName;
        this.character = character;
        this.text = text;
        this.shiftDown = shiftDown;
        this.controlDown = controlDown;
        this.altDown = altDown;
        this.metaDown = metaDown;
        this.hotkeyEvent = hotkeyEvent;
        this.eventType = eventType;
    }

    public KeyEventDTO(String codeName, String character, String text,
                       boolean shiftDown, boolean controlDown, boolean altDown, boolean metaDown,
                       HotkeyEvent hotkeyEvent) {
        this(codeName, character, text, shiftDown, controlDown, altDown, metaDown, hotkeyEvent, null);
    }

    public boolean matches(KeyEventDTO other) {
        if (other == null) return false;

        boolean codeMatches = Objects.equals(this.codeName, other.getCodeName());
        boolean modifiersMatch =
                this.shiftDown == other.isShiftDown() &&
                        this.controlDown == other.isControlDown() &&
                        this.altDown == other.isAltDown() &&
                        this.metaDown == other.isMetaDown();

        return codeMatches && modifiersMatch;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("+");

        if (controlDown) joiner.add("Ctrl");
        if (altDown) joiner.add("Alt");
        if (shiftDown) joiner.add("Shift");
        if (metaDown) joiner.add("Win");

        if (codeName != null && !codeName.isBlank()) {
            joiner.add(codeName);
        }

        return joiner.toString();
    }

    public String getCodeName() {
        return codeName;
    }

    public String getCharacter() {
        return character;
    }

    public String getText() {
        return text;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public boolean isMetaDown() {
        return metaDown;
    }

    public HotkeyEvent getKeystrokeEvent() {
        return hotkeyEvent;
    }

    public void setKeystrokeEvent(HotkeyEvent hotkeyEvent) {
        this.hotkeyEvent = hotkeyEvent;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyEventDTO that)) return false;
        return shiftDown == that.shiftDown &&
                controlDown == that.controlDown &&
                altDown == that.altDown &&
                metaDown == that.metaDown &&
                Objects.equals(codeName, that.codeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeName, shiftDown, controlDown, altDown, metaDown);
    }
}
