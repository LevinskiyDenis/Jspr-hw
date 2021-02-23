import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class ProcessRequestRunnable implements Runnable {

    Socket clientSocket;
    final int limit = 4096;
    final HashMap<String, Handler> handlers;

    final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
    int requestLineEnd;
    final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/static.html");
    final List<String> validMethods = List.of("GET", "POST");

    IRequestBuilder requestBuilder = new RequestBuilder();

    public ProcessRequestRunnable(Socket clientSocket, HashMap<String, Handler> handlers) {
        this.clientSocket = clientSocket;
        this.handlers = handlers;
    }

    @Override
    public void run() {

        try (
                final var in = new BufferedInputStream(clientSocket.getInputStream());
                final var out = new BufferedOutputStream(clientSocket.getOutputStream())
        ) {

            System.out.println("Имя треда " + Thread.currentThread().getName());
            System.out.println("Сокет клиента " + clientSocket.toString());
            in.mark(limit); // зачем использовать марк, когда буфер сам ляжет, если превысится его индекс
            final var buffer = new byte[limit];
            final var numberOfBytes = in.read(buffer);


            processRequestLine(out, buffer, numberOfBytes);

            processHeaders(in, out, buffer, numberOfBytes);

            processBody(in);

            Request request = requestBuilder.build();
            System.out.println(request.toString());

            this.processRequest(request, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequestLine(BufferedOutputStream out, byte[] buffer, int numberOfBytes) {

        requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, numberOfBytes);

        if (requestLineEnd == -1) {
            badRequest(out);
            return;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            badRequest(out);
            return;
        }

        final var method = requestLine[0];
        if (!validMethods.contains(method)) {
            badRequest(out);
            return;
        }
        requestBuilder.setRequestLineMethod(method);

        final var pathAndQuerySplit = requestLine[1].split("\\?");

        final var path = pathAndQuerySplit[0];
        if (!path.startsWith("/")) {
            badRequest(out);
            return;
        }
        requestBuilder.setRequestLinePath(path);

        if (pathAndQuerySplit.length == 2) {
            requestBuilder.setQueryStringParams(parse(pathAndQuerySplit[1]));
        }

        final var protocol = requestLine[2];
        requestBuilder.setRequestLineProtocol(protocol);
    }

    private void processHeaders(BufferedInputStream in, BufferedOutputStream out, byte[] buffer, int numberOfBytes) throws IOException {
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, numberOfBytes);
        if (headersEnd == -1) {
            badRequest(out);
            return;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        // считываем с того места, к которому скипнули строкой ранее
        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        System.out.println("Переменная headers" + headers);
        requestBuilder.setHeaders(headers);
    }

    private void processBody(BufferedInputStream in) throws IOException {
        if (!requestBuilder.getRequestLineMethod().equals("GET")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(requestBuilder.getHeaders(), "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                requestBuilder.setBody(body);
                System.out.println("Переменная body: " + body);
            }
        }
    }

    private void processRequest(Request request, BufferedOutputStream out) throws IOException {
        final var path = request.getRequestLinePath();
        Handler handler = handlers.get(Server.generateKeyForHandler(request.getRequestLineMethod(), request.getRequestLinePath()));
        if (handler != null) {
            handler.handle(request, out);
        } else {
            System.out.println("Хендлер не установлен. Обработка запроса по-умолчанию");
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }
    }

    private static HashMap<String, List<String>> parse(String path) {

        List<NameValuePair> queryStringParams = URLEncodedUtils.parse(path, StandardCharsets.UTF_8);
        HashMap<String, List<String>> queryStringMap = new HashMap<>();

        for (NameValuePair param : queryStringParams) {
            System.out.println("В цикле");
            if (queryStringMap.containsKey(param.getName())) {
                List<String> list = queryStringMap.get(param.getName());
                System.out.println(param.getValue());
                list.add(param.getValue());
                queryStringMap.put(param.getName(), list);
            } else {
                List<String> list = new ArrayList<>();
                list.add(param.getValue());
                queryStringMap.put(param.getName(), list);
            }

        }

        return queryStringMap;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 400 Bad Request\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}