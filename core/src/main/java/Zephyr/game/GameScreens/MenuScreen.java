package Zephyr.game.GameScreens;

import Zephyr.game.Main;
import Zephyr.game.network.GameClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen extends ScreenAdapter implements GameClient.GameStateCallback {
    private SpriteBatch batch;
    private Texture menuBackground;
    private GameClient client;
    private boolean gameStarted = false;
    private Stage stage;
    private BitmapFont font;
    private Main game;
    // Reference to the start button for proper removal
    private TextButton startButton;

    public MenuScreen(GameClient client, Main game) {
        this.client = client;
        this.game = game;
        client.setGameStateCallback(this);  // Listen for server messages
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        menuBackground = new Texture("MenuBackground.png");
        font = new BitmapFont();

        // Create stage with viewport
        stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        Gdx.input.setInputProcessor(stage);

        // Create button style
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture("button_up.png")));
        buttonStyle.down = new TextureRegionDrawable(new TextureRegion(new Texture("button_down.png")));

        // Create start button (only one button as requested)
        startButton = new TextButton("", buttonStyle);
        startButton.setPosition(Gdx.graphics.getWidth() / 2 - startButton.getWidth() / 2,
            Gdx.graphics.getHeight() / 2 - startButton.getHeight() / 2);

        // Add click listener
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MenuScreen", "Start button clicked");
                // Actually remove the button from the stage
                startButton.remove();
                game.setScreen(new WaitingScreen(game, client));
            }
        });

        stage.addActor(startButton);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        batch.draw(menuBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // Draw stage with UI elements
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void onPlayerConnect(int id) {
        System.out.println("Player " + id + " connected.");
    }

    @Override
    public void onGameStart(int opponentId) {
        if (!gameStarted) {
            gameStarted = true;

            // Properly remove the start button if it still exists
            if (startButton != null && startButton.getStage() != null) {
                startButton.remove();
            }

            // Determine player side
            String playerSide = (client.getPlayerId() < opponentId) ? "down" : "up";

            // Switch to PVPScreen
            Gdx.app.postRunnable(() -> {
                game.setScreen(new PVPScreen(client, playerSide));
            });

            System.out.println("Game starting! Assigned side: " + playerSide);
        }
    }

    @Override
    public void onPlayerDeath(int playerId) {
        // not needed in MenuScreen
    }

    // Implementations for other interface methods
    @Override
    public void onPlayerUpdate(int playerId, float x, float y) {
        // Not needed in MenuScreen
    }

    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {
        // Not needed in MenuScreen
    }

    @Override
    public void onPlayerDisconnect(int id) {
        // Not needed in MenuScreen
    }

    @Override
    public void dispose() {
        batch.dispose();
        menuBackground.dispose();
        stage.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
