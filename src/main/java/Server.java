import java.io.*;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private boolean threadStarted = false;
    private final Map<String, Handler> headerTypes = new HashMap<>();
    private final Map<String, Map<String, Handler>> headerMap = new HashMap<>();

    private final Request request = new Request();
    private String mimeType;
    private long length = 0L;
    private String responseStatus;
    private String content;

    private String path;

    public void listen(int port) {
        Thread[] threadPool = new Thread[64];
        int cnt = 0;

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                if (cnt < 64 && !threadStarted) {
                    threadPool[cnt] = new Thread(() -> {
                        listenProcessing(serverSocket);
                    });
                    threadStarted = true;
                    threadPool[cnt++].start();
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenProcessing(ServerSocket serverSocket) {
        final var allowedMethods = List.of(GET, POST);
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedInputStream(socket.getInputStream());
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                // лимит на request line + заголовки
                final var limit = 4096;

                in.mark(limit);
                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                // ищем request line
                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
                if (requestLineEnd == -1) {
                    badRequest(out);
                    continue;
                }

                // читаем request line
                final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                if (requestLine.length != 3) {
                    badRequest(out);
                    continue;
                }

                final var method = requestLine[0];
                if (!allowedMethods.contains(method)) {
                    badRequest(out);
                    continue;
                }
                System.out.println(method);

                final var path = requestLine[1];
                if (!path.startsWith("/")) {
                    badRequest(out);
                    continue;
                }
                System.out.println(path);

                // ищем заголовки
                final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                final var headersStart = requestLineEnd + requestLineDelimiter.length;
                final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                if (headersEnd == -1) {
                    badRequest(out);
                    continue;
                }

                // отматываем на начало буфера
                in.reset();
                // пропускаем requestLine
                in.skip(headersStart);

                final var headersBytes = in.readNBytes(headersEnd - headersStart);
                final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
                System.out.println(headers);

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

                final var filePath = Path.of(".", "public", path);
                mimeType = Files.probeContentType(filePath);

                if (!validPaths.contains(path)) {
                    badRequest(out);
                    continue;
                }
                // special case for classic
                if (path.equals("/classic.html")) {
                    responseStatus = "HTTP/1.1 200 OK\r\n";
                    final var template = Files.readString(filePath);
                    content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    );
                    threadStarted = false;
//                    getHandler(request).handle(request, out);
                }

                length = Files.size(filePath);
//                responseStatus = "HTTP/1.1 200 OK\r\n";
//                Handler handler = getHandler(request);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: + mimeType + \r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
                threadStarted = false;

//                HttpClient httpClient = (HttpClient) HttpClient.newBuilder();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void addHandler(String reqType, String path, Handler handler) {
        headerTypes.put(path, handler);
        headerMap.put(reqType, headerTypes);
    }

    public Handler getHandler(Request request){
        for(Map.Entry<String, Map<String, Handler>> entry : headerMap.entrySet()){
            if(request.getRequestMethod().equals(entry.getKey())){
                for(Map.Entry<String, Handler> handlerEntry : entry.getValue().entrySet()){
                    if(path.equals(handlerEntry.getKey()))
                        return handlerEntry.getValue();
                }
                break;
            }
        }
        return null;
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
                        "Content-Length: 0\r\n" +
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

    public String getContentType(){
        return mimeType;
    }

    public long getLength(){
        return length;
    }

    public String getResponseStatus(){
        return responseStatus;
    }

    public String getContent(){
        return content;
    }
}
