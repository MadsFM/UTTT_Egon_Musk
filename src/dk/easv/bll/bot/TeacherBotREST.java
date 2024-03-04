package dk.easv.bll.bot;

import com.google.gson.Gson;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * This bot just acts as a REST client application that communicates with a REST api in the
 * cloud.
 *
 * The cloud bot uses the Monte Carlo Search Tree algorithm for deciding it's next move.
 * It is a good benchmark for performance of your own bot, as it is hard to beat.
 *
 * This client implementation requires the Google GSon library.
 *
 */
public class TeacherBotREST implements IBot{
    private static final String BOT_NAME = "Teacher Bot (online)";
    // This bot requires a VPN connection to the EASV network
    private static final String SERVER_URI = "http://10.176.88.51:4567/doMove";
    @Override
    public IMove doMove(IGameState state) {
        Gson gson = new Gson();
        String jsonState = gson.toJson(state);

        HttpRequest request =
                HttpRequest.newBuilder(URI.create(SERVER_URI))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonState))
                .build();

        var client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());

        // This is not the most graceful exception handling, but we want the bot to die
        // if the connection fails. Normally we could retry, however that would violate
        // the max allowed time for the bot to think.
        } catch (IOException e) {
            throw new RuntimeException("Connection problems with "+ BOT_NAME,e);

        } catch (InterruptedException e) {
            throw new RuntimeException("Connection problems with "+ BOT_NAME,e);
        }

        Move move = gson.fromJson(response.body(), Move.class);
        return move;
    }

    @Override
    public String getBotName() {
        return BOT_NAME;
    }
}
