package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrioListOnSteroids extends LocalPrioritisedListBot {
    private static final String BOTNAME = "PrioList on Steroids";

    @Override
    public IMove doMove(IGameState state) {
    List<IMove> winMoves = getWinningMoves(state);
        if(!winMoves.isEmpty())
                return winMoves.get(0);

        return super.doMove(state);
    }


    // Simplified version of checking if win. Check the GameManager class to see another similar solution
    private boolean isWinningMove(IGameState state, IMove move, String player){
        // Clones the array and all values to a new array, so we don't mess with the game
        String[][] board = Arrays.stream(state.getField().getBoard()).map(String[]::clone).toArray(String[][]::new);

        //Places the player in the game. Sort of a simulation.
        board[move.getX()][move.getY()] = player;

        int startX = move.getX()-(move.getX()%3);
        if(board[startX][move.getY()]==player)
            if (board[startX][move.getY()] == board[startX+1][move.getY()] &&
                board[startX+1][move.getY()] == board[startX+2][move.getY()])
                    return true;

        int startY = move.getY()-(move.getY()%3);
        if(board[move.getX()][startY]==player)
            if (board[move.getX()][startY] == board[move.getX()][startY+1] &&
                board[move.getX()][startY+1] == board[move.getX()][startY+2])
                    return true;


        if(board[startX][startY]==player)
            if (board[startX][startY] == board[startX+1][startY+1] &&
                    board[startX+1][startY+1] == board[startX+2][startY+2])
                        return true;

        if(board[startX][startY+2]==player)
            if (board[startX][startY+2] == board[startX+1][startY+1] &&
                    board[startX+1][startY+1] == board[startX+2][startY])
                        return true;

        return false;
    }
    // Compile a list of all available winning moves
    private List<IMove> getWinningMoves(IGameState state){
        String player = "1";
        if(state.getMoveNumber()%2==0)
            player="0";

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move:avail) {
            if(isWinningMove(state,move,player))
                winningMoves.add(move);
        }
        return winningMoves;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
