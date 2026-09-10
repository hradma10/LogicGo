package cz.logicgo.ui.controllers.settingsControllers;

import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import cz.logicgo.core.misc.enums.keys.game.sudoku.SudokuEvents;
import cz.logicgo.ui.misc.windows.AlertBox;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.ui.utils.KeyEventUtils.fromJavaFx;

public class HotkeySettingsUIGenerator {

    public static Button createKeyCaptureButton(HotkeyEvent event, Map<HotkeyEvent, KeyEventDTO> hotkeyBuffer, Map<HotkeyEvent, KeyEventDTO> savedHotkeys) {
        KeyEventDTO currentKey = hotkeyBuffer.getOrDefault(event, savedHotkeys.get(event));

        String btnText = (currentKey != null) ? currentKey.toString() : "NONE";
        Button btn = new Button(btnText);
        btn.getStyleClass().add("settings-keybind-btn");
        btn.setPrefWidth(130);

        btn.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                btn.getParent().requestFocus();
                return;
            }

            KeyEventDTO newDto = fromJavaFx(e, event);

            if (!canBeChosen(event, newDto, hotkeyBuffer, savedHotkeys)) {
                AlertBox.OkWindowError(getFormatted("settings.key.non_valid"));
                e.consume();
                return;
            }

            hotkeyBuffer.put(event, newDto);
            btn.setText(newDto.toString());

            btn.getParent().requestFocus();
            e.consume();
        });

        return btn;
    }



    private static boolean canBeChosen(HotkeyEvent event, KeyEventDTO dto, Map<HotkeyEvent, KeyEventDTO> hotkeyBuffer, Map<HotkeyEvent, KeyEventDTO> savedHotkeys) {
        boolean isSudokuRelated = DefaultKeyBindings.getExclusiveByClass().get(SudokuEvents.class).contains(event.getClass());
        boolean hasNoModifiers = !dto.isControlDown() && !dto.isAltDown() && !dto.isShiftDown() && !dto.isMetaDown();

        if (isSudokuRelated && hasNoModifiers) {
            List<KeyEventDTO> staticSudokuKeys = DefaultKeyBindings.getStaticHotkeys().get(SudokuEvents.class);
            if (staticSudokuKeys != null) {
                for (KeyEventDTO staticDto : staticSudokuKeys) {
                    if (Objects.equals(staticDto.getCodeName(), dto.getCodeName())) {
                        return false;
                    }
                }
            }
        }

        List<KeyEventDTO> active = new ArrayList<>(hotkeyBuffer.values());
        active.addAll(savedHotkeys.values());

        List<Class<? extends HotkeyEvent>> exclusiveClasses = DefaultKeyBindings.getExclusiveByClass().get(event.getClass());
        if (exclusiveClasses == null) return true;

        ArrayList<Class<? extends HotkeyEvent>> classesToRemove = new ArrayList<>(DefaultKeyBindings.getAllClasses());
        classesToRemove.removeAll(exclusiveClasses);

        exclusiveClasses.stream()
                .map(exclusiveClass -> DefaultKeyBindings.getStaticHotkeys().get(exclusiveClass))
                .filter(Objects::nonNull)
                .forEach(active::addAll);

        List<KeyEventDTO> toRemove = new ArrayList<>();
        for (KeyEventDTO eventDTO : active) {
            if (eventDTO.getKeystrokeEvent() == null) continue;
            for (Class<? extends HotkeyEvent> aClass : classesToRemove) {
                if (eventDTO.getKeystrokeEvent().getClass().equals(aClass)) {
                    toRemove.add(eventDTO);
                }
            }
        }
        active.removeAll(toRemove);

        return !active.contains(dto);
    }
}
