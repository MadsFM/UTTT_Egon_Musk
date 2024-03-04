package dk.easv.gui;

import com.jfoenix.controls.JFXButton;

import dk.easv.bll.bot.*;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.stats.GameResult;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;
import static dk.easv.gui.util.FontAwesomeHelper.getFontAwesomeIconFromPlayerId;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UTTTGameController implements Initializable {

    private long botDelay = 500;
    @FXML
    private GridPane gridMacro;

    @FXML
    private StackPane stackMain;

    private final GridPane[][] gridMicros = new GridPane[3][3];
    private final JFXButton[][] jfxButtons = new JFXButton[9][9];

    BoardModel model;
    StatsModel statsModel;
    IBot bot0 = null;
    IBot bot1 = null;
    String player0 = null;
    String player1 = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gridMacro.toFront(); // Or the buttons will not work
        createMicroGridPanes();
    }

    public void startGame() {
        if (model != null) {
            model.removeListener(observable -> update());
        }

        model.addListener(observable -> update());

        // HumanVsHuman
        if (player0 != null && player1 != null) {

        }
        // HumanVsAI
        else if (bot1 != null && player0 != null) {

        }
        // AIvsHuman
        else if (bot0 != null && player1 != null) {
            // FIX HERE, KEEPS ASKING FOR VALID MOVE IF BOT PLAYS INVALID good for player bot not bot
            doBotMove();
        }
        // AIvsAI
        else if (bot0 != null && bot1 != null) {

            Thread t = new Thread(() -> {
                while (model.getGameOverState() == GameManager.GameOverState.Active
                        && model.getGameState().getField().getAvailableMoves().size()>0) {
                    // FIX HERE, KEEPS ASKING FOR VALID MOVE IF BOT PLAYS INVALID
                    boolean isValid = doBotMove();
                    try {
                        Thread.sleep(botDelay);
                    }
                    catch (InterruptedException ex) {
                        Logger.getLogger(UTTTGameController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            t.setDaemon(true); // Stops thread when main thread dies
            t.start();

        }
    }

    private boolean doBotMove() {
        int currentPlayer = model.getCurrentPlayer();
        Boolean valid = model.doMove();
        if(!valid) {
            int opponent = 0;
            if(model.getCurrentPlayer()==0)
                opponent = 1;
            model.forceGameOver(opponent);
            showWinnerPane(""+opponent);

        }
        else
            checkAndLockIfGameEnd(currentPlayer);
        return valid;
    }

    private boolean doMove(IMove move) throws Exception {
        int currentPlayer = model.getCurrentPlayer();
        boolean validMove = model.doMove(move);
        if(!validMove) return false;
        checkAndLockIfGameEnd(currentPlayer);
        return true;
    }

    private String getNameFromId(int winnerId) {
        if (winnerId == 0) {
            if (bot0 != null) {
                return bot0.getBotName();
            }
            else {
                return player0;
            }
        }
        else if (winnerId == 1) {
            if (bot1 != null) {
                return bot1.getBotName();
            }
            else {
                return player1;
            }
        }
        throw new RuntimeException("Player id not found " + winnerId);
    }


    private void showWinnerPane(String winner) {
        String winMsg;
        GameResult.Winner winStatus = GameResult.Winner.tie;
        if (winner.equalsIgnoreCase("TIE")) {
            winMsg = "Game tie";
        }
        else {
            int winnerId = Integer.parseInt(winner);
            winMsg = getNameFromId(winnerId) + " wins";
            if(model.getIsForced())
                winMsg += " (opponent false move)";
            winStatus = winnerId == 0
                    ? GameResult.Winner.player0
                    : GameResult.Winner.player1;
        }

        GameResult gr = new GameResult(
                getNameFromId(0),
                getNameFromId(1),
                winStatus);
        statsModel.addGameResult(gr);

        Label lblWinAnnounce = new Label(winMsg);
        lblWinAnnounce.setAlignment(Pos.CENTER);
        lblWinAnnounce.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lblWinAnnounce.getStyleClass().add("winner-text");
        lblWinAnnounce.getStyleClass().add("player" + winner);

        Label lbl = new Label();
        lbl.getStyleClass().add("winmsg");
        lbl.getStyleClass().add("player" + winner);

        lbl.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);

        Text fontAwesomeIcon = getFontAwesomeIconFromPlayerId(winner + "");
        lbl.setGraphic(fontAwesomeIcon);
        GridPane gridPane = new GridPane();
        gridPane.addColumn(0);
        gridPane.addRow(0);
        gridPane.addRow(1);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(100);
        cc.setHgrow(Priority.ALWAYS); // allow column to grow
        cc.setFillWidth(true); // ask nodes to fill space for column
        gridPane.getColumnConstraints().add(cc);

        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS); // allow row to grow
        rc.setFillHeight(true);
        rc.setPercentHeight(90);
        gridPane.getRowConstraints().add(rc);
        RowConstraints rc2 = new RowConstraints();
        rc2.setVgrow(Priority.ALWAYS); // allow row to grow
        rc2.setFillHeight(true);
        rc2.setPercentHeight(10);
        gridPane.getRowConstraints().add(rc2);

        gridPane.add(lbl, 0, 0);
        gridPane.add(lblWinAnnounce, 0, 1);
        gridPane.setGridLinesVisible(true);

        Platform.runLater(() -> stackMain.getChildren().add(gridPane));

    }

    private void createMicroGridPanes() {
        for (int i = 0; i < 3; i++) {
            gridMacro.addRow(i);
            for (int k = 0; k < 3; k++) {
                GridPane gp = new GridPane();
                for (int m = 0; m < 3; m++) {
                    gp.addColumn(m);
                    gp.addRow(m);
                }
                gridMicros[i][k] = gp;
                for (int j = 0; j < 3; j++) {
                    ColumnConstraints cc = new ColumnConstraints();
                    cc.setPercentWidth(33);
                    cc.setHgrow(Priority.ALWAYS); // allow column to grow
                    cc.setFillWidth(true); // ask nodes to fill space for column
                    gp.getColumnConstraints().add(cc);

                    RowConstraints rc = new RowConstraints();
                    rc.setVgrow(Priority.ALWAYS); // allow row to grow
                    rc.setFillHeight(true);
                    rc.setPercentHeight(33);
                    gp.getRowConstraints().add(rc);
                }

                gp.setGridLinesVisible(true);
                gridMacro.addColumn(k);
                gridMacro.add(gp, i, k);
            }
        }
        insertButtonsIntoGridPanes();
    }

    private void insertButtonsIntoGridPanes() {
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 3; k++) {
                GridPane gp = gridMicros[i][k];
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        JFXButton btn = new JFXButton("");
                        btn.setButtonType(JFXButton.ButtonType.RAISED);
                        btn.getStyleClass().add("tictaccell");
                        btn.setUserData(new Move(x + i * 3, y + k * 3));
                        btn.setFocusTraversable(false);
                        btn.setOnMouseClicked(
                                event -> {
                                    try {
                                        doMove((IMove) btn.getUserData()); // Player move
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    boolean isHumanVsBot = player0 != null ^ player1 != null;
                                    if (model.getGameOverState() == GameManager.GameOverState.Active && isHumanVsBot) {
                                        int currentPlayer = model.getCurrentPlayer();
                                        Boolean valid = model.doMove();
                                        checkAndLockIfGameEnd(currentPlayer);
                                    }
                                }
                        );
                        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        gp.add(btn, x, y);
                        jfxButtons[x + i * 3][y + k * 3] = btn;
                    }
                }
            }
        }
    }

    private void checkAndLockIfGameEnd(int currentPlayer) {
        if (model.getGameOverState() != GameManager.GameOverState.Active) {
            String[][] macroboard = model.getMacroboard();
            // Lock game
            for (int i = 0; i < 3; i++) {
                for (int k = 0; k < 3; k++) {
                    if (macroboard[i][k].equals(IField.AVAILABLE_FIELD)) {
                        macroboard[i][k] = IField.EMPTY_FIELD;
                    }
                }
            }
            if (model.getGameOverState().equals(GameManager.GameOverState.Tie)) {
                Platform.runLater(() -> showWinnerPane("TIE"));
            }
            else {
                Platform.runLater(() -> showWinnerPane(currentPlayer + ""));
            }
        }
    }

    private void updateGUI() throws RuntimeException {
        String[][] board = model.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int k = 0; k < board[i].length; k++) {
                if (board[i][k].equals(IField.EMPTY_FIELD)) {
                    jfxButtons[i][k].getStyleClass().add("empty");
                }
                else {
                    jfxButtons[i][k].getStyleClass().add("player" + board[i][k]);
                    jfxButtons[i][k].setGraphic(getFontAwesomeIconFromPlayerId(board[i][k]));
                }

            }
        }
        String[][] macroBoard = model.getMacroboard();
        for (int i = 0; i < macroBoard.length; i++) {
            for (int k = 0; k < macroBoard[i].length; k++) {
                if (gridMicros[i][k] != null) {
                    // Highlight available plays
                    if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD)) {
                        gridMicros[i][k].getStyleClass().add("highlight");
                    }
                    else {
                        gridMicros[i][k].getStyleClass().removeAll("highlight");
                    }

                    // If there is a win
                    if (!macroBoard[i][k].equals(IField.AVAILABLE_FIELD)
                            && !macroBoard[i][k].equals(IField.EMPTY_FIELD)
                            && gridMicros[i][k] != null) {
                        setMacroWinner(i, k);
                    }
                }
            }
        }

    }

    private void setMacroWinner(int x, int y) {
        String[][] macroBoard = model.getMacroboard();
        gridMacro.getChildren().remove(gridMicros[x][y]);
        Label lbl = new Label("");
        lbl.setGraphic(getFontAwesomeIconFromPlayerId(macroBoard[x][y]));
        lbl.getStyleClass().add("winner-label");
        lbl.getStyleClass().add("player" + macroBoard[x][y]);
        lbl.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridMicros[x][y] = null;
        gridMacro.add(lbl, x, y);
    }

    private void printBoardInConsole() {
        String[][] board = model.getGameState().getField().getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int k = 0; k < board[i].length; k++) {
                System.out.print("|" + board[i][k] + "|");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void update() {
        //updateConsole();
        Platform.runLater(() -> updateGUI());
    }

    public void setupGame(IBot bot0, IBot bot1) {
        model = new BoardModel(bot0, bot1);
        this.bot0 = bot0;
        this.bot1 = bot1;
    }

    public void setupGame(String humanName, IBot bot1) {
        model = new BoardModel(bot1, true);
        this.bot1 = bot1;
        this.player0 = humanName;
    }

    public void setupGame(IBot bot0, String humanName) {
        model = new BoardModel(bot0, false);
        this.bot0 = bot0;
        this.player1 = humanName;
    }

    public void setupGame(String humanName0, String humanName1) {
        model = new BoardModel();
        this.player0 = humanName0;
        this.player1 = humanName1;
    }

    public void setSpeed(double speed) {
        botDelay = Math.round(speed);
    }

    public void setStatsModel(StatsModel statsModel) {
        this.statsModel = statsModel;
    }
}
