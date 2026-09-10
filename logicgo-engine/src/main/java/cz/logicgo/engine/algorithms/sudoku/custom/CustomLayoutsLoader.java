package cz.logicgo.engine.algorithms.sudoku.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout;
import cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuRegionLayout;
import cz.logicgo.core.misc.enums.gameTypes.sudoku.SudokuSize;
import cz.logicgo.engine.algorithms.sudoku.custom.dto.PatternLayouts;
import cz.logicgo.engine.algorithms.sudoku.custom.dto.PatternLayouts.PatternGroup;
import cz.logicgo.engine.algorithms.sudoku.custom.dto.SudokuLayouts;
import cz.logicgo.engine.algorithms.sudoku.custom.variant.gen.OffsetGen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static cz.logicgo.core.gameClasses.sudoku.wrappers.SudokuPatternLayout.createEmptyPattern;
import static cz.logicgo.core.misc.Messages.getFormatted;
import static cz.logicgo.core.util.boardConverters.SudokuConverters.deserializeCustomPattern;


public class CustomLayoutsLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, SudokuPatternLayout> patternLayoutsByName = new ConcurrentHashMap<>();
    private static final Map<Integer, List<SudokuRegionLayout>> layoutsBySize = new ConcurrentHashMap<>();
    private static final Map<Integer, List<SudokuPatternLayout>> patternsBySize = new ConcurrentHashMap<>();
    private static final Map<String, SudokuRegionLayout> layoutsByName = new ConcurrentHashMap<>();
    private static final Map<String, String> layoutsTranslated = new ConcurrentHashMap<>();

    private static final Map<Integer, SudokuRegionLayout> defaultRegions;
    private static final Map<Integer, SudokuPatternLayout> defaultPatterns;

    private static final PatternLayouts patternLayouts;
    private static final SudokuLayouts regionLayouts;

    private static final Map<String, SudokuRegionLayout> layoutCache = new ConcurrentHashMap<>();
    private static final Map<String, SudokuPatternLayout> patternCache = new ConcurrentHashMap<>();

    private static final String sharedDataPath = System.getenv("ProgramData");
    private static final String appFolderName = "LogicGo";
    private static final String appFolderPath = sharedDataPath + File.separator + appFolderName;
    private static final String databaseFile = appFolderPath + File.separator + "sudoku_data.db";

    static {
        try (InputStream is = CustomLayoutsLoader.class.getResourceAsStream("/cz/logicgo/engine/game/sudoku/layouts.json")) {
            if (is == null) throw new IOException("layouts.json not found");
            regionLayouts = loadRegionLayoutsStatic(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = CustomLayoutsLoader.class.getResourceAsStream("/cz/logicgo/engine/game/sudoku/patterns.json")) {
            if (is == null) throw new IOException("patterns.json not found");
            patternLayouts = loadPatternLayouts(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        defaultRegions = defaultLayouts();
        defaultPatterns = defaultEmptyPatterns();
    }

    private static Connection getDbConnection() throws SQLException {
        Path dbFile = new File(databaseFile).toPath();

        if (!Files.exists(dbFile)) {
            try {
                Files.createDirectories(dbFile.getParent());
                try (InputStream is = CustomLayoutsLoader.class.getResourceAsStream("/cz/logicgo/engine/game/sudoku/sudoku_data.db")) {
                    if (is == null) throw new IOException("Database file not found in resources!");
                    Files.copy(is, dbFile, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new SQLException("Could not extract database", e);
            }
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
    }

    private static Map<Integer, SudokuRegionLayout> defaultLayouts() {
        Map<Integer, SudokuRegionLayout> map = new HashMap<>();
        map.put(4, getLayoutsByName("sudoku.layout.standard.4"));
        map.put(5, getLayoutsByName("sudoku.layout.cross.5"));
        map.put(6, getLayoutsByName("sudoku.layout.brick.6"));
        map.put(7, getLayoutsByName("sudoku.layout.standard.7"));
        map.put(8, getLayoutsByName("sudoku.layout.brick.8"));
        map.put(9, getLayoutsByName("sudoku.layout.standard.9"));
        map.put(10, getLayoutsByName("sudoku.layout.brick.10"));
        map.put(11, getLayoutsByName("sudoku.layout.standard.11"));
        map.put(12, getLayoutsByName("sudoku.layout.standard.12"));
        map.put(13, getLayoutsByName("sudoku.layout.standard.13"));
        map.put(14, getLayoutsByName("sudoku.layout.standard.14"));
        map.put(15, getLayoutsByName("sudoku.layout.transposed.15"));
        map.put(16, getLayoutsByName("sudoku.layout.standard.16"));
        return Map.copyOf(map);
    }

    private static Map<Integer, SudokuPatternLayout> defaultEmptyPatterns() {
        Map<Integer, SudokuPatternLayout> map = new HashMap<>();
        for (int size = 4; size <= 16; size++) {
            map.put(size, createEmptyPattern(size));
        }
        return Map.copyOf(map);
    }

    public static SudokuPatternLayout createPatternFromData(int[][] layout, boolean custom) {
        if (layout == null) return null;

        if (patternCache.isEmpty()) {
            patternLayouts.getPatterns().stream()
                    .flatMap(patternGroup -> patternGroup.getPatterns().stream())
                    .forEach(pattern -> {
                        String standardPatternKey = Arrays.deepToString(pattern.getPattern());
                        patternCache.put(standardPatternKey, pattern);
                    });
        }
        String cacheKey = Arrays.deepToString(layout);
        return patternCache.computeIfAbsent(cacheKey, _ ->
                new SudokuPatternLayout("custom", convertToIntegerMatrix(layout), true, custom)
        );
    }

    public static SudokuRegionLayout createLayoutFromData(int[][] layout) {
        if (layout == null) return null;
        String cacheKey = Arrays.deepToString(layout);
        return layoutCache.computeIfAbsent(cacheKey, _ ->
                new SudokuRegionLayout(convertToIntegerMatrix(layout))
        );
    }

    public static Integer[][] convertToIntegerMatrix(int[][] matrix) {
        if (matrix == null) return null;
        return Arrays.stream(matrix)
                .map(row -> Arrays.stream(row)
                        .boxed()
                        .toArray(Integer[]::new))
                .toArray(Integer[][]::new);
    }

    public static SudokuLayouts loadRegionLayoutsStatic(InputStream input) throws IOException {
        SudokuLayouts layouts = mapper.readValue(input, SudokuLayouts.class);
        layoutsBySize.clear();

        layouts.getLayouts().forEach(layout -> layoutsBySize.put(layout.getSize(), layout.getRegionLayouts()));

        layouts.getLayouts().stream()
                .flatMap(layout -> layout.getRegionLayouts().stream())
                .forEach(regionLayout -> {
                    layoutsByName.put(regionLayout.getName(), regionLayout);
                    String translated = getFormatted(regionLayout.getName());
                    regionLayout.setTranslatedName(translated);
                    layoutsTranslated.put(translated, regionLayout.getName());
                });

        return layouts;
    }

    public static PatternLayouts loadPatternLayouts(InputStream input) throws IOException {
        PatternLayouts layouts = mapper.readValue(input, PatternLayouts.class);
        patternsBySize.clear();
        patternLayoutsByName.clear();

        if (layouts.getPatterns() != null) {
            layouts.getPatterns().forEach(group ->
                    patternsBySize.put(group.getSize(), new ArrayList<>(group.getPatterns()))
            );
        }

        IntStream.rangeClosed(4, 16).forEachOrdered(s -> patternsBySize.computeIfAbsent(s, k -> new ArrayList<>())
                .add(OffsetGen.getOffsetPattern(s)));

        patternsBySize.values().forEach(list -> {
            for (SudokuPatternLayout pattern : list) {
                if (pattern == null) continue;
                patternLayoutsByName.put(pattern.getName(), pattern);
                String translated = getFormatted(pattern.getName());
                pattern.setTranslatedName(translated);
                layoutsTranslated.put(translated, pattern.getName());
            }
        });

        List<PatternGroup> updatedGroups = new ArrayList<>();
        patternsBySize.forEach((size, patterns) -> {
            PatternGroup group = new PatternGroup(size);
            group.getPatterns().addAll(patterns);
            updatedGroups.add(group);
        });
        layouts.setPatterns(updatedGroups);

        return layouts;
    }

    public static int getIrregularLayoutCountForSize(int size) {
        String sql = "SELECT COUNT(*) as count FROM irregular_layouts WHERE grid_size = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, size);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException _) {
        }
        return 0;
    }

    public static int[][] getIrregularRegionBySize(int size, int index) {
        String sql = "SELECT layout_data FROM irregular_layouts WHERE grid_size = ? AND layout_index = ?";
        try (Connection conn = getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, size);
            pstmt.setInt(2, index);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] buffer = rs.getBytes("layout_data");
                    return deserializeCustomPattern(buffer);
                }
            }
        } catch (SQLException _) {
        }
        return null;
    }

    public static List<int[][]> getAllIrregularRegionOfSize(int size) {
        String sql = "SELECT layout_data FROM irregular_layouts WHERE grid_size = ? ORDER BY layout_index";
        List<int[][]> list = new ArrayList<>();
        try (Connection conn = getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, size);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    byte[] buffer = rs.getBytes("layout_data");
                    list.add(deserializeCustomPattern(buffer));
                }
            }
            return Collections.unmodifiableList(list);
        } catch (SQLException _) {
        }
        return Collections.emptyList();
    }

    public static int getIrregularRegionId(int size, int[][] layout) {
        String sql = "SELECT layout_index, layout_data FROM irregular_layouts WHERE grid_size = ? ORDER BY layout_index ASC";
        try (Connection conn = getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, size);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    byte[] buffer = rs.getBytes("layout_data");
                    int[][] dbLayout = deserializeCustomPattern(buffer);
                    if (Arrays.deepEquals(dbLayout, layout)) {
                        return rs.getInt("layout_index");
                    }
                }
            }
        } catch (SQLException _) {}
        return -1;
    }

    public static SudokuPatternLayout getDefaultPatternLayoutForSize(int size) {
        try {
            return getPatternsForSize(size).getFirst();
        } catch (NoSuchElementException e) {
            return getEmptyPatternLayout(size);
        }
    }

    public static SudokuPatternLayout getPatternLayoutByNameAndSize(String name, int size) {
        List<SudokuPatternLayout> patterns = getPatternsForSize(size);
        for (SudokuPatternLayout pattern : patterns) {
            if (name.equals(pattern.getName())) {
                return pattern;
            }
        }
        return null;
    }

    public static SudokuRegionLayout getBasicLayout(int size) {
        return defaultRegions.get(SudokuSize.getTypeByGridSize(size).getGridSize());
    }

    public static SudokuRegionLayout getBasicLayout(SudokuSize size) {
        return defaultRegions.get(size.getGridSize());
    }

    public static SudokuPatternLayout getEmptyPatternLayout(int size) {
        return SudokuPatternLayout.createEmptyPattern(size);
    }

    public static SudokuPatternLayout getEmptyPatternLayout(SudokuSize size) {
        return defaultPatterns.get(size.getGridSize());
    }

    public static List<SudokuRegionLayout> getLayoutsForSize(int size) {
        return layoutsBySize.getOrDefault(size, Collections.emptyList());
    }

    public static List<SudokuPatternLayout> getPatternsForSize(int size) {
        return patternsBySize.getOrDefault(size, Collections.emptyList());
    }

    public static List<SudokuRegionLayout> getLayoutsForSize(SudokuSize size) {
        return layoutsBySize.getOrDefault(size.getGridSize(), Collections.emptyList());
    }

    public static SudokuRegionLayout getLayoutsByName(String name) {
        return layoutsByName.get(name);
    }

    public static SudokuRegionLayout getTypeOfRegionSizeBySize(int size, int type) {
        return layoutsBySize.getOrDefault(size, Collections.emptyList()).stream()
                .filter(layout -> type == layout.getType())
                .findFirst().orElse(null);
    }

    public static List<SudokuRegionLayout> getLayoutsBySize(int size) {
        return layoutsBySize.get(size);
    }

    public static Map<String, String> getLayoutsTranslated() {
        return layoutsTranslated;
    }

}
