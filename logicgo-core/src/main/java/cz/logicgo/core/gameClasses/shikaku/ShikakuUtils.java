package cz.logicgo.core.gameClasses.shikaku;


import cz.logicgo.core.entity.games.shikaku.Shikaku;

import java.util.*;

public class ShikakuUtils {

    public void applyRegionId(ShikakuCell[][] board, ShikakuRectangle rectangle, byte regionId) {
        int minRow = rectangle.getMinRow();
        int maxRow = rectangle.getMaxRow();
        int minCol = rectangle.getMinCol();
        int maxCol = rectangle.getMaxCol();
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                if (board[r][c] != null) {
                    board[r][c].setRegionId(regionId);
                }
            }
        }
    }

    public static ShikakuCell calculateValidEndCell(ShikakuCell start, ShikakuCell target) {
        if (start.getRow() == target.getRow() && start.getCol() == target.getCol()) {
            return target;
        }

        return target;
    }

    private static class RegionBounds {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int maxCol = Integer.MIN_VALUE;

        void update(int r, int c) {
            if (r < minRow) minRow = r;
            if (c < minCol) minCol = c;
            if (r > maxRow) maxRow = r;
            if (c > maxCol) maxCol = c;
        }
    }

    public static List<ShikakuRectangle> getSolutionRectangles(ShikakuCell[][] board, int width, int height) {
        Map<Integer, RegionBounds> boundsMap = new HashMap<>();

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                ShikakuCell cell = board[r][c];
                if (cell == null) continue;
                int regionId = cell.getRegionId();

                if (regionId <= 0) continue;

                boundsMap.putIfAbsent(regionId, new RegionBounds());
                boundsMap.get(regionId).update(r, c);
            }
        }

        List<ShikakuRectangle> solution = new ArrayList<>();

        for (Map.Entry<Integer, RegionBounds> entry : boundsMap.entrySet()) {
            int regionId = entry.getKey();
            RegionBounds bounds = entry.getValue();

            ShikakuCell startCell = board[bounds.minRow][bounds.minCol];
            ShikakuCell endCell = board[bounds.maxRow][bounds.maxCol];

            solution.add(new ShikakuRectangle(regionId, startCell, endCell));

        }

        return solution;
    }

    public static ShikakuCell[] flattenBoard(ShikakuCell[][] board) {
        return Arrays.stream(board)
                .flatMap(Arrays::stream)
                .toArray(ShikakuCell[]::new);
    }

    public static boolean isGameFinished(Shikaku shikaku) {
        if (shikaku.getRectangles().size() != shikaku.getSolutionRectangles().size()) return false;

        Set<ShikakuRectangle> rectangles = new HashSet<>(shikaku.getRectangles());
        shikaku.getSolutionRectangles().forEach(rectangles::remove);

        return rectangles.isEmpty();
    }
}
