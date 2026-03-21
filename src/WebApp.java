import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebApp {
    public static void start() {
        try {
            // This starts a tiny web server so Azure is happy
            HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/", (exchange) -> {
                String response = "<h1>Todo App is running!</h1><p>The GUI is running in the background.</p>";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Web Server started on port 80");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
