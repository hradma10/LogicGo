package cz.logicgo.core.util.converters;

import cz.logicgo.core.misc.HistorySudokuPlay;
import jakarta.persistence.AttributeConverter;

import java.util.List;

import static cz.logicgo.core.util.boardConverters.SudokuConverters.deserializeSudokuPlayHistory;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.serializeSudokuPlayHistory;


public class HistorySudokuPlayConverter implements AttributeConverter<List<HistorySudokuPlay>, byte[]> {
    @Override
    public byte[] convertToDatabaseColumn(List<HistorySudokuPlay> attribute) {
        return serializeSudokuPlayHistory(attribute);
    }

    @Override
    public List<HistorySudokuPlay> convertToEntityAttribute(byte[] dbData) {
        return deserializeSudokuPlayHistory(dbData);
    }
}
