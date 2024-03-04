package dk.easv.bll.bot;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author jeppjleemoritzled
 */
public class DrunkenBot extends LocalPrioritisedListBot {
    private static final String BOTNAME="Drunken Bot";
    
    // Drunken bot is worse than random but wins against it's super class
    public DrunkenBot() {
        int[][] pref = {
            {0, 0}, {2, 2}, {0, 2}, {2, 0},  //Corners ordered across
            {0, 1}, {2, 1}, {1, 0}, {1, 2}, //Outer Middles ordered across
            {1, 1}}; //Center
        super.preferredMoves = pref;

    }

    @Override
    public String getBotName() {
        return BOTNAME; //To change body of generated methods, choose Tools | Templates.
    }
    
    

}
