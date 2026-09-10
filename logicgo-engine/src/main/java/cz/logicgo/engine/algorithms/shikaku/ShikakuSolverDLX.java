package cz.logicgo.engine.algorithms.shikaku;


import cz.logicgo.core.exceptions.parallel.LimitReachedException;
import cz.logicgo.core.gameClasses.shikaku.ShikakuCell;
import cz.logicgo.core.misc.enums.gameTypes.shikaku.ShikakuType;
import cz.logicgo.engine.algorithms.shikaku.solver.Clue;
import cz.logicgo.engine.algorithms.shikaku.solver.Rect;
import cz.logicgo.engine.context.ShikakuSolveContext;

import java.util.*;

public class ShikakuSolverDLX {

    public static int solve(ShikakuCell[][] board, ShikakuType shikakuType, ShikakuSolveContext context) throws LimitReachedException {
        int height = board.length;
        int width = board[0].length;

        List<Clue> clues = findPossiblePlacings(board, shikakuType);

        DLX dlx = new DLX(width, height, clues.size());
        dlx.buildMatrix(clues);

        try {
            dlx.search(context);
        } catch (LimitReachedException e) {

        }

        return context.getSolutionCount();
    }

    public static List<Clue> findPossiblePlacings(ShikakuCell[][] board, ShikakuType shikakuType) {
        int height = board.length;
        int width = board[0].length;
        List<Clue> clues = new ArrayList<>();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (board[r][c].getClue() > 0) {
                    clues.add(new Clue(r, c, board[r][c].getClue()));
                }
            }
        }

        for (Clue clue : clues) {
            findClueCandidates(clue, board, shikakuType);
        }

        Queue<Clue> forcedClues = new LinkedList<>();
        for (Clue clue : clues) {
            if (clue.getCandidates().size() == 1) {
                forcedClues.add(clue);
            }
        }

        while (!forcedClues.isEmpty()) {
            Clue forcedClue = forcedClues.poll();
            Rect forcedRect = forcedClue.getCandidates().getFirst();

            for (Clue otherClue : clues) {
                if (otherClue != forcedClue && otherClue.getCandidates().size() > 1) {
                    boolean removed = otherClue.getCandidates().removeIf(otherRect ->
                            intersects(forcedRect, otherRect)
                    );
                    if (removed && otherClue.getCandidates().size() == 1) {
                        forcedClues.add(otherClue);
                    }
                }
            }
        }

        clues.sort(Comparator.comparingInt(a -> a.getCandidates().size()));
        return clues;
    }

    public static void findClueCandidates(Clue clue, ShikakuCell[][] board, ShikakuType type) {
        int value = clue.getValue();

        if (type == ShikakuType.OFF_BY_ONE) {
            if (value == 1) {
                addCandidatesForArea(clue, 2, board);
            } else {
                addCandidatesForArea(clue, value + 1, board);
                addCandidatesForArea(clue, value - 1, board);
            }
        } else {
            addCandidatesForArea(clue, value, board);
        }
    }

    private static void addCandidatesForArea(Clue clue, int targetArea, ShikakuCell[][] board) {
        int height = board.length;
        int width = board[0].length;
        int row = clue.getRow();
        int col = clue.getCol();

        for (int h = 1; h <= targetArea; h++) {
            if (targetArea % h != 0) continue;
            int w = targetArea / h;

            for (int topRow = row - h + 1; topRow <= row; topRow++) {
                for (int topCol = col - w + 1; topCol <= col; topCol++) {
                    if (notOutOfBounds(topRow, topCol, w, h, width, height)) {
                        if (isValidCandidate(topRow, topCol, topRow + h - 1, topCol + w - 1, clue, board)) {
                            clue.getCandidates().add(new Rect(topRow, topCol, topRow + h - 1, topCol + w - 1));
                        }
                    }
                }
            }
        }
    }

    private static boolean intersects(Rect a, Rect b) {
        return a.r1() <= b.r2() &&
                a.r2() >= b.r1() &&
                a.c1() <= b.c2() &&
                a.c2() >= b.c1();
    }

    private static boolean notOutOfBounds(int row, int col, int w, int h, int width, int height) {
        return row >= 0 && col >= 0 && (row + h - 1) < height && (col + w - 1) < width;
    }

    private static boolean isValidCandidate(int r1, int c1, int r2, int c2, Clue myClue, ShikakuCell[][] board) {
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                int boardClue = board[r][c].getClue();
                if (boardClue > 0 && (r != myClue.getRow() || c != myClue.getCol())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class DLX {
        static class Node {
            Node left, right, up, down;
            ColumnNode column;

            Node() {
                left = right = up = down = this;
            }
        }

        static class ColumnNode extends Node {
            int size;

            ColumnNode() {
                super();
                size = 0;
            }
        }

        private final ColumnNode header;
        private final ColumnNode[] columns;
        private final int width;
        private final int cellColumnsCount;

        public DLX(int width, int height, int cluesCount) {
            this.width = width;
            this.cellColumnsCount = width * height;
            int totalColumns = cellColumnsCount + cluesCount;

            header = new ColumnNode();
            columns = new ColumnNode[totalColumns];

            Node lastAdded = header;
            for (int i = 0; i < totalColumns; i++) {
                columns[i] = new ColumnNode();

                columns[i].left = lastAdded;
                columns[i].right = header;
                lastAdded.right = columns[i];
                header.left = columns[i];
                lastAdded = columns[i];
            }
        }

        public void buildMatrix(List<Clue> clues) {
            for (int clueIdx = 0; clueIdx < clues.size(); clueIdx++) {
                Clue clue = clues.get(clueIdx);
                for (Rect rect : clue.getCandidates()) {
                    Node firstInRow = null;

                    for (int r = rect.r1(); r <= rect.r2(); r++) {
                        for (int c = rect.c1(); c <= rect.c2(); c++) {
                            int colIdx = r * width + c;
                            firstInRow = appendNodeToRow(colIdx, firstInRow);
                        }
                    }

                    int clueColIdx = cellColumnsCount + clueIdx;
                    appendNodeToRow(clueColIdx, firstInRow);
                }
            }
        }

        private Node appendNodeToRow(int colIdx, Node firstInRow) {
            ColumnNode colHeader = columns[colIdx];
            Node node = new Node();
            node.column = colHeader;

            node.down = colHeader;
            node.up = colHeader.up;
            colHeader.up.down = node;
            colHeader.up = node;
            colHeader.size++;

            if (firstInRow == null) {
                firstInRow = node;
                node.left = node;
                node.right = node;
            } else {
                node.right = firstInRow;
                node.left = firstInRow.left;
                firstInRow.left.right = node;
                firstInRow.left = node;
            }
            return firstInRow;
        }

        public void search(ShikakuSolveContext context) throws LimitReachedException {
            if (context.getSolutionCount() > 1) return;

            context.stepCounter().increment();
            if (context.overLimit()) throw new LimitReachedException();

            if (header.right == header) {
                context.incrementSolutionCountAndGet();
                return;
            }

            ColumnNode c = chooseColumn();
            if (c.size == 0) return;

            cover(c);
            for (Node r = c.down; r != c; r = r.down) {
                for (Node j = r.right; j != r; j = j.right) {
                    cover(j.column);
                }

                search(context);

                for (Node j = r.left; j != r; j = j.left) {
                    uncover(j.column);
                }
            }
            uncover(c);
        }

        private ColumnNode chooseColumn() {
            ColumnNode best = null;
            int minSize = Integer.MAX_VALUE;

            for (Node n = header.right; n != header; n = n.right) {
                ColumnNode c = (ColumnNode) n;
                if (c.size < minSize) {
                    minSize = c.size;
                    best = c;
                }
            }
            return best;
        }

        private void cover(ColumnNode c) {
            c.right.left = c.left;
            c.left.right = c.right;
            for (Node i = c.down; i != c; i = i.down) {
                for (Node j = i.right; j != i; j = j.right) {
                    j.down.up = j.up;
                    j.up.down = j.down;
                    j.column.size--;
                }
            }
        }

        private void uncover(ColumnNode c) {
            for (Node i = c.up; i != c; i = i.up) {
                for (Node j = i.left; j != i; j = j.left) {
                    j.column.size++;
                    j.down.up = j;
                    j.up.down = j;
                }
            }
            c.right.left = c;
            c.left.right = c;
        }
    }
}
