module cz.logicgo.engine {
    requires cz.logicgo.core;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires com.github.librepdf.openpdf;
    requires java.desktop;

    exports cz.logicgo.engine.algorithms.bridges;
    exports cz.logicgo.engine.algorithms.mazes;
    exports cz.logicgo.engine.algorithms.shikaku;
    exports cz.logicgo.engine.algorithms.sudoku;
    exports cz.logicgo.engine.algorithms.sudoku.solvers;
    exports cz.logicgo.engine.algorithms.sudoku.validators;
    exports cz.logicgo.engine.util.generate;
    exports cz.logicgo.engine.algorithms.genStatistics;
    exports cz.logicgo.engine.export.ruleGen;
    exports cz.logicgo.engine.algorithms.sudoku.grader;
    exports cz.logicgo.engine.algorithms.sudoku.custom;
    exports cz.logicgo.engine.algorithms.settings;
    exports cz.logicgo.engine.algorithms.drawing;
    exports cz.logicgo.engine.algorithms.sudoku.custom.variant.gen;
    exports cz.logicgo.engine.algorithms.sudoku.helper;

    exports cz.logicgo.engine.algorithms.sudoku.custom.dto to com.fasterxml.jackson.databind;

}
