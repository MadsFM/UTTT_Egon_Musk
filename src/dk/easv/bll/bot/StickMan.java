package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class StickMan implements IBot {
    private static final Random random = new Random();
    private static final String BOT_NAME = "StickMan";
    private static final int MAX_ITERATIONS = 2000;
    private static final double EXPLORATION_FACTOR = 1.41;

    private class TreeNode {
        IGameState state;
        List<TreeNode> children = new ArrayList<>();
        TreeNode parent;
        IMove move;
        int wins = 0;
        int visits = 0;

        TreeNode(IGameState state, TreeNode parent, IMove move) {
            this.state = state;
            this.parent = parent;
            this.move = move;
        }

        private IGameState performMove(IMove move) {
            IGameState nextState = state; // Assuming a shallow copy of the game state
            // Apply the move to the game state
            // Example: nextState = applyMove(state, move);
            return nextState;
        }
    }

    public IMove doMove(IGameState state) {
        TreeNode rootNode = new TreeNode(state, null, null);
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            TreeNode promisingNode = selectPromisingNode(rootNode)
                    .orElseThrow(() -> new IllegalStateException("No promising node found"));
            if (!gameOver(promisingNode.state)) {
                expandNode(promisingNode);
                System.out.println("Expanded node with move: " + promisingNode.move);
                System.out.println("Number of children: " + promisingNode.children.size());
            }
            TreeNode nodeToExplore = promisingNode.children.isEmpty() ? promisingNode :
                    promisingNode.children.get(random.nextInt(promisingNode.children.size()));
            int simulationResult = simulateRandomPlayout(nodeToExplore.state);
            backPropagation(nodeToExplore, simulationResult);
            System.out.println("Iteration " + (i + 1) + " completed.");
        }
        System.out.println("All iterations completed.");

        // Check if the rootNode has children before deciding the next move
        if (!rootNode.children.isEmpty()) {
            // rootNode has children, so we select the best move among them
            System.out.println("---------" + selectBestMove(rootNode));
            return selectBestMove(rootNode);
        } else {
            // rootNode has no children, fallback to a random move from the available moves in the current state
            List<IMove> availableMoves = state.getField().getAvailableMoves();
            if (!availableMoves.isEmpty()) {
                IMove randomMove = availableMoves.get(random.nextInt(availableMoves.size()));
                System.out.println("Fallback to random move: " + randomMove);
                return randomMove;
            } else {
                throw new IllegalStateException("No moves available to make a selection");
            }
        }
    }



    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    private Optional<TreeNode> selectPromisingNode(TreeNode rootNode) {
        TreeNode node = rootNode;
        while (!node.children.isEmpty()) {
            node = node.children.stream()
                    .max(Comparator.comparing(this::calculateUCB1))
                    .orElse(null);
        }
        return Optional.ofNullable(node);
    }

    private double calculateUCB1(TreeNode node) {
        if (node.visits == 0) return Double.MAX_VALUE;
        return (double) node.wins / node.visits +
                EXPLORATION_FACTOR * Math.sqrt(Math.log(node.parent.visits) / node.visits);
    }

    private void expandNode(TreeNode node) {
        IField field = node.state.getField(); // Get the field from the game state
        List<IMove> possibleMoves = field.getAvailableMoves();
        for (IMove move : possibleMoves) {
            IGameState nextState = node.performMove(move);
            TreeNode childNode = new TreeNode(nextState, node, move);
            node.children.add(childNode);
        }
    }

    private int simulateRandomPlayout(IGameState state) {
        // Assuming implementation details for simulation logic.
        return random.nextBoolean() ? 1 : 0; // Simplified random outcome.
    }

    private void backPropagation(TreeNode node, int result) {
        while (node != null) {
            node.visits++;
            if (result == 1) node.wins++;
            node = node.parent;
        }
    }

    private IMove selectBestMove(TreeNode rootNode) {
        if (rootNode.children.isEmpty()) {
            throw new IllegalStateException("No children nodes available to select a best move.");
        }

        TreeNode bestNode = null;
        double bestValue = Double.MIN_VALUE;
        for (TreeNode child : rootNode.children) {
            double value = (double) child.wins / child.visits;
            System.out.println("Child move: " + child.move + ", Wins: " + child.wins + ", Visits: " + child.visits + ", Value: " + value);
            if (value > bestValue) {
                bestValue = value;
                bestNode = child;
            }
        }

        if (bestNode != null) {
            System.out.println("Best move selected based on win ratio: " + bestNode.move);
            return bestNode.move;
        } else {
            System.out.println("Fallback to random move due to best node being null.");
            List<IMove> availableMoves = rootNode.state.getField().getAvailableMoves();
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }
    }



    // Placeholder methods remain conceptual and should be implemented based on game mechanics.
    private boolean gameOver(IGameState state) {
        IField field = state.getField();
        String[][] board = field.getBoard();

        // Check rows for a winning condition
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2]) && !board[i][0].equals(IField.EMPTY_FIELD)) {
                return true; // Game over, a player has won
            }
        }

        // Check columns for a winning condition
        for (int j = 0; j < 3; j++) {
            if (board[0][j].equals(board[1][j]) && board[1][j].equals(board[2][j]) && !board[0][j].equals(IField.EMPTY_FIELD)) {
                return true; // Game over, a player has won
            }
        }

        // Check diagonals for a winning condition
        if ((board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2]) && !board[0][0].equals(IField.EMPTY_FIELD)) ||
                (board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0]) && !board[0][2].equals(IField.EMPTY_FIELD))) {
            return true; // Game over, a player has won
        }

        // Check for a draw (no more available moves)
        if (field.isFull()) {
            return true; // Game over, it's a draw
        }

        // If none of the above conditions are met, the game is not over
        return false;
    }

}
