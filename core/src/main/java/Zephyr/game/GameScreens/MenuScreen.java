package Zephyr.game.GameScreens;

import Zephyr.game.Main;
import Zephyr.game.network.GameClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;

public class MenuScreen extends ScreenAdapter {
    private Main game;
    private SpriteBatch batch;
    private Texture background;
    private Texture startButton;
    private GameClient client1;
    private GameClient client2;
    private static final String DEFAULT_SERVER = "127.0.0.1";
    private static final int DEFAULT_PORT = 6000;
    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("<here>.png");
        startButton = new Texture("start_button.png");
    }

    @Override
    public void render(float delta) {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(startButton, Gdx.graphics.getWidth() / 2 - startButton.getWidth() / 2, 200);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
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
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        startButton.dispose();
    }
}

class GameOverScreen extends ScreenAdapter {
    private Main game;
    private SpriteBatch batch;
    private Texture background;

    public GameOverScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("game_over.png");
    }

    @Override
    public void render(float delta) {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
    }
}
