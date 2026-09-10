package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SudokuSizeConverter implements AttributeConverter<SudokuSize, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SudokuSize sudokuSize) {
        if (sudokuSize == null) {
            return null;
        }
        return sudokuSize.getId();
    }

    @Override
    public SudokuSize convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return PersistableEnum.fromId(dbData, SudokuSize.class);
    }
}
