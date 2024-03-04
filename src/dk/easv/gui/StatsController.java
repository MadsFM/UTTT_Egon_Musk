/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.easv.gui;

import com.jfoenix.controls.JFXListView;
import dk.easv.bll.game.stats.GameResult;
import dk.easv.bll.game.stats.GameResult.Winner;
import dk.easv.gui.util.FontAwesomeHelper;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jeppjleemoritzled
 */
public class StatsController implements Initializable {

    @FXML
    private JFXListView<GameResult> listResults;

    private StatsModel statsModel;
    
    private final String[] allPlayerstyles = {"playerTIE","player0","player1"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listResults.setCellFactory(p->new CustomGameResultListCell());
    }    

    public void setStatsModel(StatsModel statsModel, Stage stage) {
        this.statsModel = statsModel;
        listResults.setItems(statsModel.getGameResults());
        stage.titleProperty().bind(statsModel.lastSimulationResultsProperty());
    }

    @FXML
    private void clickClearList(ActionEvent event) {
        statsModel.clear();
    }

    private class CustomGameResultListCell extends ListCell<GameResult> {

        @Override
        protected void updateItem(GameResult item, boolean empty) {
            super.updateItem(item, empty);
            Node fontAwe=FontAwesomeHelper.getFontAwesomeIconFromPlayerId("TIE");
            this.getStyleClass().removeAll(allPlayerstyles);
            String winName = "Tie";
            String styleClass = "playerTIE";
            if (!empty && item != null) {
                if(item.getWinner()==Winner.player0) {
                    fontAwe =
                        FontAwesomeHelper.getFontAwesomeIconFromPlayerId("0");
                    winName = item.getPlayer0();
                    styleClass=("player0");
                } else if (item.getWinner()==Winner.player1) {
                    fontAwe =
                        FontAwesomeHelper.getFontAwesomeIconFromPlayerId("1");
                    winName = item.getPlayer1();
                    styleClass=("player1");
                }
                this.setGraphic(fontAwe);
                this.getStyleClass().add(styleClass);
                this.getStyleClass().add("stat-items");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss");
                this.setText(item.getDate().format(dtf) + "\t" +
                        item.getPlayer0() + 
                        " vs " + 
                        item.getPlayer1() + 
                        " | " + winName);
                this.setContentDisplay(ContentDisplay.RIGHT);
            }   else
                setText(null);
        }
    }
    
    
}
