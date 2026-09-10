package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.util.boardConverters.SudokuConverters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SudokuPatternConverter implements AttributeConverter<SudokuPatternLayout, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(SudokuPatternLayout attribute) {
        return SudokuConverters.sudokuPatternToBytes(attribute);
    }

    @Override
    public SudokuPatternLayout convertToEntityAttribute(byte[] dbData) {
        return SudokuConverters.bytesToSudokuPattern(dbData);
    }
}
