package dk.easv.gui;


import dk.easv.bll.bot.IBot;
import dk.easv.bll.game.*;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.ArrayList;
import java.util.List;

public class BoardModel implements Observable{
    private static final int TIME_PER_MOVE = 1000; //Each bot is allowed 1000ms per move
    private final List<InvalidationListener> listeners = new ArrayList<>();
    private final GameManager game;
    private boolean isForced=false;
    
    public BoardModel() {
        game = new GameManager(new GameState());
        game.getCurrentState().setTimePerMove(TIME_PER_MOVE);
    }
    public BoardModel(IBot bot, boolean humanPlaysFirst) {
        game = new GameManager(new GameState(), bot, humanPlaysFirst);
        game.getCurrentState().setTimePerMove(TIME_PER_MOVE);
    }
    public BoardModel(IBot bot1, IBot bot2) {
        game = new GameManager(new GameState(), bot1, bot2);
        game.getCurrentState().setTimePerMove(TIME_PER_MOVE);
    }

    private void notifyAllListeners(){
        for (InvalidationListener listener : listeners){
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

    public IGameState getGameState() {
        return game.getCurrentState();
    }

    // If bot is cheating/malfunctioning opponent wins
   public void forceGameOver(int winner){
        isForced=true;
        game.setCurrentPlayer(winner);
        game.setGameOver(GameManager.GameOverState.Win);
   }

    public boolean doMove() {
        boolean valid = game.updateGame();
        if(valid)
            notifyAllListeners();
        return valid;
    }

    public boolean doMove(IMove move){
        boolean valid = game.updateGame(move);
        if(valid)
            notifyAllListeners();
        return valid;
    }

    public String[][] getMacroboard()
    {
        return game.getCurrentState().getField().getMacroboard();
    }

    public String[][] getBoard(){
        return game.getCurrentState().getField().getBoard();
    }

    public int getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public GameManager.GameOverState getGameOverState() {
        return game.getGameOver();
    }

    public boolean getIsForced() {
        return isForced;
    }

}
