package cz.logicgo.core.util.converters.settings;

import cz.logicgo.core.gameClasses.keys.KeyEventDTO;
import cz.logicgo.core.misc.enums.keys.HotkeyEvent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cz.logicgo.core.misc.Compression.compress;
import static cz.logicgo.core.misc.Compression.decompress;
import static cz.logicgo.core.misc.enums.keys.EventRegistry.getAllEvents;

@Converter
public class KeyEventConverter implements AttributeConverter<HashMap<HotkeyEvent, KeyEventDTO>, byte[]> {

    private static final Map<String, HotkeyEvent> CACHE = new ConcurrentHashMap<>();

    static {
        registerEvents(getAllEvents().toArray(new HotkeyEvent[0]));
    }

    private static void registerEvents(HotkeyEvent[] events) {
        if (events != null) {
            for (HotkeyEvent event : events) {
                CACHE.put(event.getQualifiedName(), event);
            }
        }
    }

    @Override
    public byte[] convertToDatabaseColumn(HashMap<HotkeyEvent, KeyEventDTO> eventDTOS) {
        if (eventDTOS == null || eventDTOS.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            for (KeyEventDTO event : eventDTOS.values()) {
                if (event == null || event.getKeystrokeEvent() == null) {
                    continue;
                }

                dos.writeUTF(event.getCodeName() != null ? event.getCodeName() : "");
                dos.writeUTF(event.getCharacter() != null ? event.getCharacter() : "");
                dos.writeUTF(event.getText() != null ? event.getText() : "");
                dos.writeUTF(event.getKeystrokeEvent().getQualifiedName());

                dos.writeBoolean(event.isShiftDown());
                dos.writeBoolean(event.isControlDown());
                dos.writeBoolean(event.isAltDown());
                dos.writeBoolean(event.isMetaDown());
            }

            return compress(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Chyba při serializaci klávesových zkratek", e);
        }
    }

    @Override
    public HashMap<HotkeyEvent, KeyEventDTO> convertToEntityAttribute(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new HashMap<>();
        }

        byte[] decompressed = decompress(bytes);
        var keyEventDTOs = new HashMap<HotkeyEvent, KeyEventDTO>();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
             DataInputStream dis = new DataInputStream(bais)) {

            while (dis.available() > 0) {
                String codeName = dis.readUTF();
                String character = dis.readUTF();
                String text = dis.readUTF();
                String keyStrokeEventString = dis.readUTF();

                boolean shiftDown = dis.readBoolean();
                boolean controlDown = dis.readBoolean();
                boolean altDown = dis.readBoolean();
                boolean metaDown = dis.readBoolean();

                HotkeyEvent hotkeyEvent = getKeyStrokeEvent(keyStrokeEventString);

                KeyEventDTO event = new KeyEventDTO(
                        codeName,
                        character.isEmpty() ? null : character,
                        text.isEmpty() ? null : text,
                        shiftDown,
                        controlDown,
                        altDown,
                        metaDown,
                        hotkeyEvent
                );

                if (hotkeyEvent != null) {
                    keyEventDTOs.put(hotkeyEvent, event);
                }
            }

            return keyEventDTOs;
        } catch (IOException e) {
            throw new RuntimeException("Chyba při deserializaci klávesových zkratek", e);
        }
    }

    public static HotkeyEvent getKeyStrokeEvent(String keyStrokeString) {
        if (keyStrokeString == null || keyStrokeString.isEmpty()) {
            return null;
        }
        return CACHE.get(keyStrokeString);
    }
}
