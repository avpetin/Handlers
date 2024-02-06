import java.io.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers;
    private final ConcurrentHashMap<String, Handler> handlerMap = new ConcurrentHashMap<>();
    private Request request;
    private AtomicBoolean threadEnded;
    private int cnt = 0;

    public Server() {
        handlers = new ConcurrentHashMap<>();
        threadEnded = new AtomicBoolean();
    }

    public void listen(int port) {
        Thread[] threadPool = new Thread[64];

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                if (!threadEnded.get()) {
                    threadEnded.set(true);
                    threadPool[cnt] = new Thread(() -> {
                        listenProcessing(serverSocket);
                        if (++cnt >= 64) {
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
        try (
                final var socket = serverSocket.accept();
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            request = new Request().parse(in, out);
            Handler handler = getHandler(request, request.getPath());
            if (handler != null) {
                handler.handle(request, out);
            }
            threadEnded.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String reqType, String path, Handler handler) {
        handlerMap.put(path, handler);
        handlers.put(reqType, handlerMap);
        //        for(String s : Response.validPaths){
    }

    public Handler getHandler(Request request, String path) {
        for (Map.Entry<String, ConcurrentHashMap<String, Handler>> entry : handlers.entrySet()) {
            if (request.getRequestMethod().equals(entry.getKey())) {
                for (Map.Entry<String, Handler> handlerEntry : entry.getValue().entrySet()) {
                    String key = handlerEntry.getKey().substring(1);
                    if (path.contains(key))
                        return handlerEntry.getValue();
                }
                break;
            }
        }
        return null;
    }
}
