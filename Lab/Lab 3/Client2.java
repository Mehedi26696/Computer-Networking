import java.io.*;
import java.net.*;

public class Client2 {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8080;
        
        Socket socket = null;
        DataInputStream dataIn = null;
        DataOutputStream dataOut = null;
        BufferedReader userInput = null;
        
        try {
           
            socket = new Socket(serverAddress, port);
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());
            userInput = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.println("Connected to server: " + serverAddress + ":" + port);
            
            String message;
            while (true) {
                System.out.print("Enter message (type 'Exit' to quit): ");
                message = userInput.readLine();
                
                if (message == null || "Exit".equalsIgnoreCase(message.trim())) {
                    dataOut.writeUTF("Exit");
                    break;
                }
                
                dataOut.writeUTF(message);
                String response = dataIn.readUTF();
                
                if (response != null) {
                    System.out.println("Server: " + response);
                } else {
                    System.out.println("Server closed the connection.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (dataIn != null) dataIn.close();
                if (dataOut != null) dataOut.close();
                if (userInput != null) userInput.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
