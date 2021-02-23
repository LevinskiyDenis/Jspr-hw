import java.io.*;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {

        Server server = new Server(9080, Executors.newFixedThreadPool(64));

        server.addHandler("GET", "/messages", (request, out) -> {
            try {
                out.write((
                        "HTTP/1.1 404\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.launch();
    }
}
