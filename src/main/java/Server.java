import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final Map<String, Map<String, Handler>> handlers;
    private final Map<String, Handler> handlerMap = new HashMap<>();
    private Request request;
    private AtomicBoolean threadEnded;
    private int cnt = 0;

    public Server(){
        handlers = new HashMap<>();
        threadEnded = new AtomicBoolean();
    }

    public void listen(int port) {
        Thread[] threadPool = new Thread[64];

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                if(!threadEnded.get()) {
                    threadEnded.set(true);
                    threadPool[cnt] = new Thread(() -> {
                        listenProcessing(serverSocket);
                        if(++cnt >= 64) {
                            cnt = 0;
                        }
                    });
                    threadPool[cnt].start();
                }
                Thread.sleep(100);
           }
        } catch (InterruptedException | IOException e) {
                e.printStackTrace();
        }
    }

    private void listenProcessing(ServerSocket serverSocket) {
//        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                if(requestLine != null) {
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        // just close socket
                        return;
                    }

                    request = new Request();
                    request.setRequestMethod(parts[0]);
                    request.setHeader(parts[1]);

                    Handler handler = getHandler(request, request.getHeader());
                    if(handler != null) {
                        handler.handle(request, out);
                    }
                    threadEnded.set(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
    }

    public void addHandler(String reqType, String path, Handler handler) {
        handlerMap.put(path, handler);
        handlers.put(reqType, handlerMap);
    }

    public Handler getHandler(Request request, String path){
        for(Map.Entry<String, Map<String, Handler>> entry : handlers.entrySet()){
            String rm = request.getRequestMethod();
            String key = entry.getKey();
            if(rm.equals(key)){
                for(Map.Entry<String, Handler> handlerEntry : entry.getValue().entrySet()){
                    if(path.equals(handlerEntry.getKey()))
                        return handlerEntry.getValue();
                }
                break;
            }
        }
        return null;
    }


/*    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }*/
}
