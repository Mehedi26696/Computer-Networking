import java.io.*;
import java.net.*;
import java.util.Date;

public class  Server {
    private static final int PORT = 8080;
    private static final String UPLOAD_DIR = "uploads/";

    public static void main(String[] args) throws IOException {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("HTTP Socket Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                StringBuilder headerBuilder = new StringBuilder();
                int prev = -1, curr;
                boolean headerEnd = false;
                while (!headerEnd && (curr = is.read()) != -1) {
                    headerBuilder.append((char) curr);
                    if (prev == '\r' && curr == '\n') {
                        int len = headerBuilder.length();
                        if (len >= 4 &&
                            headerBuilder.charAt(len - 4) == '\r' &&
                            headerBuilder.charAt(len - 3) == '\n' &&
                            headerBuilder.charAt(len - 2) == '\r' &&
                            headerBuilder.charAt(len - 1) == '\n') {
                            headerEnd = true;
                        }
                    }
                    prev = curr;
                }
                String headersStr = headerBuilder.toString();
                BufferedReader headerReader = new BufferedReader(new StringReader(headersStr));
                String requestLine = headerReader.readLine();
                if (requestLine == null) return;
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length < 2) return;
                String method = requestParts[0];
                String path = requestParts[1];
                String line;
                int contentLength = -1;
                String xFilename = null;
                while ((line = headerReader.readLine()) != null && !line.isEmpty()) {
                    String lower = line.toLowerCase();
                    if (lower.startsWith("content-length:")) {
                        try {
                            contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                        } catch (NumberFormatException e) {
                            contentLength = -1;
                        }
                    } else if (lower.startsWith("x-filename:")) {
                        xFilename = line.substring("x-filename:".length()).trim();
                    }
                }

                if (method.equalsIgnoreCase("GET") && path.startsWith("/download")) {
                    handleGet(path, out);
                } else if (method.equalsIgnoreCase("POST") && path.equals("/upload")) {
                    handlePost(is, out, xFilename, contentLength);
                } else {
                    sendResponse(out, "HTTP/1.1 405 Method Not Allowed\r\n\r\nMethod Not Allowed");
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleGet(String path, OutputStream out) throws IOException {
            String filename = null;
            if (path.contains("filename=")) {
                filename = path.split("filename=")[1];
            }

            if (filename == null || filename.isEmpty()) {
                sendResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\nMissing filename");
                return;
            }

            File file = new File(UPLOAD_DIR + filename);
            if (!file.exists()) {
                sendResponse(out, "HTTP/1.1 404 Not Found\r\n\r\nFile Not Found");
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            String headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: " + file.length() + "\r\n" +
                    "Content-Type: application/octet-stream\r\n" +
                    "Content-Disposition: attachment; filename=\"" + filename + "\"\r\n" +
                    "\r\n";
            out.write(headers.getBytes());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            fis.close();
        }

        private void handlePost(InputStream is, OutputStream out, String filename, int contentLength) throws IOException {
            if (filename == null || filename.isEmpty()) {
                filename = "upload_" + new Date().getTime() + ".dat";
            }
            if (contentLength <= 0) {
                String response = "HTTP/1.1 411 Length Required\r\n\r\nContent-Length header missing or invalid.";
                out.write(response.getBytes());
                return;
            }
            FileOutputStream fos = new FileOutputStream(new File(UPLOAD_DIR + "upload_" + filename));
            byte[] buffer = new byte[4096];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int toRead = Math.min(buffer.length, contentLength - totalRead);
                int bytesRead = is.read(buffer, 0, toRead);
                if (bytesRead == -1) break;
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            fos.close();
            String response = "HTTP/1.1 200 OK\r\n\r\nFile uploaded successfully as: " + filename;
            out.write(response.getBytes());
        }

        private void sendResponse(OutputStream out, String response) throws IOException {
            out.write(response.getBytes());
        }
    }
}

