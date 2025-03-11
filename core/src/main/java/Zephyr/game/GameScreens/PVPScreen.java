package Zephyr.game.GameScreens;

import Zephyr.game.network.GameClient;
import Zephyr.game.player.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    private static final float UPDATE_INTERVAL = 1 / 60f;
    private boolean opponentConnected = false;
    private String playerSide;
    private BitmapFont font;

    public PVPScreen(GameClient client, String side) {
        this.client = client;
        this.playerSide = side;
        this.remotePlayers = new ObjectMap<>();
        client.setGameStateCallback(this);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        backdrop = new Texture("Arena.png");
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // Initialize local player based on assigned side
        int startY = playerSide.equals("down") ? 100 : 450;
        localPlayer = new Player(300, startY, 200, 800, 600, playerSide);

        System.out.println("PVP Screen initialized. Assigned side: " + playerSide);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update local player (pass opponent for collision detection if available)
        Player opponent = null;
        if (!remotePlayers.isEmpty()) {
            opponent = remotePlayers.values().next();
        }
        localPlayer.update(opponent);

        // Send position updates at regular intervals
        lastUpdateTime += delta;
        if (lastUpdateTime >= UPDATE_INTERVAL) {
            client.sendPlayerPosition(localPlayer.getX(), localPlayer.getY());
            lastUpdateTime = 0;
        }

        batch.begin();
        batch.draw(backdrop, 0, 0, 800, 600);

        // Render local player
        localPlayer.render(batch);

        // Render all remote players
        for (Player player : remotePlayers.values()) {
            player.render(batch);
        }

        // Display player info
        font.draw(batch, "Your ID: " + client.getPlayerId() + " (" + playerSide + ")", 10, 580);

        // Display opponent info if connected
        if (opponentConnected && !remotePlayers.isEmpty()) {
            font.draw(batch, "Opponent Connected", 10, 550);
        } else {
            font.draw(batch, "Waiting for opponent...", 10, 550);
        }

        batch.end();
    }

    @Override
    public void onPlayerUpdate(int playerId, float x, float y) {
        if (playerId == client.getPlayerId()) {
            // Update local player position (server authority)
            localPlayer.setPosition(x, y);
            return;
        }

        // Handle remote player updates
        Player remotePlayer = remotePlayers.get(playerId);
        if (remotePlayer == null) {
            // Create opponent with correct side (opposite of local player)
            String remoteSide = playerSide.equals("down") ? "up" : "down";
            remotePlayer = new Player(x, y, 200, 800, 600, remoteSide);
            remotePlayers.put(playerId, remotePlayer);
            opponentConnected = true;
        }
        remotePlayer.setPosition(x, y);
    }

    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {
        if (playerId == client.getPlayerId()) {
            // Our own projectile - already handled locally
            return;
        }

        // Remote player projectile
        Player shooter = remotePlayers.get(playerId);
        if (shooter != null) {
            shooter.spawnProjectile(x, y, directionX, directionY);
        } else {
            // If we don't have the remote player yet, create a temporary one
            String remoteSide = playerSide.equals("down") ? "up" : "down";
            Player tempPlayer = new Player(x, y, 200, 800, 600, remoteSide);
            tempPlayer.spawnProjectile(x, y, directionX, directionY);
            remotePlayers.put(playerId, tempPlayer);
        }
    }

    @Override
    public void onPlayerConnect(int id) {
        System.out.println("Player " + id + " connected to PVP game");
        if (id != client.getPlayerId() && !remotePlayers.containsKey(id)) {
            opponentConnected = true;
        }
    }

    @Override
    public void onPlayerDisconnect(int id) {
        Player removed = remotePlayers.remove(id);
        if (removed != null) {
            removed.dispose();
            opponentConnected = false;
        }
        System.out.println("Player " + id + " disconnected from game");
    }

    @Override
    public void onGameStart(int opponentId) {
        // Already in game, ignore
    }

    @Override
    public void dispose() {
        batch.dispose();
        backdrop.dispose();
        localPlayer.dispose();
        font.dispose();

        for (Player player : remotePlayers.values()) {
            player.dispose();
        }
    }
}
