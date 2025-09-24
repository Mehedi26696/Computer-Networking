import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

public class FileServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String uploadDir = "uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/download", exchange -> handleDownload(exchange, uploadDir));
        server.createContext("/upload", exchange -> handleUpload(exchange, uploadDir));
        server.setExecutor(null); // Default executor
        System.out.println("Server started on port " + port);
        server.start();
    }

    static void handleDownload(HttpExchange exchange, String uploadDir) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();
        String filename = null;
        if (query != null && query.startsWith("filename=")) {
            filename = query.substring("filename=".length());
        }

        if (!method.equalsIgnoreCase("GET")) {
            String msg = "Method Not Allowed";
            System.out.println("[SERVER] 405 Method Not Allowed for /download");
            exchange.sendResponseHeaders(405, msg.length());
            exchange.getResponseBody().write(msg.getBytes());
            exchange.close();
            return;
        }
        File file = new File(uploadDir + filename);
        if (!file.exists()) {
            String msg = "File Not Found";
            System.out.println("[SERVER] 404 Not Found: " + filename);
            exchange.sendResponseHeaders(404, msg.length());
            exchange.getResponseBody().write(msg.getBytes());
            exchange.close();
            return;
        }
        System.out.println("[SERVER] 200 OK: Downloading " + filename);
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        exchange.close();
    }

    static void handleUpload(HttpExchange exchange, String uploadDir) throws IOException {
        String method = exchange.getRequestMethod();
        if (!method.equalsIgnoreCase("POST")) {
            System.out.println("[SERVER] 405 Method Not Allowed for /upload");
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        String filename = exchange.getRequestHeaders().getFirst("X-Filename");
        if (filename == null || filename.isEmpty()) {
            filename = "upload_" + System.currentTimeMillis() + ".dat";
        }
        File file = new File(uploadDir + "upload_" + filename);
        try (InputStream is = exchange.getRequestBody();
             FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        String response = "File uploaded successfully as: " + filename;
        System.out.println("[SERVER] 200 OK: Uploaded " + filename);
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
