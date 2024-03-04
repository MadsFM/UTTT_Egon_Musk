package dk.easv.bll.game;

import dk.easv.bll.field.Field;
import dk.easv.bll.field.IField;

public class GameState implements IGameState{
    IField field;
    int moveNumber;
    int roundNumber;
    int timePerMove = 1000; //1000ms default value, can be changes depending on game specifics.

    public GameState(){
        field = new Field();
        moveNumber=0;
        roundNumber=0;
    }

    public GameState(IGameState state) {
        field = new Field();
        field.setMacroboard(state.getField().getMacroboard());
        field.setBoard(state.getField().getBoard());

        moveNumber = state.getMoveNumber();
        roundNumber = state.getRoundNumber();
    }

    @Override
    public IField getField() {
        return field;
    }

    @Override
    public int getMoveNumber() {
        return moveNumber;
    }

    @Override
    public void setMoveNumber(int moveNumber) {
        this.moveNumber=moveNumber;
    }

    @Override
    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    @Override
    public int getTimePerMove()
    {
        return this.timePerMove;
    }

    @Override
    public void setTimePerMove(int milliSeconds)
    {
        this.timePerMove = milliSeconds;
    }
}
