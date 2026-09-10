package cz.logicgo.ui.utils;

import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class KeyEventUtils {

    private KeyEventUtils() {}

    public static KeyEventDTO fromJavaFx(KeyEvent event, HotkeyEvent hotkeyEvent) {
        String codeName = event.getCode() != null ? event.getCode().name() : "";
        String eventType = event.getEventType() != null ? event.getEventType().getName() : null;

        return new KeyEventDTO(
                codeName,
                event.getCharacter(),
                event.getText(),
                event.isShiftDown(),
                event.isControlDown(),
                event.isAltDown(),
                event.isMetaDown(),
                hotkeyEvent,
                eventType
        );
    }

    public static KeyEventDTO fromJavaFx(KeyEvent event) {
        return fromJavaFx(event, null);
    }

    public static KeyCode toKeyCode(KeyEventDTO dto) {
        if (dto == null || dto.getCodeName() == null || dto.getCodeName().isBlank()) {
            return KeyCode.UNDEFINED;
        }
        try {
            return KeyCode.valueOf(dto.getCodeName());
        } catch (IllegalArgumentException e) {
            return KeyCode.UNDEFINED;
        }
    }

    public static boolean matches(KeyEventDTO dto, KeyEvent event) {
        if (dto == null || event == null) return false;

        KeyCode expectedCode = toKeyCode(dto);
        return expectedCode == event.getCode() &&
               dto.isShiftDown() == event.isShiftDown() &&
               dto.isControlDown() == event.isControlDown() &&
               dto.isAltDown() == event.isAltDown() &&
               dto.isMetaDown() == event.isMetaDown();
    }
}
