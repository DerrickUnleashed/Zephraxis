package Zephyr.game;

import Zephyr.game.GameScreens.PVPScreen;
import Zephyr.game.network.GameClient;
import Zephyr.game.network.GameServer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends Game {

    private boolean isServerMode;

    @Override
    public void create() {
        // Prompt the user to choose server or client mode
        Gdx.app.log("Main", "Do you want to start as a server or client?");

        // Simulate user input for simplicity here; replace this with actual input logic.
        String mode = "client"; // Change this to "server" to run the server, or "client" for the client.

        if ("server".equalsIgnoreCase(mode)) {
            startServer();
        } else if ("client".equalsIgnoreCase(mode)) {
            startClient();
        } else {
            Gdx.app.log("Main", "Invalid mode selected. Exiting...");
            Gdx.app.exit();
        }
    }

    private void startServer() {
        // Start the game server in a new thread
        new Thread(() -> {
            GameServer.main(new String[]{});
            Gdx.app.postRunnable(() -> {
                Gdx.app.log("Main", "Server started. Waiting for players...");
                // After starting the server, you can log the server status or do other operations.
                // No need to transition to a separate screen for the server.
            });
        }).start();
    }

    private void startClient() {
        GameClient client = new GameClient();
        client.create(); // Initialize the client
        setScreen(new PVPScreen(client)); // Transition to the PVP screen where the client is active
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
