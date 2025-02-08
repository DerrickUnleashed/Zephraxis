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
    private Player localPlayer1, localPlayer2;
    private ObjectMap<Integer, Player> remotePlayers;
    private Texture backdrop;
    private GameClient client1;
    private GameClient client2;
    private float lastUpdateTime = 0;
    private static final float UPDATE_INTERVAL = 1 / 60f;

    public PVPScreen(GameClient client1, GameClient client2) {
        this.client1 = client1;
        this.client2 = client2;
        this.remotePlayers = new ObjectMap<>();

        // Register callbacks for both clients
        client1.setGameStateCallback(this);
        client2.setGameStateCallback(this);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 600, 600);
        backdrop = new Texture("Arena.png");

        // Initialize local players at different positions
        localPlayer1 = new Player(300, 150, 200, 800, 800,"up");
        localPlayer2 = new Player(300, 450, 200, 800, 800,"down");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        localPlayer1.update(localPlayer2);
        localPlayer2.update(localPlayer1);

        lastUpdateTime += delta;
        if (lastUpdateTime >= UPDATE_INTERVAL) {
            client1.sendPlayerPosition(localPlayer1.getX(), localPlayer1.getY());
            client2.sendPlayerPosition(localPlayer2.getX(), localPlayer2.getY());
            lastUpdateTime = 0;
        }

        batch.begin();
        batch.draw(backdrop, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        localPlayer1.render(batch);
        localPlayer2.render(batch);

        for (Player player : remotePlayers.values()) {
            player.render(batch);
        }
        batch.end();
    }

    @Override
    public void onPlayerUpdate(int playerId, float x, float y) {
        if (playerId == client1.getPlayerId()) {
            localPlayer1.setPosition(x, y);
            return;
        } else if (playerId == client2.getPlayerId()) {
            localPlayer2.setPosition(x, y);
            return;
        }

        Player remotePlayer = remotePlayers.get(playerId);
        if (remotePlayer == null) {
            remotePlayer = new Player(x, y, 200, 800, 600,"up");
            remotePlayers.put(playerId, remotePlayer);
        }
        remotePlayer.setPosition(x, y);
    }

    @Override
    public void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY) {
        if (playerId == client1.getPlayerId() || playerId == client2.getPlayerId()) {
            return; // Don't process own projectiles
        }

        Player shooter = remotePlayers.get(playerId);
        if (shooter != null) {
            shooter.spawnProjectile(x, y, directionX, directionY);
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
        localPlayer1.dispose();
        localPlayer2.dispose();
        for (Player player : remotePlayers.values()) {
            player.dispose();
        }
    }
}
