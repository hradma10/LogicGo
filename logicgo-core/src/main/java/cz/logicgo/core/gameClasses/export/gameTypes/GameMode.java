package cz.logicgo.core.gameClasses.export.gameTypes;


import cz.logicgo.core.misc.enums.Difficulty;
import cz.logicgo.core.misc.enums.TypeGame;

public interface GameMode {
    Difficulty getDifficulty();

    int getCount();

    TypeGame getTypeGame();
}
