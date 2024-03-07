package dk.easv.bll.bot;

import dk.easv.bll.field.Field;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;

public class JerryBot implements IBot {
    private static final int MAX_RETRIES = 3;
    private static final String PLAYER_0 = "0"; // Identifier for player 0
    private static final String PLAYER_1 = "1"; // Identifier for player 1

    @Override
    public IMove doMove(IGameState state) {
        int moveNumber = state.getMoveNumber();
        System.out.println("Move number: " + moveNumber);

        IMove move = selectMoveBasedOnGamePhase(state);

        // If no valid moves are found, use a default move
        if (move == null) {
            System.out.println("No applicable strategy found. Using default move.");
            move = defaultMove(state.getField());
        }

        // Attempt to make the move
        if (move != null && isValidMove(state, move)) {
            System.out.println("Selected move: " + move.getX() + ", " + move.getY());
            return move;
        }

        // Retry logic
        for (int i = 0; i < MAX_RETRIES; i++) {
            System.out.println("Retry #" + (i + 1));
            move = selectMoveBasedOnGamePhase(state);
            if (move != null && isValidMove(state, move)) {
                System.out.println("Selected move: " + move.getX() + ", " + move.getY());
                return move;
            }
        }

        System.out.println("Maximum number of retries reached. Stopping the process.");
        // Return a default move if all retries fail
        return defaultMove(state.getField());
    }


    private IMove defaultMove(IField field) {
        List<IMove> availableMoves = field.getAvailableMoves();

        // If there are available moves, randomly select one
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(new Random().nextInt(availableMoves.size()));
        }

        // If no available moves, return null
        return null;
    }

    private IMove selectMoveBasedOnGamePhase(IGameState state) {
        int emptyPositions = countEmptyPositions(state.getField());
        IMove move;

        // Adjust these thresholds as needed based on your game strategy
        int earlyGameThreshold = calculateEarlyGameThreshold(state);
        int lateGameThreshold = calculateLateGameThreshold(state);

        if (emptyPositions > earlyGameThreshold) {
            System.out.println("Switching to Early Game Strategy");
            move = earlyGameStrategy(state);
        } else if (emptyPositions <= lateGameThreshold) {
            System.out.println("Switching to Late Game Strategy");
            move = lateGameStrategy(state);
        } else {
            System.out.println("Switching to Middle Game Strategy");
            move = middleGameStrategy(state);
        }

        return move;
    }

    private boolean isValidMove(IGameState state, IMove move) {
        IField field = state.getField();
        return field.isInActiveMicroboard(move.getX(), move.getY()) &&
                field.getPlayerId(move.getX(), move.getY()).equals(IField.EMPTY_FIELD);
    }

    private int countEmptyPositions(IField field) {
        int count = 0;
        for (String[] row : field.getBoard()) {
            for (String cell : row) {
                if (cell.equals(IField.EMPTY_FIELD)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int calculateEarlyGameThreshold(IGameState state) {
        // Calculate the total number of positions on the board
        int totalPositions = state.getField().getBoard().length * state.getField().getBoard()[0].length;

        // Define the percentage of empty positions to consider as early game
        double earlyGamePercentage = 0.7; // Adjust this value as needed

        // Calculate the early game threshold based on the percentage
        return (int) (totalPositions * earlyGamePercentage);
    }

    private int calculateLateGameThreshold(IGameState state) {
        // Calculate the total number of positions on the board
        int totalPositions = state.getField().getBoard().length * state.getField().getBoard()[0].length;

        // Define the percentage of empty positions to consider as late game
        double lateGamePercentage = 0.15; // Adjust this value as needed

        // Calculate the late game threshold based on the percentage
        return (int) (totalPositions * lateGamePercentage);
    }


    private IMove earlyGameStrategy(IGameState state) {
        IField field = state.getField();

        // Check if the center of the board is available
        if (field.isInActiveMicroboard(1, 1)) {
            return new Move(1, 1);
        }

        List<IMove> availableCorners = getAvailableCorners(field);
        List<IMove> availableEdges = getAvailableEdges(field);

        // Combine available corners and edges
        List<IMove> availableMoves = new ArrayList<>();
        availableMoves.addAll(availableCorners);
        availableMoves.addAll(availableEdges);

        // Shuffle the available moves to randomize selection
        Collections.shuffle(availableMoves);

        // Select the first valid move
        for (IMove move : availableMoves) {
            if (isValidMove(state, move)) {
                return move;
            }
        }

        // No valid moves found, return null
        return null;
    }

    private IMove middleGameStrategy(IGameState state) {
        IField field = state.getField();

        // Block opponent's immediate win if possible
        IMove move = blockOpponentWin(field);
        if (move != null && isValidMove(state, move)) {
            return move;
        }

        // Claim third position if possible
        move = claimThirdPosition(field);
        if (move != null && isValidMove(state, move)) {
            return move;
        }

        // No valid moves found, return null
        return null;
    }

    private IMove lateGameStrategy(IGameState state) {
        IField field = state.getField();

        // Find immediate win if possible
        IMove move = findImmediateWin(field);
        if (move != null && isValidMove(state, move)) {
            return move;
        }

        // Block opponent's immediate win if possible
        move = blockOpponentWin(field);
        if (move != null && isValidMove(state, move)) {
            return move;
        }

        // No valid moves found, return null
        return null;
    }


    private List<IMove> getAvailableCorners(IField field) {
        List<IMove> availableCorners = new ArrayList<>();
        if (field.isInActiveMicroboard(0, 0))
            availableCorners.add(new Move(0, 0));
        if (field.isInActiveMicroboard(0, 2))
            availableCorners.add(new Move(0, 2));
        if (field.isInActiveMicroboard(2, 0))
            availableCorners.add(new Move(2, 0));
        if (field.isInActiveMicroboard(2, 2))
            availableCorners.add(new Move(2, 2));
        return availableCorners;
    }

    private List<IMove> getAvailableEdges(IField field) {
        List<IMove> availableEdges = new ArrayList<>();
        if (field.isInActiveMicroboard(0, 1))
            availableEdges.add(new Move(0, 1));
        if (field.isInActiveMicroboard(1, 0))
            availableEdges.add(new Move(1, 0));
        if (field.isInActiveMicroboard(1, 2))
            availableEdges.add(new Move(1, 2));
        if (field.isInActiveMicroboard(2, 1))
            availableEdges.add(new Move(2, 1));
        return availableEdges;
    }

    private IMove blockOpponentWin(IField field) {
        String[][] board = field.getBoard();
        for (int i = 0; i < board.length; i++) {
            if (board[i][0].equals(PLAYER_1) && board[i][1].equals(PLAYER_1) && board[i][2].equals(IField.EMPTY_FIELD)) {
                return new Move(i, 2);
            }
        }
        return null;
    }

    private IMove claimThirdPosition(IField field) {
        String[][] board = field.getBoard();
        for (int i = 0; i < board.length; i++) {
            if (board[i][0].equals(PLAYER_0) && board[i][1].equals(PLAYER_0) && board[i][2].equals(IField.EMPTY_FIELD)) {
                return new Move(i, 2);
            }
        }
        return null;
    }

    private IMove findImmediateWin(IField field) {
        String[][] board = field.getBoard();
        for (int i = 0; i < board.length; i++) {
            if (board[i][0].equals(PLAYER_0) && board[i][1].equals(PLAYER_0) && board[i][2].equals(PLAYER_0)) {
                return new Move(i, 2);
            }
        }
        return null;
    }

    @Override
    public String getBotName() {
        return "JerryBot";
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
}
