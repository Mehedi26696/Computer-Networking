import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer_13_47{
    private static int clientIdCounter = 1;
    private static HashMap<Integer, PrintWriter> clientOutputs = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Chat Server started on port 12345... waiting for clients to connect");

            new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        int clientId = clientIdCounter++;
                        ClientHandler handler = new ClientHandler(socket, clientId);
                        handler.start();
                    } catch (IOException e) {
                        System.out.println("Error accepting client connection: " + e.getMessage());
                        break;
                    }
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            System.out.println("Server is running. Commands:");
            System.out.println("- Type 'shutdown' to stop the server");
            System.out.println("- Type 'C<number>: <message>' to send a message to a specific client");
            System.out.println("  Example: C1: Hello there!");

            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("shutdown")) {
                    System.out.println("Server shutting down...");
                    break;
                }
                if (input.startsWith("C") && input.contains(":")) {
                    String[] parts = input.split(":", 2);
                    try {
                        int clientId = Integer.parseInt(parts[0].substring(1));
                        String message = parts[1].trim();
                        PrintWriter clientOutput = clientOutputs.get(clientId);
                        if (clientOutput != null) {
                            clientOutput.println("Server: " + message);
                            clientOutput.flush();
                            System.out.println("Message sent to Client " + clientId);
                        } else {
                            System.out.println("Error: No client found with ID " + clientId);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid client ID format.");
                        continue;
                    }
                } else {
                    System.out.println("Invalid format. Use: C<number>: <message>");
                    System.out.println("Example: C1: Hello!");
                }
            }

            serverSocket.close();
            scanner.close();

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private int clientId;

        public ClientHandler(Socket socket, int clientId) {
            this.clientSocket = socket;
            this.clientId = clientId;
        }

        public void run() {
            try {
                BufferedReader input = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
                );
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
                clientOutputs.put(clientId, output);
                System.out.println("Client " + clientId + " connected successfully.");
                output.println("Welcome! You are Client " + clientId);
                output.println("Type 'exit' to disconnect from the server.");
                String clientMessage;
                while ((clientMessage = input.readLine()) != null) {
                    if (clientMessage.equalsIgnoreCase("exit")) {
                        System.out.println("Client " + clientId + " disconnected.");
                        break;
                    }
                    System.out.println("Client " + clientId + " says: " + clientMessage);
                }
                clientOutputs.remove(clientId);
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error handling Client " + clientId + ": " + e.getMessage());
                clientOutputs.remove(clientId);
            }
        }
    }
}
