package cz.logicgo.core.util.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static cz.logicgo.core.util.boardConverters.SudokuConverters.deserializeCustomPattern;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.serializeCustomPattern;

@Converter(autoApply = true)
public class CustomIrregularConverter implements AttributeConverter<int[][], byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(int[][] board) {
        if (board == null || board.length == 0) return null;
        return serializeCustomPattern(board);
    }

    @Override
    public int[][] convertToEntityAttribute(byte[] encodedBoard) {
        if (encodedBoard == null || encodedBoard.length == 0) return new int[0][0];
        return deserializeCustomPattern(encodedBoard);
    }
}
