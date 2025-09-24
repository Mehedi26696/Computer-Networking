import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class FileClient {

    static String baseURL = "http://localhost:8080";
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Upload File");
        System.out.println("2. Download File");
        System.out.print("Choose: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.print("Enter file path to upload: ");
            String filePath = scanner.nextLine();
            uploadFile(filePath);
        } else if (choice == 2) {
            System.out.print("Enter filename to download: ");
            String filename = scanner.nextLine();
            downloadFile(filename);
        }
    }

    static void uploadFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File does not exist.");
                return;
            }

            URI uri = URI.create(baseURL + "/upload");
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("X-Filename", file.getName());

            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream outputStream = connection.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            System.out.println(responseCode + " " + responseMessage);

            InputStream responseStream = (responseCode >= 200 && responseCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (responseStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Server Response: " + line);
                }
                reader.close();
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("Error uploading file: " + e.getMessage());
        }
    }

    static void downloadFile(String filename) {
        try {
            URI uri = URI.create(baseURL + "/download?filename=" + filename);
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestMethod("GET");
            // connection.setRequestMethod("POST");

            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                File downloadDir = new File("download");
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs();
                }
                String outputFile = "download/" + filename;
                InputStream inputStream = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                inputStream.close();

                System.out.println(responseCode + " " + responseMessage);
                System.out.println("Download complete. Saved as: " + outputFile);

            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                System.out.println(responseCode + " " + responseMessage);
                System.out.println("File not found on server.");
            } else if (responseCode == HttpURLConnection.HTTP_BAD_METHOD) { 
                System.out.println(responseCode + " " + responseMessage);
                System.out.println("Invalid request method.");

            } else {
                System.out.println("Server returned unexpected response: " + responseCode + " " + responseMessage);
            }

            connection.disconnect();

        } catch (Exception e) {
            System.out.println("Error downloading file: " + e.getMessage());
        }
    }
}
