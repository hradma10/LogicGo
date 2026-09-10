package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.util.boardConverters.SudokuConverters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SudokuRegionConverter implements AttributeConverter<SudokuRegionLayout, byte[]> {


    @Override
    public byte[] convertToDatabaseColumn(SudokuRegionLayout attribute) {
        return SudokuConverters.sudokuRegionToBytes(attribute);
    }

    @Override
    public SudokuRegionLayout convertToEntityAttribute(byte[] dbData) {
        return SudokuConverters.bytesToSudokuRegion(dbData);
    }
}
