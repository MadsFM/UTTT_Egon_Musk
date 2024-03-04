package dk.easv.bll.game;

import dk.easv.bll.field.IField;

/**
 *
 * @author mjl
 */
public interface IGameState {

    IField getField();

    int getMoveNumber();
    void setMoveNumber(int moveNumber);

    int getRoundNumber();
    void setRoundNumber(int roundNumber);

    int getTimePerMove();
    void setTimePerMove(int milliSeconds);
}
