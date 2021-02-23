import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;


public class Server {

    final int port;
    ExecutorService threadPool;
    ServerSocket serverSocket;
    HashMap<String, Handler> handlers = new HashMap<>();


    public Server(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
    }

    public void launch() {
        createServerSocket();
        processClientRequest();
    }

    public void createServerSocket() {

        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Сервер создан на порте %d \n", this.port);

    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void processClientRequest() {

        while (true) {

            System.out.println("Сервер ждет подключений клиентов");

            try {
                final var clientSocket = this.serverSocket.accept();
                System.out.println("Клиент подключился");
                ProcessRequestRunnable myRunnable = new ProcessRequestRunnable(clientSocket, this.handlers);
                threadPool.submit(myRunnable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addHandler(String requestLineMethod, String requestLinePath, Handler handler) {
        final String key = Server.generateKeyForHandler(requestLineMethod, requestLinePath);
        this.handlers.put(key, handler);
    }

    public static String generateKeyForHandler(String requestLineMethod, String requestLinePath) {
        return new StringBuilder().append(requestLineMethod).append("~").append(requestLinePath).toString();
    }

}
