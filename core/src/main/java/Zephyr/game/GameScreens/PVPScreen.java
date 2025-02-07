package Zephyr.game.GameScreens;

import Zephyr.game.network.GameClient;
import Zephyr.game.player.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.ObjectMap;

public class PVPScreen extends ScreenAdapter implements GameClient.GameStateCallback {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player localPlayer;
    private ObjectMap<Integer, Player> remotePlayers;
    private Texture backdrop;
    private GameClient client;
    private float lastUpdateTime = 0;
    private static final float UPDATE_INTERVAL = 1/60f; // 60 updates per second

    public PVPScreen(GameClient client) {
        this.client = client;
        this.remotePlayers = new ObjectMap<>();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        backdrop = new Texture("arena.png");

        // Initialize local player
        localPlayer = new Player(400, 50, 200, 800, 600);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update local player
        localPlayer.update();

        // Send position updates at fixed interval
        lastUpdateTime += delta;
        if (lastUpdateTime >= UPDATE_INTERVAL) {
            client.sendPlayerPosition(localPlayer.getX(), localPlayer.getY());
            lastUpdateTime = 0;
        }

        // Render game state
        batch.begin();

        // Draw backdrop
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        batch.draw(backdrop, 0, 0, screenWidth, screenHeight);

        // Draw all players
        localPlayer.render(batch);
        for (Player player : remotePlayers.values()) {
            player.render(batch);
        }

        batch.end();
    }

    @Override
    public void onPlayerUpdate(int playerId, float x, float y) {
        Player remotePlayer = remotePlayers.get(playerId);
        if (remotePlayer == null) {
            remotePlayer = new Player(x, y, 200, 800, 600);
            remotePlayers.put(playerId, remotePlayer);
        }
        remotePlayer.setPosition(x, y);
    }

    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {
        if (playerId != client.getPlayerId()) {
            Player shooter = remotePlayers.get(playerId);
            if (shooter != null) {
                shooter.spawnProjectile(x, y, directionX, directionY);
            }
        }
    }

    @Override
    public void onPlayerConnect(int id) {
        System.out.println("Player " + id + " connected");
    }

    @Override
    public void onPlayerDisconnect(int id) {
        Player removed = remotePlayers.remove(id);
        if (removed != null) {
            removed.dispose();
        }
        System.out.println("Player " + id + " disconnected");
    }

    @Override
    public void dispose() {
        batch.dispose();
        backdrop.dispose();
        localPlayer.dispose();
        for (Player player : remotePlayers.values()) {
            player.dispose();
        }
    }
}
