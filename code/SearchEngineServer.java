import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SearchEngineServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/search", new SearchHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String response = rankResults(query);

                // Enable CORS by setting appropriate headers. i put these beacuse I was encountering an error (Cross-Origin Resource Sharing) policy restriction. This error occurs because the frontend (My HTML page) is hosted on one origin (e.g., null or file:// if I'm loading it directly from my file system), and it's trying to make a request to a backend server (http://localhost:8080/search) hosted on a different origin (localhost:8080).
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

                exchange.sendResponseHeaders(200, response.getBytes().length);
                // This effectively sends the response back to the client.
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    public static String rankResults(String query) {
        // here i will implement tha rank logic
        return "Ranked results for query: " + query;
    }
}
