package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.bridge.Island;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

import static cz.logicgo.core.util.boardConverters.BridgeConverters.deserializeIslands;
import static cz.logicgo.core.util.boardConverters.BridgeConverters.serializeIslands;


@Converter(autoApply = true)
public class IslandConverter implements AttributeConverter<List<Island>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<Island> attribute) {
        return serializeIslands(attribute);
    }

    @Override
    public List<Island> convertToEntityAttribute(byte[] encodedBoard) {
        return deserializeIslands(encodedBoard);
    }
}
