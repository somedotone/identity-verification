package some.one.contract;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class TestServer {

    private HttpServer server;

    private boolean isRunning = false;
    private boolean isOneShot = false;



    public void startServer(String response) {
        startServer(response, 8042);
    }


    public void startServer(String response, int port) {
        try {
            isRunning = true;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new HttpHandler() {

                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                    if(isOneShot) stop();
                }
            });

            server.setExecutor(null);
            server.start();

        } catch (IOException e) {
            isRunning = false;
            e.printStackTrace();
        }
    }


    public boolean isRunning() {
        return isRunning;
    }


    public void setOneShot(boolean state) {
        isOneShot = state;
    }


    public void stop() {
        stop(0);
    }


    public void stop(int delay) {
        if(isRunning) server.stop(delay);
        isRunning = false;
    }
}
