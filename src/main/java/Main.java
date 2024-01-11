import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
        Response response = new Response();
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                // TODO: handlers code
                response.sendResponse(Path.of(request.getPath()), responseStream);
            }
        });

        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                // TODO: handlers code
                response.sendResponse(Path.of(request.getPath()), responseStream);
            }
        });

        server.listen(9999);
    }
}