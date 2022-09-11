package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static final int COUNT_THREAD = 64;
    public static final String GET = "GET";
    public static final String POST = "POST";
    final List<String> allowedMethods = List.of(GET, POST);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> allHandlers = new ConcurrentHashMap<>();

    private final ExecutorService threadPull = Executors.newFixedThreadPool(COUNT_THREAD);

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPull.execute(() -> connectionProcessing(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        var methodMap = allHandlers.get(method);
        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            allHandlers.put(method, methodMap);
        }
        methodMap.put(path, handler);
    }

    private void connectionProcessing(Socket socket) {
        System.out.println(Thread.currentThread().getName());
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);

            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }

            // читаем request line
            final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }
            System.out.println(Arrays.toString(requestLine));

            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }
            System.out.println(method);

            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }
            System.out.println(path);

            // ищем заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + requestLineDelimiter.length;
            final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            final var request = new Request(method, path, headers, null);
            System.out.println(request);

            // для GET тела нет
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    final var body = new String(bodyBytes);
                    System.out.println(body);
                }
            }

            var methodMap = allHandlers.get(request.getMethod());
            if (methodMap == null) {
                notFound(out);
                return;
            }

            var handler = methodMap.get(request.getPath());
            if (handler == null) {
                notFound(out);
                return;
            }

            handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Type: " + 0 + "\r\n" +
                        "Content-Length: " + 0 + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: " + 0 + "\r\n" +
                        "Content-Length: " + 0 + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
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
