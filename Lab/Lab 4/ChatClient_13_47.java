import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ChatClient_13_47 {
    public static void main(String[] args) {
        String serverIP = "10.68.82.110";
        int port = 12345;
        try {
            Socket socket = new Socket(serverIP, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            Scanner sc = new Scanner(System.in);

            System.out.println("Choose Option:");
            System.out.println("1. Download File");
            System.out.println("2. Exit");
            int option = sc.nextInt();
            sc.nextLine();   

            if(option != 1) {
                reader.close();
                writer.close();
                dataIn.close();
                sc.close();
                socket.close();
                System.out.println("Exiting...");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Enter the file name")) {
                    break;
                }
            }

            while (true) {
                System.out.print("Enter the file name to download (or type 'exit' to quit): ");
                String fileName = sc.nextLine();
                if (fileName.equalsIgnoreCase("exit")) {
                    break;
                }
                writer.write(fileName + "\n");
                writer.flush();

                String response = reader.readLine();
                if (response != null && response.equals("FOUND")) {
                    long fileSize = dataIn.readLong();  
                    String saveName = "download files" + File.separator + "downloaded_" + fileName;
                    FileOutputStream fos = new FileOutputStream(saveName);
                    byte[] buffer = new byte[4*1024];
                    long totalRead = 0;
                    int read = 0;
                    while (totalRead < fileSize) {
                        read = dataIn.read(buffer); 
                        if (read == -1) {
                            break;
                        }
                        fos.write(buffer, 0, read); 
                        totalRead += read;
                       // System.out.println("Downloaded " + totalRead + " of " + fileSize + " bytes");
                    }
                    fos.flush();
                    fos.close();
                    System.out.println("File downloaded as: " + saveName);
                    System.out.println("Download completed. File Size: " + fileSize/1024 + " KB");
                } else {
                    System.out.println("File not found on server.");
                }
            }

            reader.close();
            writer.close();
            dataIn.close();
            sc.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
