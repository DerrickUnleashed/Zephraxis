package Zephyr.game.network;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient extends ApplicationAdapter {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private Thread listenerThread;
    private boolean isRunning;
    private boolean isConnected;

    @Override
    public void create() {
        // Initialize connection to the server
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 12345); // Replace with actual server IP and port
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            isRunning = true;
            isConnected = true;

            // Start a thread to listen to server messages
            listenerThread = new Thread(this::listenToServer);
            listenerThread.start();

            System.out.println("Connected to the server!");
        } catch (IOException e) {
            System.err.println("Unable to connect to the server: " + e.getMessage());
            isConnected = false;
        }
    }

    @Override
    public void render() {
        if (!isConnected) {
            ScreenUtils.clear(0.2f, 0.2f, 0.2f, 1f); // Show a different screen if not connected
            return;
        }

        // Handle user input and send messages to the server
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            sendMessage("MOVE UP");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            sendMessage("MOVE DOWN");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            sendMessage("MOVE LEFT");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            sendMessage("MOVE RIGHT");
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            sendMessage("SHOOT");
        }

        // Check for incoming messages from the server
        String message = readMessage();
        if (message != null) {
            handleServerMessage(message);
        }
    }

    public void sendMessage(String message) {
        // Send a message to the server
        if (isConnected && writer != null) {
            writer.println(message);
        }
    }

    public String readMessage() {
        // Read a message from the server
        try {
            if (reader != null && reader.ready()) {
                return reader.readLine(); // Use 'reader' to read the message from the server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if no message is available
    }

    private void listenToServer() {
        // Continuously listen to messages from the server
        try {
            String message;
            while (isRunning && (message = reader.readLine()) != null) {
                System.out.println("Server: " + message);
                handleServerMessage(message);
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        }
    }

    private void handleServerMessage(String message) {
        // Parse and handle messages from the server
        if (message.startsWith("PLAYER")) {
            // Example format: PLAYER x y
            String[] parts = message.split(" ");
            if (parts.length == 3) {
                try {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    // Update opponent's position in the game
                    System.out.println("Opponent moved to: (" + x + ", " + y + ")");
                } catch (NumberFormatException e) {
                    System.err.println("Invalid PLAYER message format: " + message);
                }
            }
        } else if (message.startsWith("SHOOT")) {
            // Handle opponent shooting
            System.out.println("Opponent fired a projectile!");
        } else {
            // Handle other types of server messages if needed
            System.out.println("Unhandled message: " + message);
        }
    }

    @Override
    public void dispose() {
        // Clean up resources and stop the listener thread
        try {
            isRunning = false;
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.join();
            }
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }
}
