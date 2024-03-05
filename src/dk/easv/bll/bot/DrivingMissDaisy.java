package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DrivingMissDaisy implements IBot {
    private static final int MOVE_TIME_MS = 1000;
    private static final String BOT_NAME = "DrivingMissDaisy";
    private int centerCounter = 0; // Counter to keep track of how many times center was played

    @Override
    public IMove doMove(IGameState state) {
        return calculateWinningMove(state, MOVE_TIME_MS);
    }

    private IMove calculateWinningMove(IGameState state, int maxTimeMs) {
        long startTime = System.currentTimeMillis();
        Random rand = new Random();

        // Check if it's the first move, play the center of the center
        if (isFirstMove(state)) {
            return new Move(4, 4);
        }

        // Check if there is a winning move in any microboard
        List<IMove> moves = state.getField().getAvailableMoves();
        for (IMove move : moves) {
            if (isMicroboardCenter(move)) {
                return move;
            }
        }

        // Check if there is a possible winning move in any microboard
        for (IMove move : moves) {
            if (isMicroboardWin(state, move)) {
                return move;
            }
        }

        // If no winning move in any microboard, proceed with the previous logic
        // ...

        // If no clear win found and center position of any microboard is not available, return random valid move
        while (System.currentTimeMillis() < startTime + maxTimeMs) {
            GameSimulator simulator = new GameSimulator(state);
            IMove move = playGame(simulator, rand);
            if (simulator.getGameOver() == GameOverState.Win) {
                return move;
            }
        }

        // If no clear win found, return random valid move
        return getRandomMove(new GameSimulator(state), rand);
    }

    private boolean isFirstMove(IGameState state) {
        return state.getField().getAvailableMoves().size() == 81;
    }

    private boolean isMicroboardCenter(IMove move) {
        int microX = move.getX() % 3;
        int microY = move.getY() % 3;
        return microX == 1 && microY == 1;
    }

    private boolean isMicroboardWin(IGameState state, IMove move) {
        int x = move.getX();
        int y = move.getY();
        String[][] board = state.getField().getBoard();
        int currentPlayer = state.getMoveNumber() % 2;
        String playerSymbol = String.valueOf(currentPlayer);

        // Check horizontal, vertical, and diagonal lines in the microboard
        return (board[x][0].equals(playerSymbol) && board[x][1].equals(playerSymbol) && board[x][2].equals(playerSymbol)) ||
                (board[0][y].equals(playerSymbol) && board[1][y].equals(playerSymbol) && board[2][y].equals(playerSymbol)) ||
                (board[0][0].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][2].equals(playerSymbol)) ||
                (board[0][2].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][0].equals(playerSymbol));
    }

    private IMove playGame(GameSimulator simulator, Random rand) {
        while (simulator.getGameOver() == GameOverState.Active) {
            IMove move = getRandomMove(simulator, rand);
            if (move != null) {
                simulator.updateGame(move);
                // Update centerCounter if X played in the center
                if (isCenterMove(simulator, move)) {
                    centerCounter++;
                }
            } else {
                break; // Exit the loop if no move is available
            }
        }
        return simulator.getLastMove();
    }

    private boolean isCenterMove(GameSimulator simulator, IMove move) {
        return simulator.getCurrentState().getMoveNumber() % 2 == 0 && move.getX() == 4 && move.getY() == 4;
    }

    private IMove getRandomMove(GameSimulator simulator, Random rand) {
        List<IMove> moves = simulator.getCurrentState().getField().getAvailableMoves();
        if (moves.isEmpty()) {
            return null; // Handle the case where no moves are available
        }
        return moves.get(rand.nextInt(moves.size()));
    }

    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    public static class Move implements IMove {
        private final int x;
        private final int y;

        public Move(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return x == move.x && y == move.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static class GameSimulator {
        private final IGameState currentState;
        private IMove lastMove;
        private GameOverState gameOver;

        public GameSimulator(IGameState currentState) {
            this.currentState = new GameState(currentState);
            this.gameOver = GameOverState.Active;
        }

        public GameOverState getGameOver() {
            return gameOver;
        }

        public IGameState getCurrentState() {
            return currentState;
        }

        public IMove getLastMove() {
            return lastMove;
        }

        public void updateGame(IMove move) {
            if (!verifyMoveLegality(move))
                return;

            updateBoard(move);
        }

        private boolean verifyMoveLegality(IMove move) {
            IField field = currentState.getField();
            return field.isInActiveMicroboard(move.getX(), move.getY())
                    && move.getX() >= 0 && move.getX() < 9
                    && move.getY() >= 0 && move.getY() < 9
                    && field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD);
        }

        private void updateBoard(IMove move) {
            String[][] board = currentState.getField().getBoard();
            int currentPlayer = currentState.getMoveNumber() % 2;
            board[move.getX()][move.getY()] = String.valueOf(currentPlayer);
            currentState.setMoveNumber(currentState.getMoveNumber() + 1);
            if (currentState.getMoveNumber() % 2 == 0) {
                currentState.setRoundNumber(currentState.getRoundNumber() + 1);
            }
            lastMove = move;
            checkAndUpdateIfWin(move);
        }

        private void checkAndUpdateIfWin(IMove move) {
            if (isMicroboardWin(move.getX(), move.getY())) {
                gameOver = GameOverState.Win;
                return;
            }

            if (checkMainBoardWin()) {
                gameOver = GameOverState.Win;
                return;
            }

            // Check for tie if no win
            if (currentState.getMoveNumber() == 81) {
                gameOver = GameOverState.Tie;
            }
        }

        private boolean isMicroboardWin(int x, int y) {
            String[][] board = currentState.getField().getBoard();
            int currentPlayer = currentState.getMoveNumber() % 2;
            String playerSymbol = String.valueOf(currentPlayer);

            // Check horizontal, vertical, and diagonal lines in the microboard
            return (board[x][0].equals(playerSymbol) && board[x][1].equals(playerSymbol) && board[x][2].equals(playerSymbol)) ||
                    (board[0][y].equals(playerSymbol) && board[1][y].equals(playerSymbol) && board[2][y].equals(playerSymbol)) ||
                    (board[0][0].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][2].equals(playerSymbol)) ||
                    (board[0][2].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][0].equals(playerSymbol));
        }

        private boolean checkMainBoardWin() {
            String[][] board = currentState.getField().getBoard();
            int currentPlayer = currentState.getMoveNumber() % 2;
            String playerSymbol = String.valueOf(currentPlayer);

            // Check horizontal, vertical, and diagonal lines in the main board
            for (int i = 0; i < 3; i++) {
                if ((board[i][0].equals(playerSymbol) && board[i][1].equals(playerSymbol) && board[i][2].equals(playerSymbol)) ||
                        (board[0][i].equals(playerSymbol) && board[1][i].equals(playerSymbol) && board[2][i].equals(playerSymbol))) {
                    return true;
                }
            }

            // Check diagonals
            return (board[0][0].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][2].equals(playerSymbol)) ||
                    (board[0][2].equals(playerSymbol) && board[1][1].equals(playerSymbol) && board[2][0].equals(playerSymbol));
        }
    }

    public enum GameOverState {
        Active,
        Win,
        Tie
    }
}
