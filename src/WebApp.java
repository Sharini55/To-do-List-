import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebApp {
    public static void start() {
        try {
            // Azure listens on Port 80. This tells Azure "I am here!"
            // Change this line in WebApp.java
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", (exchange) -> {
                String response = "<html><body style='font-family:sans-serif; text-align:center; padding-top:50px;'>" +
                                  "<h1>✅ Smart To-Do Application is Online!</h1>" +
                                  "<p>The Backend is running successfully on Azure.</p>" +
                                  "<p style='color:gray;'>Note: The Desktop GUI is active in 'Headless' mode.</p>" +
                                  "</body></html>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Web Heartbeat started on port 80");
        } catch (Exception e) {
            System.out.println("Could not start web heartbeat: " + e.getMessage());
        }
    }
}
