package dk.easv.bll.bot;

import dk.easv.bll.bot.IBot;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;



public class LocalPrioritisedListBot implements IBot {

    private static final String BOTNAME = "Local Prio ListBot";
    // Moves {row, col} in order of preferences. {0, 0} at top-left corner
    protected int[][] preferredMoves = {
            {1, 1}, //Center
            {0, 0}, {2, 2}, {0, 2}, {2, 0},  //Corners ordered across
            {0, 1}, {2, 1}, {1, 0}, {1, 2}}; //Outer Middles ordered across

    /**
     * Makes a turn. Edit this method to make your bot smarter.
     * A bot that uses a local prioritised list algorithm, in order to win any local board,
     * and if all boards are available for play, it'll run a on the macroboard,
     * to select which board to play in.
     *
     * @return The selected move we want to make.
     */
    @Override
    public IMove doMove(IGameState state) {
        //Find macroboard to play in
        for (int[] move : preferredMoves)
        {
            if(state.getField().getMacroboard()[move[0]][move[1]].equals(IField.AVAILABLE_FIELD))
            {
                //find move to play
                for (int[] selectedMove : preferredMoves)
                {
                    int x = move[0]*3 + selectedMove[0];
                    int y = move[1]*3 + selectedMove[1];
                    if(state.getField().getBoard()[x][y].equals(IField.EMPTY_FIELD))
                    {
                        return new Move(x,y);
                    }
                }
            }
        }

        //NOTE: Something failed, just take the first available move I guess!
        return state.getField().getAvailableMoves().get(0);
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
