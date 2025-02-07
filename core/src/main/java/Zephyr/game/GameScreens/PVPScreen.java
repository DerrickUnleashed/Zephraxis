package Zephyr.game.GameScreens;

import Zephyr.game.network.GameClient;
import Zephyr.game.player.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class PVPScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player localPlayer;
    private Player remotePlayer; // For opponent
    private Texture backdrop;
    private GameClient localClient;

    public PVPScreen(GameClient localClient) {
        this.localClient = localClient;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600); // Set camera size
        backdrop = new Texture("arena.png");

        // Initialize players
        localPlayer = new Player(400, 50, 200, 800, 600); // Local player
        remotePlayer = new Player(400, 500, 200, 800, 600); // Opponent player
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        ScreenUtils.clear(0, 0, 0, 1);

        // Update the local player
        localPlayer.update();

        // Send local player data to the server
        localClient.sendMessage("PLAYER " + localPlayer.getX() + " " + localPlayer.getY());

        // Receive opponent's data from the server
        String serverMessage = localClient.readMessage();
        if (serverMessage != null && serverMessage.startsWith("PLAYER")) {
            String[] data = serverMessage.split(" ");
            float opponentX = Float.parseFloat(data[1]);
            float opponentY = Float.parseFloat(data[2]);
            remotePlayer.setPosition(opponentX, opponentY);
        }

        // Draw the backdrop and players
        batch.begin();

        // Draw scaled backdrop
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float backdropAspectRatio = (float) backdrop.getWidth() / backdrop.getHeight();
        float screenAspectRatio = screenWidth / screenHeight;

        float scaledWidth, scaledHeight;
        if (backdropAspectRatio > screenAspectRatio) {
            scaledWidth = screenWidth;
            scaledHeight = screenWidth / backdropAspectRatio;
        } else {
            scaledHeight = screenHeight;
            scaledWidth = screenHeight * backdropAspectRatio;
        }

        batch.draw(backdrop, 0, 0, scaledWidth, scaledHeight);

        // Draw players
        localPlayer.render(batch);
        remotePlayer.render(batch);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        backdrop.dispose();
        localPlayer.dispose();
        remotePlayer.dispose();
    }
}
