package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;

public class KakaoBotv1 implements IBot {
    @Override
    public IMove doMove(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        String[][] macroBoard = state.getField().getMacroboard();
        String[][] board = state.getField().getBoard();

       // System.out.println(Arrays.deepToString(macroBoard));
      //  System.out.println(Arrays.deepToString(board));
        //System.out.println(availableMoves);


        KakaoBotv1.GameSimulator simulator = createSimulator(state);
        IGameState currentState = simulator.currentState;

        List<IMove> kakaoMoves = currentState.getField().getAvailableMoves();

        List<IMove> badMoves = new ArrayList<>();
        for (IMove kakaoMove : kakaoMoves) {
            KakaoBotv1.GameSimulator tempSimulator = createSimulator(simulator.currentState);
            tempSimulator.updateGame(kakaoMove);

            List<IMove> enemyMoves = tempSimulator.currentState.getField().getAvailableMoves();
            for (IMove enemyMove : enemyMoves) {
              // System.out.println("enemyMoves: " + enemyMoves);
                KakaoBotv1.GameSimulator enemySim = createSimulator(tempSimulator.currentState);
                enemySim.updateGame(enemyMove);

//                System.out.println(enemySim);
                if (enemySim.gameOver != KakaoBotv1.GameOverState.Active) {
//                    System.out.println(enemySim);
                    badMoves.add(kakaoMove);
                }
            }
        }

/*        System.out.println("availableMoves: " + availableMoves);
        System.out.println("badMoves: " + badMoves);*/

        List<IMove> goodMoves = new ArrayList<>();

        for (IMove mv : availableMoves) {
            boolean isBad = false;
            for (IMove badMove : badMoves) {
                if (mv.getX() == badMove.getX() && mv.getY() == badMove.getY()) {
                    isBad = true;
                }
            }

            if (!isBad) {
                goodMoves.add(mv);
            }
        }


        //tjekker microBoards
        IMove strategicMove = evaluateStrategicMoves(state, goodMoves);
        if (strategicMove != null) {
            return strategicMove;
        }

//        System.out.println("goodMoves: " + goodMoves);

        if (goodMoves.isEmpty()) return availableMoves.get(new Random().nextInt(availableMoves.size()));
        return goodMoves.get(new Random().nextInt(goodMoves.size()));
    }

    private IMove evaluateStrategicMoves(IGameState state, List<IMove> moves) {
        String playerSymbol = "0"; // Bot's symbol
        String opponentSymbol = "1"; // Opponent's symbol

        // Assuming you're evaluating for the bot first, then for blocking opponent's moves.
        // First, try to find a winning move for the bot
        for (IMove move : moves) {
            if (isStrategicMove(state.getField().getBoard(), move, playerSymbol, true)) {
                return move; // A winning move for the bot
            }
        }
        for (int i = 0; i < 9; i++) { // Loop through all microboards
            int microX = i % 3;
            int microY = i / 3;
            String[][] microboard = getMicroboard(state.getField().getBoard(), microX, microY);
            for (IMove move : moves) {
                if (isStrategicMove(microboard, move, playerSymbol, false)) {
                    return move; // A winning move for the bot in MicroBoard
                }
            }
        }

        // If no winning move for the bot is found, try to find a move to block the opponent's win
        for (IMove move : moves) {
            if (isStrategicMove(state.getField().getBoard(), move, opponentSymbol, true)) {
                return move; // A blocking move against the opponent in MacroBoard
            }
        }

        // If no strategic move found, return null
        return null;
    }

    private boolean isStrategicMove(String[][] board, IMove move, String playerSymbol, boolean isMacroBoard) {
        boolean strategic = false;
        String[][] microboard;
        if (isMacroBoard) {
            microboard = getMicroboard(board, move.getX() / 3, move.getY() / 3);
        } else {
            microboard = board;
        }
        // Tjekker diagonaler
        String[] diag = extractLine(microboard, -1, "diag");
        String[] antiDiag = extractLine(microboard, -1, "antiDiag");
        if (hasWinningPotential(diag, playerSymbol) || hasWinningPotential(antiDiag, playerSymbol)) {
            strategic = true;
        }

        // Specielt tilfælde: Evaluerer om trækket blokerer e   n potentiel sejr for modstanderen
        String opponentSymbol = playerSymbol.equals("0") ? "1" : "0";
        if (!strategic) { // Hvis ikke allerede fundet strategisk for spilleren, tjek for blokering af modstander
            for (int i = 0; i < 3; i++) {
                String[] row = extractLine(microboard, i, "row");
                String[] col = extractLine(microboard, i, "col");
                if (hasWinningPotential(row, opponentSymbol) || hasWinningPotential(col, opponentSymbol)) {
                    strategic = true;
                    break;
                }
            }
            String[] diagOpp = extractLine(microboard, -1, "diag");
            String[] antiDiagOpp = extractLine(microboard, -1, "antiDiag");
            if (hasWinningPotential(diagOpp, opponentSymbol) || hasWinningPotential(antiDiagOpp, opponentSymbol)) {
                strategic = true;
            }
        }

        return strategic;
    }

    // Ekstraherer en linje (række, kolonne, diagonal) fra microboardet
    private String[] extractLine(String[][] board, int index, String type) {
        String[] line = new String[3];
        switch (type) {
            case "row":
                System.arraycopy(board[index], 0, line, 0, 3);
                break;
            case "col":
                for (int i = 0; i < 3; i++) line[i] = board[i][index];
                break;
            case "diag":
                for (int i = 0; i < 3; i++) line[i] = board[i][i];
                break;
            case "antiDiag":
                for (int i = 0; i < 3; i++) line[i] = board[i][2 - i];
                break;
        }
        return line;
    }

    // Tjekker om en specifik række, kolonne eller diagonal har potentiale til at vinde
    private boolean hasWinningPotential(String[] line, String playerSymbol) {
        int playerCount = 0;
        int emptyCount = 0;
        for (String cell : line) {
            if (cell.equals(playerSymbol)) playerCount++;
            else if (cell.equals(IField.EMPTY_FIELD)) emptyCount++;
        }
        // En linje har vinderpotentiale, hvis den indeholder mindst én af spillerens symboler og resten er tomme
        return playerCount > 0 && (playerCount + emptyCount) == 3;
    }



    public String[][] getMicroboard(String[][] board, int macroX, int macroY) {
        String[][] microboard = new String[3][3];
        int startX = macroX * 3;
        int startY = macroY * 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                microboard[i][j] = board[startX + i][startY + j];
            }
        }
        return microboard;
    }







    private KakaoBotv1.GameSimulator createSimulator(IGameState state) {
        KakaoBotv1.GameSimulator simulator = new KakaoBotv1.GameSimulator(new GameState());
        simulator.setGameOver(KakaoBotv1.GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }

    @Override
    public String getBotName() {
        return "Kakao Botv1";
    }

    // Simulator
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
            KakaoBotv1.Move move = (KakaoBotv1.Move) o;
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
        private volatile KakaoBotv1.GameOverState gameOver = KakaoBotv1.GameOverState.Active;

        public void setGameOver(KakaoBotv1.GameOverState state) {
            gameOver = state;
        }

        public KakaoBotv1.GameOverState getGameOver() {
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
                if (isWin(macroBoard, new KakaoBotv1.Move(macroX, macroY), "" + currentPlayer))
                    gameOver = KakaoBotv1.GameOverState.Win;
                else if (isTie(macroBoard, new KakaoBotv1.Move(macroX, macroY)))
                    gameOver = KakaoBotv1.GameOverState.Tie;
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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String[][] board = currentState.getField().getBoard();
            for (int i = 0; i < board.length; i++) {
                if (i % 3 == 0 && i != 0) {
                    sb.append("------+-------+------\n");
                }

                for (int j = 0; j < board[i].length; j++) {
                    if (j % 3 == 0 && j != 0) {
                        sb.append("| ");
                    }
                    sb.append(board[j][i]).append(" ");
                }

                sb.append("\n");
            }
            return sb.toString();
        }
    }
}