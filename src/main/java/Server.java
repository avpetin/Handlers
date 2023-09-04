import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class Server {
    private boolean threadStarted = false;
    private Map<String, Handler> headerTypes = new HashMap<>();
    private Map<String, Map<String, Handler>> headerMap = new HashMap<>();

    private Request request = new Request();
    private String mimeType;
    private long length = 0L;
    private String responseStatus;
    private String content;

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
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final List<String> requestList = new ArrayList<>();
                final List<String> bodyList = new ArrayList<>();
                List<String> currentList = requestList;
                while (in.ready()) {
                    String s = in.readLine();
                    if(s.contains("\r\n\r\n")){
                        currentList = bodyList;
                    }
                    currentList.add(s);
                }

                String requestLine = requestList.get(0);
                final var parts = requestLine.split(" ");

                request.setRequestMethod(parts[0]);

                requestLine = requestList.get(1);
                final String[] header = requestLine.split(" ");
                request.setHeader(header[1]);

                request.setBody(bodyList);

                if (parts.length != 3) {
                    // just close socket
                    threadStarted = false;
                    return;
                }

                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    responseStatus = "HTTP/1.1 404 Not Found\r\n";
                    length = 0;
                    threadStarted = false;
                    return;
                }

                final var filePath = Path.of(".", "public", path);
                mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    responseStatus = "HTTP/1.1 200 OK\r\n";
                    final var template = Files.readString(filePath);
                    content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    );
                    threadStarted = false;
                    return;
                }

                length = Files.size(filePath);
                responseStatus = "HTTP/1.1 200 OK\r\n";
                Files.copy(filePath, out);
                threadStarted = false;
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
