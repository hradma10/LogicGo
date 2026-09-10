package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuVariant;
import cz.logicgo.core.misc.interfaces.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SudokuVariantConverter implements AttributeConverter<SudokuVariant, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SudokuVariant sudokuVariant) {
        if (sudokuVariant == null) {
            return null;
        }
        return sudokuVariant.getId();
    }

    @Override
    public SudokuVariant convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return PersistableEnum.fromId(value, SudokuVariant.class);
    }
}
