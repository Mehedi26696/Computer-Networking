import java.io.*;
import java.net.*;

public class ChatServer_13_47 {
    public static void main(String[] args) {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("File Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
 
                File folder = new File("ServerFiles");
              

                String filesList = "Available files:\n";
                String[] names = folder.list();
                if (names != null) {
                    boolean foundFile = false;
                    for (int i = 0; i < names.length; i++) {
                        File f = new File(folder, names[i]);
                        if (f.isFile()) {
                            filesList += names[i] + "\n";
                            foundFile = true;
                        }
                    }
                    if (!foundFile) {
                        filesList += "<No files found>\n";
                    }
                } else {
                    filesList += "<No files found>\n";
                }
                writer.write(filesList);
                writer.write("Enter the file name you want to download:\n");
                writer.flush();

                String fileName = reader.readLine();
                File file = new File(folder, fileName);
                
                if (file.exists() && file.isFile()) {
                
                    writer.write("FOUND\n");
                    writer.flush();
                    
                    long fileSize = file.length();
                    dataOut.writeLong(fileSize);
                    
             
                    FileInputStream fileInput = new FileInputStream(file);
                    byte[] buffer = new byte[4*1024];
                    int bytesRead = 0;
                    
                   
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, bytesRead);
                        System.err.println("Sent " + bytesRead + " bytes");
                    }
                    
                    dataOut.flush();
                    fileInput.close();
                    System.out.println("Successfully sent file: " + fileName);
                } else {
           
                    writer.write("NOT_FOUND\n");
                    writer.flush();
                    System.out.println("File not found: " + fileName);
                }

                reader.close();
                writer.close();
                dataOut.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Client error: " + e.getMessage());
            }
        }
    }
}
