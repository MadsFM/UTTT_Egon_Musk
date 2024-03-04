package dk.easv.bll.game;

import dk.easv.bll.bot.IBot;
import dk.easv.bll.field.IField;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

/**
 * This is a proposed GameManager for Ultimate Tic-Tac-Toe,
 * the implementation of which is up to whoever uses this interface.
 * Note that initializing a game through the constructors means
 * that you have to create a new instance of the game manager
 * for every new game of a different type (e.g. Human vs Human, Human vs Bot or Bot vs Bot),
 * which may not be ideal for your solution, so you could consider refactoring
 * that into an (re-)initialize method instead.
 * @author mjl
 */
public class GameManager {

    /**
     * Three different game modes.
     */
    public enum GameMode{
        HumanVsHuman,
        HumanVsBot,
        BotVsBot
    }

    public enum GameOverState{
        Active,
        Win,
        Tie
    }
    
    private final IGameState currentState;
    private int currentPlayer = 0; //player0 == 0 && player1 == 1
    private GameMode mode = GameMode.HumanVsHuman;
    private IBot bot = null;
    private IBot bot2 = null;
    private volatile GameOverState gameOver = GameOverState.Active;

    public void setGameOver(GameOverState state) {
        gameOver = state;
    }
    public GameOverState getGameOver() {
        return gameOver;
    }

    public void setCurrentPlayer(int player) {
        currentPlayer = player;
    }
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public IGameState getCurrentState()
    {
        return currentState;
    }
    private boolean playerGoesFirst = false;
    /**
     * Set's the currentState so the game can begin.
     * Game expected to be played Human vs Human
     * @param currentState Current game state, usually an empty board,
     * but could load a saved dk.easv.bll.game.
     */
    public GameManager(IGameState currentState) {
        this.currentState = currentState;
        mode = GameMode.HumanVsHuman;
    }

    /**
     * Set's the currentState so the game can begin.
     * Game expected to be played Human vs Bot
     * @param currentState Current game state, usually an empty board,
     * but could load a saved game.
     * @param bot The bot to play against in vsBot mode.
     */
    public GameManager(IGameState currentState, IBot bot, boolean humanPlaysFirst) {
        this.currentState = currentState;
        playerGoesFirst=humanPlaysFirst;
        mode = GameMode.HumanVsBot;
        this.bot = bot;
    }
    
    /**
     * Set's the currentState so the game can begin.
     * Game expected to be played Bot vs Bot
     * @param currentState Current game state, usually an empty board,
     * but could load a saved game.
     * @param bot The first bot to play.
     * @param bot2 The second bot to play.
     */
    public GameManager(IGameState currentState, IBot bot, IBot bot2) {
        this.currentState = currentState;
        mode = GameMode.BotVsBot;
        this.bot = bot;
        this.bot2 = bot2;
    }
    
    /**
     * User input driven Update
     * @param move The next user dk.easv.bll.move
     * @return Returns true if the update was successful, false otherwise.
     */
    public Boolean updateGame(IMove move)
    {
        if(!verifyMoveLegality(move)) 
            return false;
        
        updateBoard(move);
        currentPlayer = (currentPlayer + 1) % 2;
        
        return true;
    }
    
    /**
     * Non-User driven input, e.g. an update for playing a bot move.
     * @return Returns true if the update was successful, false otherwise.
     */
    public Boolean updateGame()
    {
        //Check game mode is set to one of the bot modes.
        assert(mode != GameMode.HumanVsHuman);
        
        //Check if player is bot, if so, get bot input and update the state based on that.
        if(mode == GameMode.HumanVsBot && currentPlayer == 1 && playerGoesFirst)
        {
             IMove botMove = bot.doMove(new GameState(currentState));
             return updateGame(botMove);
        }
        else if(mode == GameMode.HumanVsBot && !playerGoesFirst && currentPlayer == 0)
        {
            IMove botMove = bot.doMove(new GameState(currentState));
            return updateGame(botMove);
        }
        
        //Check bot is not equal to null, and throw an exception if it is.
        assert(bot != null);
        assert(bot2 != null);

        //Check if player is bot, if so, get bot input and update the state based on that.
        if(mode == GameMode.BotVsBot)
        {
            assert(bot != null);
            assert(bot2 != null);

            IMove botMove = currentPlayer == 0 ? bot.doMove(new GameState(currentState)) : bot2.doMove(new GameState(currentState));

            return updateGame(botMove);
        }
        return false;
    }



    private Boolean verifyMoveLegality(IMove move)
    {
        IField field = currentState.getField();
        boolean isValid=field.isInActiveMicroboard(move.getX(), move.getY());

        if(isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
        if(isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

        if(isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
            isValid=false;

        return isValid;
    }
    
    private void updateBoard(IMove move)
    {
        String[][] board = currentState.getField().getBoard();
        board[move.getX()][move.getY()]=currentPlayer+"";
        currentState.setMoveNumber(currentState.getMoveNumber() + 1);
        if(currentState.getMoveNumber() % 2 == 0) { currentState.setRoundNumber(currentState.getRoundNumber() + 1); }
        checkAndUpdateIfWin(move);
        updateMacroboard(move);

    }

    private void checkAndUpdateIfWin(IMove move) {
        String[][] macroBoard = currentState.getField().getMacroboard();
        int macroX = move.getX()/3;
        int macroY = move.getY()/3;

        if(macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD) ) {

            String[][] board = getCurrentState().getField().getBoard();

            if(isWin(board,move, ""+currentPlayer))
                macroBoard[macroX][macroY] = currentPlayer + "";
            else if(isTie(board,move))
                macroBoard[macroX][macroY] = "TIE";
            
            //Check macro win
            if(isWin(macroBoard,new Move(macroX,macroY), ""+currentPlayer))
                gameOver = GameOverState.Win;
            else if(isTie(macroBoard,new Move(macroX,macroY)))
                gameOver = GameOverState.Tie;
        }

    }

    private boolean isTie(String[][] board, IMove move){
        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        for (int i = startX; i < startX+3; i++) {
            for (int k = startY; k < startY+3; k++) {
                if(board[i][k].equals(IField.AVAILABLE_FIELD) ||
                        board[i][k].equals(IField.EMPTY_FIELD) )
                    return false;
            }
        }
        return true;
    }


    public static boolean isWin(String[][] board, IMove move, String currentPlayer){
        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        //check col
        for (int i = startY; i < startY + 3; i++) {
            if (!board[move.getX()][i].equals(currentPlayer))
                break;
            if (i == startY + 3 - 1) return true;
        }

        //check row
        for (int i = startX; i < startX + 3; i++) {
            if (!board[i][move.getY()].equals(currentPlayer))
                break;
            if (i == startX + 3 - 1) return true;
        }

        //check diagonal
        if (localX == localY) {
            //we're on a diagonal
            int y = startY;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][y++].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }

        //check anti diagonal
        if (localX + localY == 3 - 1) {
            int less = 0;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][(startY + 2)-less++].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }
        return false;
    }
    
    private void updateMacroboard(IMove move)
    {
        String[][] macroBoard = currentState.getField().getMacroboard();
        for (int i = 0; i < macroBoard.length; i++)
            for (int k = 0; k < macroBoard[i].length; k++) {
                if(macroBoard[i][k].equals(IField.AVAILABLE_FIELD))
                    macroBoard[i][k] = IField.EMPTY_FIELD;
            }

        int xTrans = move.getX()%3;
        int yTrans = move.getY()%3;

        if(macroBoard[xTrans][yTrans].equals(IField.EMPTY_FIELD))
            macroBoard[xTrans][yTrans] = IField.AVAILABLE_FIELD;
        else {
            // Field is already won, set all fields not won to avail.
            for (int i = 0; i < macroBoard.length; i++)
                for (int k = 0; k < macroBoard[i].length; k++) {
                    if(macroBoard[i][k].equals(IField.EMPTY_FIELD))
                        macroBoard[i][k] = IField.AVAILABLE_FIELD;
                }
        }
    }
}
