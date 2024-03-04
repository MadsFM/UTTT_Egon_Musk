/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.easv.bll.game.stats;

import java.time.LocalDateTime;

/**
 *
 * @author jeppjleemoritzled
 */
public class GameResult {
    
    private String player0;

    private String player1;

    private Winner winner;
    
    private LocalDateTime date = LocalDateTime.now();

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public enum Winner{
        player0,
        player1,
        tie
    }

    public GameResult(String player0, String getPlayer1, Winner winner) {
        this.player0 = player0;
        this.player1 = getPlayer1;
        this.winner = winner;
    }

    public Winner getWinner() {
        return winner;
    }

    public void setWinner(Winner winner) {
        this.winner = winner;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer0() {
        return player0;
    }

    public void setPlayer0(String player0) {
        this.player0 = player0;
    }

}
