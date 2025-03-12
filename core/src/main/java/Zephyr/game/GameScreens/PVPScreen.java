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
    private boolean localDeathReported = false;
    private boolean gameOver = false;
    private String gameResult = "";

    public PVPScreen(GameClient client, String side) {
        this.client = client;
        this.playerSide = side;
        this.remotePlayers = new ObjectMap<>();
        client.setGameStateCallback(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Update local player with delta time
        Player opponent = null;
        if (!remotePlayers.isEmpty()) {
            opponent = remotePlayers.values().next();
        }

        // Only update if game isn't over
        if (!gameOver) {
            // Update the local player with delta time
            localPlayer.update(opponent, delta);

            // Update all remote players' projectiles
            for (Player remotePlayer : remotePlayers.values()) {
                remotePlayer.update(localPlayer, delta); // Update remote player and their projectiles
            }

            // Check if local player died and report it
            if (localPlayer.isDead() && !localDeathReported) {
                client.sendPlayerDeath("self");
                localDeathReported = true;
                gameResult = "You Lost!";
                gameOver = true;
                System.out.println("Local player died, sent death notification");
            }

            // Check if opponent died
            if (opponent != null && opponent.isDead()) {
                gameResult = "You Won!";
                gameOver = true;
            }

            // Send position updates at regular intervals if alive
            lastUpdateTime += delta;
            if (lastUpdateTime >= UPDATE_INTERVAL) {
                if (!localPlayer.isDead()) {
                    client.sendPlayerPosition(localPlayer.getX(), localPlayer.getY());
                }
                lastUpdateTime = 0;
            }
        }

        batch.begin();
        batch.draw(backdrop, 0, 0, 800, 600);

        // Render local player
        localPlayer.render(batch);

        // Render all remote players and their projectiles
        for (Player player : remotePlayers.values()) {
            player.render(batch);
        }

        // Display player info
        font.draw(batch, "Your ID: " + client.getPlayerId() + " (" + playerSide + ")", 10, 580);

        // Display opponent info if connected
        if (opponentConnected && !remotePlayers.isEmpty()) {
            font.draw(batch, "Opponent Connected", 10, 550);

            // Display game result if game is over
            if (gameOver) {
                font.draw(batch, gameResult, 350, 300);
            }
        } else {
            font.draw(batch, "Waiting for opponent...", 10, 550);
        }

        batch.end();
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
        localPlayer = new Player(300, startY, 200, 800, 600, playerSide, client); // Pass client here

        System.out.println("PVP Screen initialized. Assigned side: " + playerSide);
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
            remotePlayer = new Player(x, y, 200, 800, 600, remoteSide, client); // Pass client here
            remotePlayers.put(playerId, remotePlayer);
            opponentConnected = true;
        }
        remotePlayer.setPosition(x, y);
    }


    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {
        System.out.println("Received projectile from player " + playerId +
            " at (" + x + "," + y + ") with direction (" +
            directionX + "," + directionY + ")");

        // Check if client is null
        if (client == null) {
            System.err.println("Error: Client is null in onProjectileSpawn!");
            return;
        }

        if (playerId == client.getPlayerId()) {
            // Our own projectile - already handled locally
            System.out.println("Ignoring our own projectile notification");
            return;
        }

        // Remote player projectile
        Player shooter = remotePlayers.get(playerId);
        if (shooter != null) {
            shooter.spawnProjectile(x, y, directionX, directionY, false);
            System.out.println("Spawned projectile for existing remote player " + playerId);
        } else {
            // If we don't have the remote player yet, create a temporary one
            String remoteSide = playerSide.equals("down") ? "up" : "down";
            Player tempPlayer = new Player(x, y, 200, 800, 600, remoteSide, client); // Pass client here
            tempPlayer.spawnProjectile(x, y, directionX, directionY, false);
            remotePlayers.put(playerId, tempPlayer);
            System.out.println("Created new remote player and spawned projectile for player " + playerId);
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
    public void onPlayerDeath(int playerId) {
        if (playerId == client.getPlayerId()) {
            // Local player death
            localPlayer.setDead(true);
            gameResult = "You Lost!";
            gameOver = true;
            System.out.println("Received death confirmation for local player " + playerId);
        } else {
            // Remote player death
            Player deadPlayer = remotePlayers.get(playerId);
            if (deadPlayer != null) {
                deadPlayer.setDead(true);
                gameResult = "You Won!";
                gameOver = true;
                System.out.println("Remote player " + playerId + " died");
            }
        }
    }

    @Override
    public void onPlayerDisconnect(int id) {
        Player removed = remotePlayers.remove(id);
        if (removed != null) {
            removed.dispose();
            opponentConnected = false;
            if (!gameOver) {
                gameResult = "Opponent Disconnected - You Win!";
                gameOver = true;
            }
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
    // Add a method to PVPScreen to get remote player IDs
    public Iterable<Integer> getRemotePlayerIds() {
        return remotePlayers.keys();
    }
}
