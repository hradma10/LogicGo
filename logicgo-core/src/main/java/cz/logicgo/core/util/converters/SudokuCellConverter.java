package cz.logicgo.core.util.converters;

import cz.logicgo.core.gameClasses.sudoku.sudokuCells.SudokuCell;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static cz.logicgo.core.util.boardConverters.SudokuConverters.deserializeBoard;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.serializeBoard;

@Converter(autoApply = true)
public class SudokuCellConverter implements AttributeConverter<SudokuCell[][], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(SudokuCell[][] board) {
        return serializeBoard(board);
    }

    @Override
    public SudokuCell[][] convertToEntityAttribute(byte[] encodedBoard) {
        return deserializeBoard(encodedBoard);
    }
}
