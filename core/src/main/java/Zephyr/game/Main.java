package Zephyr.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;

import Zephyr.game.GameScreens.PVPScreen;
import Zephyr.game.network.GameClient;

@SuppressWarnings("unused")
public class Main extends Game {
    private GameClient client1;

    @Override
    public void create() {
        // Initialize the server address and port
        String serverIp = "127.0.0.1";  // Local testing
        int serverPort = 6000; // Port used for client-server communication

        // Create and start client1 (local player)
        client1 = new GameClient(serverIp, serverPort); 
        client1.create();  // Ensure the client is created properly

        // Wait for client connection before setting the screen
        setScreen(new PVPScreen(client1));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        client1.dispose();  // Proper cleanup of client resources
    }
}
