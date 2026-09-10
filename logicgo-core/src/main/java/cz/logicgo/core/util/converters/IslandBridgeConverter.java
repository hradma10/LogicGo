package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.bridge.IslandBridge;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

import static cz.logicgo.core.util.boardConverters.BridgeConverters.deserializeIslandBridges;
import static cz.logicgo.core.util.boardConverters.BridgeConverters.serializeIslandBridges;


@Converter(autoApply = true)
public class IslandBridgeConverter implements AttributeConverter<List<IslandBridge>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<IslandBridge> attribute) {
        return serializeIslandBridges(attribute);
    }

    @Override
    public List<IslandBridge> convertToEntityAttribute(byte[] encodedBoard) {
        return deserializeIslandBridges(encodedBoard);
    }
}
