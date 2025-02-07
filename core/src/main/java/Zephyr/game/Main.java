package Zephyr.game;

import com.badlogic.gdx.Game;
import Zephyr.game.GameScreens.PVPScreen;
import Zephyr.game.network.GameClient;

@SuppressWarnings("unused")
public class Main extends Game {
    private GameClient client1;

    // Server address can be passed as a system property
    private static final String DEFAULT_SERVER = "https://d7ce-2405-201-e002-c856-8116-aa1f-fdb6-9f90.ngrok-free.app";
    private static final int DEFAULT_PORT = 6000;

    @Override
    public void create() {
        // Get server address from system property or use default
        String serverIp = System.getProperty("server.address", DEFAULT_SERVER);
        int serverPort = Integer.parseInt(System.getProperty("server.port", String.valueOf(DEFAULT_PORT)));

        System.out.println("Connecting to server: " + serverIp + ":" + serverPort);

        // Create and start client
        client1 = new GameClient(serverIp, serverPort, new PVPScreen(null));
        client1.create();

        // Set up the game screen
        PVPScreen gameScreen = new PVPScreen(client1);
        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (client1 != null) {
            client1.dispose();
        }
    }
}
