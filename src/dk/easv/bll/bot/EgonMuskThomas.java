package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;

import static com.sun.org.apache.xml.internal.security.utils.XMLUtils.selectNode;

public class EgonMuskThomas implements IBot {
    final int moveTimeMs = 1000;
    private String BOT_NAME = getClass().getSimpleName();
    IGameState currentState = new GameState(); // Assuming GameState implements IGameState
    GameManager gameManager = new GameManager(currentState);

    @Override
    public IMove doMove(IGameState state) {
        return calculateWinningMove(state, moveTimeMs);
    }

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard().clone());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard().clone());
        return simulator;
    }
    // This is an added utility class to help with move evaluation
    class MoveEvaluation {
        IMove move;
        int wins;
        int simulations;

        public MoveEvaluation(IMove move) {
            this.move = move;
            this.wins = 0;
            this.simulations = 0;
        }

        // Simulated UCB1-like score for move selection
        public double getScore(int totalSimulations) {
            if (this.simulations == 0) return Double.MAX_VALUE; // Maximize exploration
            return ((double) this.wins / this.simulations) +
                    Math.sqrt(2 * Math.log(totalSimulations) / this.simulations);
        }
    }
    class TreeNode {
        IMove move;
        IGameState gameState;
        TreeNode parent;
        List<TreeNode> children = new ArrayList<>();
        int wins = 0;
        int visits = 0;

        public TreeNode(IGameState gameState, TreeNode parent, IMove move) {
            this.gameState = gameState;
            this.parent = parent;
            this.move = move;
        }

        // Adds a child node to this node
        public void addChild(TreeNode child) {
            children.add(child);
        }

        // Selects a child node based on UCB1
        public TreeNode selectChild() {
            TreeNode selected = null;
            double bestValue = Double.MIN_VALUE;
            for (TreeNode child : children) {
                double ucb1Value = child.wins / (double) child.visits +
                        Math.sqrt(2 * Math.log(this.visits) / child.visits);
                if (ucb1Value > bestValue) {
                    selected = child;
                    bestValue = ucb1Value;
                }
            }
            return selected;
        }
    }


    // Modification to the calculateWinningMove method
    private IMove calculateWinningMove(IGameState state, int maxTimeMs) {
        long endTime = System.currentTimeMillis() + maxTimeMs;
        TreeNode root = new TreeNode(state.clone(), null, null); // Clone the current state as the root

        while (System.currentTimeMillis() < endTime) {
            TreeNode selected = selectNode(root); // Selection phase
            if (selected.gameState.getGameOver() == GameOverState.Active) {
                expandNode(selected); // Expansion phase
            }
            TreeNode nodeToSimulate = selected;
            if (!selected.children.isEmpty()) {
                nodeToSimulate = selected.selectChild(); // Choose a child to simulate
            }
            GameOverState result = simulateGame(nodeToSimulate.gameState); // Simulation phase
            backpropagate(nodeToSimulate, result); // Backpropagation phase
        }

        // Choose the best move at the end of MCTS
        TreeNode bestChild = Collections.max(root.children, Comparator.comparing(c -> c.wins / (double) c.visits));
        return bestChild.move;
    }




    private IMove selectBestMove(List<IMove> availableMoves, int[] wins, int[] blocks, int[] simulations) {
        double bestScore = -1;
        IMove bestMove = availableMoves.get(0); // Default to the first move

        for (int i = 0; i < availableMoves.size(); i++) {
            double winRatio = (double) wins[i] / simulations[i];
            double blockRatio = (double) blocks[i] / simulations[i];
            double score = winRatio + blockRatio; // Combine winning and blocking in the score

            if (score > bestScore) {
                bestScore = score;
                bestMove = availableMoves.get(i);
            } else if (score == bestScore && blockRatio > 0) {
                bestMove = availableMoves.get(i);
            }
        }

        return bestMove;
    }

    private boolean simulateGame(GameSimulator simulator, IMove move, Random rand, int currentPlayer) {
        // Apply the move to the simulator and continue the game until a game over state is reached
        simulator.updateGame(move);
        while (simulator.getGameOver() == GameOverState.Active) {
            // Simulate moves for both players until the game ends
            List<IMove> moves = simulator.getCurrentState().getField().getAvailableMoves();
            if (moves.isEmpty()) break;
            IMove randomMove = moves.get(rand.nextInt(moves.size()));
            simulator.updateGame(randomMove);
        }

        // Check if the current player won the game
        return simulator.getGameOver() == GameOverState.Win && simulator.getCurrentPlayer() == currentPlayer;
    }



    /*
        The code below is a simulator for simulation of gameplay. This is needed for AI.

        It is put here to make the Bot independent of the GameManager and its subclasses/enums

        Now this class is only dependent on a few interfaces: IMove, IField, and IGameState

        You could say it is self-contained. The drawback is that if the game rules change, the simulator must be
        changed accordingly, making the code redundant.

     */

    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    public enum GameOverState {
        Active,
        Win,
        Tie
    }

    public class Move implements IMove {
        int x = 0;
        int y = 0;

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

        class GameSimulator {
        private final IGameState currentState;
        private int currentPlayer = 0; //player0 == 0 && player1 == 1
        private volatile GameOverState gameOver = GameOverState.Active;

            private int getCurrentPlayer() {
                return currentPlayer;
            }

            public void setGameOver(GameOverState state) {
            gameOver = state;
        }

        public GameOverState getGameOver() {
            return gameOver;
        }

        public void setCurrentPlayer(int player) {
            currentPlayer = player;
        }

        public IGameState getCurrentState() {
            return currentState;
        }

        public GameSimulator(IGameState currentState) {
            this.currentState = currentState;
        }

        public Boolean updateGame(IMove move) {
            if (!verifyMoveLegality(move))
                return false;

            updateBoard(move);
            currentPlayer = (currentPlayer + 1) % 2;

            return true;
        }

        private Boolean verifyMoveLegality(IMove move) {
            IField field = currentState.getField();
            boolean isValid = field.isInActiveMicroboard(move.getX(), move.getY());

            if (isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
            if (isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

            if (isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
                isValid = false;

            return isValid;
        }

        private void updateBoard(IMove move) {
            String[][] board = currentState.getField().getBoard();
            board[move.getX()][move.getY()] = currentPlayer + "";
            currentState.setMoveNumber(currentState.getMoveNumber() + 1);
            if (currentState.getMoveNumber() % 2 == 0) {
                currentState.setRoundNumber(currentState.getRoundNumber() + 1);
            }
            checkAndUpdateIfWin(move);
            updateMacroboard(move);

        }

        private void checkAndUpdateIfWin(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            int macroX = move.getX() / 3;
            int macroY = move.getY() / 3;

            if (macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                    macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD)) {

                String[][] board = getCurrentState().getField().getBoard();

                if (isWin(board, move, "" + currentPlayer))
                    macroBoard[macroX][macroY] = currentPlayer + "";
                else if (isTie(board, move))
                    macroBoard[macroX][macroY] = "TIE";

                //Check macro win
                if (isWin(macroBoard, new Move(macroX, macroY), "" + currentPlayer))
                    gameOver = GameOverState.Win;
                else if (isTie(macroBoard, new Move(macroX, macroY)))
                    gameOver = GameOverState.Tie;
            }

        }

        private boolean isTie(String[][] board, IMove move) {
            int localX = move.getX() % 3;
            int localY = move.getY() % 3;
            int startX = move.getX() - (localX);
            int startY = move.getY() - (localY);

            for (int i = startX; i < startX + 3; i++) {
                for (int k = startY; k < startY + 3; k++) {
                    if (board[i][k].equals(IField.AVAILABLE_FIELD) ||
                            board[i][k].equals(IField.EMPTY_FIELD))
                        return false;
                }
            }
            return true;
        }


        public boolean isWin(String[][] board, IMove move, String currentPlayer) {
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
                    if (!board[i][(startY + 2) - less++].equals(currentPlayer))
                        break;
                    if (i == startX + 3 - 1) return true;
                }
            }
            return false;
        }

        private void updateMacroboard(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            for (int i = 0; i < macroBoard.length; i++)
                for (int k = 0; k < macroBoard[i].length; k++) {
                    if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD))
                        macroBoard[i][k] = IField.EMPTY_FIELD;
                }

            int xTrans = move.getX() % 3;
            int yTrans = move.getY() % 3;

            if (macroBoard[xTrans][yTrans].equals(IField.EMPTY_FIELD))
                macroBoard[xTrans][yTrans] = IField.AVAILABLE_FIELD;
            else {
                // Field is already won, set all fields not won to avail.
                for (int i = 0; i < macroBoard.length; i++)
                    for (int k = 0; k < macroBoard[i].length; k++) {
                        if (macroBoard[i][k].equals(IField.EMPTY_FIELD))
                            macroBoard[i][k] = IField.AVAILABLE_FIELD;
                    }
            }
        }
    }

}
