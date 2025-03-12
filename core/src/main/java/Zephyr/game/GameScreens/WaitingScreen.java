package Zephyr.game.GameScreens;

import Zephyr.game.Main;
import Zephyr.game.network.GameClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class WaitingScreen extends ScreenAdapter implements GameClient.GameStateCallback {
    private Main game;
    private SpriteBatch batch;
    private Texture waitingBackground;
    private GameClient client;
    private BitmapFont font;
    private GlyphLayout layout;
    private float stateTime = 0;
    private String waitingMessage = "Waiting for an opponent to join...";
    private boolean gameStarted = false;

    public WaitingScreen(Main game, GameClient client) {
        this.game = game;
        this.client = client;
        client.setGameStateCallback(this);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        waitingBackground = new Texture("waiting.png");
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f);
        layout = new GlyphLayout();

        // Make sure server knows we're ready to play
        client.sendReadyToPlay();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;

        // Update animation dots
        String dots = "";
        int numDots = (int)(stateTime * 2) % 4;
        for (int i = 0; i < numDots; i++) {
            dots += ".";
        }

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(waitingBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        layout.setText(font, waitingMessage + dots, Color.WHITE, Gdx.graphics.getWidth() * 0.8f, Align.center, true);
        font.draw(batch, layout,
            Gdx.graphics.getWidth() / 2 - layout.width / 2,
            Gdx.graphics.getHeight() / 2 + layout.height);

        batch.end();

        // Game start will be handled by callbacks
    }

    @Override
    public void onGameStart(int opponentId) {
        if (!gameStarted) {
            gameStarted = true;

            // Determine player side based on ID
            String playerSide = (client.getPlayerId() < opponentId) ? "down" : "up";

            final String finalSide = playerSide;
            Gdx.app.postRunnable(() -> {
                game.setScreen(new PVPScreen(client, finalSide));
            });

            System.out.println("Game starting from WaitingScreen! Side: " + playerSide);
        }
    }

    @Override
    public void onPlayerDeath(int playerId) {

    }

    @Override
    public void onPlayerConnect(int id) {
        System.out.println("Player " + id + " connected while waiting.");
    }

    // Other required interface methods
    @Override
    public void onPlayerUpdate(int playerId, float x, float y) {}

    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {}

    @Override
    public void onPlayerDisconnect(int id) {}

    @Override
    public void dispose() {
        batch.dispose();
        waitingBackground.dispose();
        font.dispose();
    }
}
