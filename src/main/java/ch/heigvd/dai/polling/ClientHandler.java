package ch.heigvd.dai.polling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final CommandHandler commandHandler;

    public ClientHandler(Socket clientSocket, PollService pollService) {
        this.clientSocket = clientSocket;
        this.commandHandler = new CommandHandler(pollService);
    }

    @Override
    public void run() {

        try (
                Socket socket = clientSocket;

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(),
                                StandardCharsets.UTF_8));

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream(),
                                StandardCharsets.UTF_8))
        ) {

            System.out.println("Client connected: "
                    + socket.getRemoteSocketAddress());

            String message;

            // Read commands until the client disconnects.
            while ((message = reader.readLine()) != null) {

                String response = commandHandler.handle(message);

                writer.write(response);
                writer.write("\n");

                // Send the response immediately.
                writer.flush();

                if (message.strip().equals("QUIT")
                        && response.equals("OK")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Client connection error: "
                    + e.getMessage());

        } finally {
            System.out.println("Client disconnected");
        }
    }
}