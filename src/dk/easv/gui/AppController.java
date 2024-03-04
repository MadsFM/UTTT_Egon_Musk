package dk.easv.gui;

import com.jfoenix.controls.*;
import dk.easv.bll.bot.IBot;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.stats.GameResult;
import dk.easv.dal.DynamicBotClassHandler;
import static dk.easv.gui.util.FontAwesomeHelper.getFontAwesomeIconFromPlayerId;
import static dk.easv.dal.DynamicBotClassHandler.loadBotList;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.AnchorPane;

public class AppController implements Initializable {

    public JFXButton btnTrash;
    public JFXButton btnDiamond;
    @FXML
    private JFXTextField txtHumanNameLeft;
    @FXML
    private JFXRadioButton radioRightAI;
    @FXML
    private JFXTextField txtHumanNameRight;
    @FXML
    private JFXRadioButton radioLeftAI;
    @FXML
    private JFXRadioButton radioRightHuman;
    @FXML
    private ToggleGroup toggleLeft;
    @FXML
    private ToggleGroup toggleRight;

    @FXML
    private JFXButton btnStart;
    @FXML
    private JFXComboBox<IBot> comboBotsRight;
    @FXML
    private JFXComboBox<IBot> comboBotsLeft;
    @FXML
    private JFXRadioButton radioLeftHuman;
    @FXML
    private JFXSlider sliderSpeed;

    StatsModel statsModel = new StatsModel();
    @FXML
    private AnchorPane anchorMain;
    private BooleanProperty simulation= new SimpleBooleanProperty(false);
    private volatile int winsBot1 = 0;
    private volatile int winsBot2 = 0;
    private volatile int ties = 0;
    @FXML
    private JFXToggleButton toggleBtnSim;
    @FXML
    private JFXSlider sliderSim;
    
    private Stage statsWindow  = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<IBot> bots = FXCollections.observableArrayList();
        try {
            DynamicBotClassHandler.writeBotsToTextFile();
            bots = loadBotList();
        }
        catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }

        comboBotsLeft.setButtonCell(new CustomIBotListCell());
        comboBotsLeft.setCellFactory(p -> new CustomIBotListCell());
        comboBotsLeft.setItems(bots);
        comboBotsRight.setButtonCell(new CustomIBotListCell());
        comboBotsRight.setCellFactory(p -> new CustomIBotListCell());
        comboBotsRight.setItems(bots);
        btnStart.setDisableVisualFocus(true);
        btnDiamond.setGraphic(getFontAwesomeIconFromPlayerId("1"));
        btnTrash.setGraphic(getFontAwesomeIconFromPlayerId("0"));

        radioLeftAI.selectedProperty().addListener((observable, oldValue, newValue) -> comboBotsLeft.setDisable(!newValue));
        radioLeftHuman.selectedProperty().addListener((observable, oldValue, newValue) -> txtHumanNameLeft.setDisable(!newValue));
        radioRightAI.selectedProperty().addListener((observable, oldValue, newValue) -> comboBotsRight.setDisable(!newValue));
        radioRightHuman.selectedProperty().addListener((observable, oldValue, newValue) -> txtHumanNameRight.setDisable(!newValue));
        comboBotsLeft.getSelectionModel().selectFirst();
        comboBotsLeft.setDisable(true);
        comboBotsRight.getSelectionModel().selectFirst();
        comboBotsRight.setDisable(true);
        simulation.bind(toggleBtnSim.selectedProperty());
        /*simulation.addListener((obs,old,isSelected)->{
            if(isSelected){
                
            }
        });*/
    }

    @FXML
    private void clickOpenStats(ActionEvent event) throws IOException {
        openStatsWindow();
    }
    
    private void openStatsWindow() throws IOException {
        if(statsWindow==null)
        {
            statsWindow = new Stage();
            statsWindow.initModality(Modality.WINDOW_MODAL);
            FXMLLoader fxLoader = new FXMLLoader(
                    getClass().getResource("Stats.fxml"));

            Parent root = fxLoader.load();

            StatsController controller
                    = ((StatsController) fxLoader.getController());

            Scene scene = new Scene(root);
            statsWindow.setScene(scene);

            controller.setStatsModel(statsModel, statsWindow);
            statsWindow.setOnCloseRequest(
                    (obs)-> statsWindow=null
            );
            statsWindow.showAndWait();
        }
        else
        {
            statsWindow.toFront();
        }
    }

    private void startSimulation(long amountOfSimulations) {
        int multiCores = Runtime.getRuntime().availableProcessors();
        winsBot1 = 0;
        winsBot2 = 0;
        ties = 0;
        for (int i = 0; i < multiCores; i++) {
            Thread t = new Thread(
                    new Simulator(amountOfSimulations/multiCores, 
                        this.comboBotsLeft.getValue().getClass(), 
                        this.comboBotsRight.getValue().getClass()));
            t.setDaemon(true);
            t.start();
        }
    }

    @FXML
    private void clickSelector(ActionEvent event) {
        if(toggleLeft.getSelectedToggle()==radioLeftAI &&
                toggleRight.getSelectedToggle()== radioRightAI) {
            toggleBtnSim.setSelected(false);
            toggleBtnSim.setDisable(false);
            sliderSim.setDisable(false);
        } else {
            toggleBtnSim.setSelected(false);
            toggleBtnSim.setDisable(true);
            sliderSim.setDisable(true);
        }
    }
    
    private class Simulator implements Runnable{
        private final long amountOfSimulations;
        private IBot bot1;
        private IBot bot2;
        public Simulator(
                long amountOfSimulations, 
                Class<? extends IBot> b1, 
                Class<? extends IBot> b2) {

            this.amountOfSimulations=amountOfSimulations;
            try {
                this.bot1 = b1.newInstance();
                this.bot2 = b2.newInstance();
            }
            catch (InstantiationException ex) {
                Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IllegalAccessException ex) {
                Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @Override
        public void run() {
            for (int i = 0; i < amountOfSimulations/2; i++) {
                BoardModel model = new BoardModel(bot1, bot2);
                int currentPlayer = 0;
                while (model.getGameOverState() == GameManager.GameOverState.Active
                         && model.getGameState().getField().getAvailableMoves().size()>0) {
                    currentPlayer = model.getCurrentPlayer();
                    Boolean valid = model.doMove();
                    if (!valid) {
                        throw new RuntimeException("Bot not following rules!");
                    }
                }
                // There is a tie
                if (model.getGameOverState().equals(GameManager.GameOverState.Tie)) {
                    this.addGameResult(
                            new GameResult(
                                    bot1.getBotName(), 
                                    bot2.getBotName(), 
                                    GameResult.Winner.tie));
                    ties++;
                }
                else { // There is a winner
                    GameResult.Winner winResult=null;
                    if(currentPlayer==0) {
                        winsBot1++;
                        winResult = GameResult.Winner.player0;
                    }
                    else if(currentPlayer==1){
                        winsBot2++;
                        winResult = GameResult.Winner.player1;
                    }
                        
                    this.addGameResult(
                            new GameResult(
                                    bot1.getBotName(), 
                                    bot2.getBotName(), 
                                    winResult));
                }
                
            }
            for (int i = 0; i < amountOfSimulations/2; i++) {
                BoardModel model = new BoardModel(bot2, bot1);
                int currentPlayer = 0;
                while (model.getGameOverState() == GameManager.GameOverState.Active
                         && model.getGameState().getField().getAvailableMoves().size()>0) {
                    currentPlayer = model.getCurrentPlayer();
                    Boolean valid = model.doMove();
                    if (!valid) {
                        throw new RuntimeException("Bot not following rules!");
                    }
                }
                // There is a tie
                if (model.getGameOverState().equals(GameManager.GameOverState.Tie)) {
                    this.addGameResult(
                            new GameResult(
                                    bot2.getBotName(), 
                                    bot1.getBotName(), 
                                    GameResult.Winner.tie));
                    ties++;
                }
                else { // There is a winner
                    GameResult.Winner winResult=null;
                    if(currentPlayer==0) {
                        winsBot2++;
                        winResult = GameResult.Winner.player0;
                    }
                    else if(currentPlayer==1){
                        winsBot1++;
                        winResult = GameResult.Winner.player1;
                    }
                        
                    this.addGameResult(
                            new GameResult(
                                    bot2.getBotName(), 
                                    bot1.getBotName(), 
                                    winResult));
                }
                
            }
            setSimulationResults(bot1.getBotName() + " vs " +
                        bot2.getBotName() + " | " +
                        "w/w/t " + winsBot1 + "/" +
                        winsBot2 + "/" + ties);
        }
        private void setSimulationResults(String result) {
            Platform.runLater(()-> 
                statsModel.setLastSimulationResults(result));
        }
        private void addGameResult(GameResult gameResult) {
            Platform.runLater(()->
                statsModel.addGameResult(gameResult));
        }
    }
    private class CustomIBotListCell extends ListCell<IBot> {

        @Override
        protected void updateItem(IBot item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                setText(item.getBotName());
            }
            else {
                setText(null);
            }
        }
    }

    @FXML
    public void clickStart(ActionEvent actionEvent) throws IOException {
        if (simulation.get()) {
            startSimulation(Math.round(sliderSim.getValue()));
            statsModel.clear();
            openStatsWindow();
        }
        else {
            Stage primaryStage = new Stage();
            primaryStage.initModality(Modality.WINDOW_MODAL);
            FXMLLoader fxLoader = new FXMLLoader(getClass().getResource("UTTTGame.fxml"));

            Parent root = fxLoader.load();

            UTTTGameController controller = ((UTTTGameController) fxLoader.getController());

            if (toggleLeft.getSelectedToggle().equals(radioLeftAI)
                    && toggleRight.getSelectedToggle().equals(radioRightAI)) {
                controller.setupGame(comboBotsLeft.getSelectionModel().getSelectedItem(), comboBotsRight.getSelectionModel().getSelectedItem());
                primaryStage.setTitle(
                        comboBotsLeft.getSelectionModel().getSelectedItem().getBotName()
                        + " vs "
                        + comboBotsRight.getSelectionModel().getSelectedItem().getBotName());
            }
            else if (toggleLeft.getSelectedToggle().equals(radioLeftHuman)
                    && toggleRight.getSelectedToggle().equals(radioRightAI)) {
                controller.setupGame(txtHumanNameLeft.getText(), comboBotsRight.getSelectionModel().getSelectedItem());
                primaryStage.setTitle(
                        txtHumanNameLeft.getText()
                        + " vs "
                        + comboBotsRight.getSelectionModel().getSelectedItem().getBotName());
            }
            else if (toggleLeft.getSelectedToggle().equals(radioLeftAI)
                    && toggleRight.getSelectedToggle().equals(radioRightHuman)) {
                controller.setupGame(comboBotsLeft.getSelectionModel().getSelectedItem(), txtHumanNameRight.getText());
                primaryStage.setTitle(
                        comboBotsLeft.getSelectionModel().getSelectedItem().getBotName()
                        + " vs "
                        + txtHumanNameRight.getText());
            }
            else if (toggleLeft.getSelectedToggle().equals(radioLeftHuman)
                    && toggleRight.getSelectedToggle().equals(radioRightHuman)) {
                controller.setupGame(txtHumanNameLeft.getText(), txtHumanNameRight.getText());
                primaryStage.setTitle(
                        txtHumanNameLeft.getText()
                        + " vs "
                        + txtHumanNameRight.getText());
            }
            controller.setSpeed(sliderSpeed.getMax() - sliderSpeed.getValue());
            controller.startGame();
            controller.setStatsModel(statsModel);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.showAndWait();
        }
    }
}
