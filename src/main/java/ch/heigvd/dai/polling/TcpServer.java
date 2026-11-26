package ch.heigvd.dai.polling;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {

    private final int port;
    private final int threadCount;
    private final PollService pollService;

    public TcpServer(int port, int threadCount, PollService pollService) {
        this.port = port;
        this.threadCount = threadCount;
        this.pollService = pollService;
    }

    public void start() throws IOException {

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        // Keep track of connected clients.
        Set<Socket> clients = ConcurrentHashMap.newKeySet();

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("TCP server started on port " + port);

            while (true) {

                // Wait for a new client.
                Socket clientSocket = serverSocket.accept();

                clients.add(clientSocket);

                // Handle each client in a separate thread.
                executor.execute(() -> {

                    try {
                        ClientHandler handler =
                                new ClientHandler(clientSocket, pollService);

                        handler.run();

                    } finally {
                        clients.remove(clientSocket);
                    }
                });
            }

        } finally {

            executor.shutdownNow();

            // Close remaining connections if the server stops.
            for (Socket client : clients) {
                try {
                    client.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}