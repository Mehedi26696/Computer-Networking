import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Upload File");
        System.out.println("2. Download File");
        System.out.print("Choose: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        try {
            if (choice == 1) {
                System.out.print("Enter file path to upload: ");
                String filePath = scanner.nextLine();
                uploadFile(filePath);
            } else if (choice == 2) {
                System.out.print("Enter filename to download: ");
                String filename = scanner.nextLine();
                downloadFile(filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void uploadFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        try (Socket socket = new Socket(HOST, PORT);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             FileInputStream fis = new FileInputStream(file)) {

            String request = "POST /upload HTTP/1.1\r\n" +
                    "Host: " + HOST + "\r\n" +
                    "X-Filename: " + file.getName() + "\r\n" +
                    "Content-Length: " + file.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(request.getBytes());

            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
    private static void downloadFile(String filename) throws IOException {
        String downloadDir = "download";
        File dir = new File(downloadDir);
        if (!dir.exists()) dir.mkdirs();

        try (Socket socket = new Socket(HOST, PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            String request = "GET /download?filename=" + filename + " HTTP/1.1\r\n" +
                    "Host: " + HOST + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            out.write(request.getBytes());
            out.flush();

            ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
            int prev = -1, curr;
            boolean headerEnd = false;
            while (!headerEnd && (curr = in.read()) != -1) {
                headerBuffer.write(curr);
                if (prev == '\r' && curr == '\n') {
                    byte[] hb = headerBuffer.toByteArray();
                    int len = hb.length;
                    if (len >= 4 && hb[len-4] == '\r' && hb[len-3] == '\n' && hb[len-2] == '\r' && hb[len-1] == '\n') {
                        headerEnd = true;
                    }
                }
                prev = curr;
            }
            String headers = headerBuffer.toString();
            System.out.print(headers);
            

            String statusLine = headers.split("\r?\n")[0];
            if (statusLine.contains("404 Not Found")) {
                System.out.println("File not found");
                return;
            }
            else if (statusLine.contains("405 Method Not Allowed")) {
                System.out.println("Method Not Allowed");
                return;
            }
            else if (statusLine.contains("200 OK")) {
                FileOutputStream fos = new FileOutputStream(downloadDir + File.separator + filename);
                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                System.out.println("Downloaded file saved as: " + downloadDir + File.separator + filename);
            } else {
                 
            }
        }
    }
}

