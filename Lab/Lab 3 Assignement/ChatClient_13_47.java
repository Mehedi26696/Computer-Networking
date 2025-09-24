import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient_13_47{
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            System.out.println("Connected to server!");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            Scanner sc = new Scanner(System.in);
            while (true) {
                String msg = sc.nextLine();
                out.println(msg);
                if (msg.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
