package dk.easv.bll.bot;

import dk.easv.bll.bot.IBot;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Random;

public class RandomBot implements IBot {

    private static final String BOTNAME = "Random Dude";
    private Random rand = new Random();

    /**
     * Makes a turn. Edit this method to make your bot smarter.
     * Currently does only random moves.
     *
     * @return The selected move we want to make.
     */
    @Override
    public IMove doMove(IGameState state) {
        List<IMove> moves = state.getField().getAvailableMoves();
        if (moves.size() > 0) {
            return moves.get(rand.nextInt(moves.size())); /* get random move from available moves */
        }

        return null;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
