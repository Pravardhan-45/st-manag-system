import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final int PORT = getPort();
    private static final StudentDAO dao = new StudentDAO();

    private static int getPort() {
        String port = System.getenv("PORT");
        return (port != null) ? Integer.parseInt(port) : 8080;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/students", new StudentHandler());
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        System.out.println(">>> Student Dashboard started at http://localhost:" + PORT);
        server.start();
    }

    static class StudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String response = "";
            int statusCode = 200;
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    List<Student> students = dao.getAllStudents();
                    response = "[" + students.stream().map(Student::toJson).collect(Collectors.joining(",")) + "]";
                } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                    Map<String, String> body = parseRequestBody(exchange.getRequestBody());
                    Student s = new Student(
                        body.containsKey("id") ? Integer.parseInt(body.get("id")) : 0,
                        body.get("name"), Integer.parseInt(body.get("age")),
                        body.get("course"), Integer.parseInt(body.get("marks"))
                    );
                    if ("POST".equalsIgnoreCase(method)) dao.addStudent(s); else dao.updateStudent(s);
                    response = "{\"status\":\"success\"}";
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    int id = Integer.parseInt(exchange.getRequestURI().getQuery().split("=")[1]);
                    dao.deleteStudent(id);
                    response = "{\"status\":\"success\"}";
                }
            } catch (Exception e) { statusCode = 500; response = "{\"error\":\"" + e.getMessage() + "\"}"; }
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("." + path);
            if (file.exists() && !file.isDirectory()) {
                String contentType = path.endsWith(".css") ? "text/css" : (path.endsWith(".js") ? "application/javascript" : "text/html");
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                Files.copy(file.toPath(), exchange.getResponseBody());
            } else { exchange.sendResponseHeaders(404, 0); }
            exchange.getResponseBody().close();
        }
    }

    private static Map<String, String> parseRequestBody(InputStream is) throws IOException {
        String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
        Map<String, String> map = new HashMap<>();
        if (body.isEmpty()) return map;
        
        // Basic cleanup and splitting for the simple JSON format used by our frontend
        String cleanBody = body.trim();
        if (cleanBody.startsWith("{")) cleanBody = cleanBody.substring(1);
        if (cleanBody.endsWith("}")) cleanBody = cleanBody.substring(0, cleanBody.length() - 1);
        
        String[] pairs = cleanBody.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2); // Split only on the first colon
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }
}
