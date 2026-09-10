package cz.logicgo.engine.algorithms.sudoku.records;

public record RemovedElement(int rowIndex, int columnIndex, int element, int order) {
    @Override
    public String toString() {
        return "RemovedElement{" +
                "rowIndex=" + rowIndex +
                ", columnIndex=" + columnIndex +
                ", element=" + element +
                ", order=" + order +
                '}';
    }
}
