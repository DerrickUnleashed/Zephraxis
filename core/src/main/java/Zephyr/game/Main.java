package Zephyr.game;

import Zephyr.game.GameScreens.MenuScreen;
import Zephyr.game.network.GameClient;
import com.badlogic.gdx.Game;

public class Main extends Game {
    private GameClient client;
    private static final String DEFAULT_SERVER = "192.168.18.41"; 
    private static final int DEFAULT_PORT = 6000;

    @Override
    public void create() {
        String serverIp = System.getProperty("server.address", DEFAULT_SERVER);
        int serverPort = Integer.parseInt(System.getProperty("server.port", String.valueOf(DEFAULT_PORT)));

        System.out.println("Connecting to server: " + serverIp + ":" + serverPort);

        client = new GameClient(serverIp, serverPort, null);
        client.create();
        client.setGame(this); // Pass the game instance to the client

        setScreen(new MenuScreen(client, this)); // Pass both client and game instance
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (client != null) {
            client.dispose();
        }
    }
}
