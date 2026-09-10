package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.sudoku.ParityType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static cz.logicgo.core.util.boardConverters.SudokuConverters.bytesToParities;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.paritiesToBytes;

@Converter
public class ParityConstraintConverter implements AttributeConverter<ParityType[][], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(ParityType[][] attribute) {
        return paritiesToBytes(attribute);
    }

    @Override
    public ParityType[][] convertToEntityAttribute(byte[] dbData) {
        return bytesToParities(dbData);
    }
}
