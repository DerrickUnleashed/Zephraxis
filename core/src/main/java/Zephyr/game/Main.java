package Zephyr.game;

import com.badlogic.gdx.Game;
import Zephyr.game.GameScreens.PVPScreen;
import Zephyr.game.network.GameClient;

public class Main extends Game {
    private GameClient client1;
    private GameClient client2;

    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 6000;

    @Override
    public void create() {
        String serverIp = System.getProperty("server.address", DEFAULT_SERVER);
        int serverPort = Integer.parseInt(System.getProperty("server.port", String.valueOf(DEFAULT_PORT)));

        System.out.println("Connecting to server: " + serverIp + ":" + serverPort);

        client1 = new GameClient(serverIp, serverPort, null);
        client2 = new GameClient(serverIp, serverPort, null);

        client1.create();
        client2.create();

        PVPScreen gameScreen = new PVPScreen(client1, client2);
        client1.setGameStateCallback(gameScreen);
        client2.setGameStateCallback(gameScreen);

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
        if (client2 != null) {
            client2.dispose();
        }
    }
}
