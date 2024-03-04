package dk.easv.bll.field;

import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;

public class Field implements IField{

    volatile String[][] board = new String[9][9];
    volatile String[][] macroBoard = new String[3][3];

    public Field() {
        clearBoard();
    }

    @Override
    public void clearBoard() {
        board = new String[9][9];
        for (int i = 0; i < board.length; i++)
            for (int k = 0; k < board[i].length; k++) {
                board[i][k] = EMPTY_FIELD;
            }
        for (int i = 0; i < macroBoard.length; i++)
            for (int k = 0; k < macroBoard[i].length; k++) {
                macroBoard[i][k] = AVAILABLE_FIELD;
            }
    }

    @Override
    public List<IMove> getAvailableMoves() {
        List<IMove> availMoves = new ArrayList<>();

        for (int i = 0; i < board.length; i++)
            for (int k = 0; k < board[i].length; k++) {
                if(isInActiveMicroboard(i,k) && board[i][k].equals(EMPTY_FIELD)) {
                    availMoves.add(new Move(i,k));
                }
        }

        return availMoves;
    }

    @Override
    public String getPlayerId(int column, int row) {
        return board[column][row];
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < board.length; i++)
            for (int k = 0; k < board[i].length; k++) {
                if(board[i][k]!=EMPTY_FIELD && board[i][k]!=AVAILABLE_FIELD)
                    return false;
            }
        return true;
    }

    @Override
    public boolean isFull() {
        for (int i = 0; i < board.length; i++)
            for (int k = 0; k < board[i].length; k++) {
                if(board[i][k]==EMPTY_FIELD || board[i][k]==AVAILABLE_FIELD)
                    return false;
            }
        return true;
    }

    @Override
    public Boolean isInActiveMicroboard(int x, int y) {
        int xTrans = x>0 ? x/3 : 0;
        int yTrans = y>0 ? y/3 : 0;
        String value = macroBoard[xTrans][yTrans];
        return value.equals(AVAILABLE_FIELD);
    }

    @Override
    public String[][] getBoard() {
        return board;
    }

    @Override
    public String[][] getMacroboard() {
        return macroBoard;
    }

    @Override
    public void setBoard(String[][] board)
    {
        //NOTE: Cloning here, for simulation purposes
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.board[i][j] = board[i][j];
            }
        }
    }

    @Override
    public void setMacroboard(String[][] macroboard)
    {
        //NOTE: Cloning here, for simulation purposes
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.macroBoard[i][j] = macroboard[i][j];
            }
        }
    }
}
