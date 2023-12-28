import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        final var server = new Server();
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
        for(String s : Response.validPaths){
            server.addHandler("GET", s, new Handler() {
                public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                    // TODO: handlers code
                    Response response = new Response();
                    response.parseRequest(responseStream, s);
                }
            });

            server.addHandler("POST", s, new Handler() {
                public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                    // TODO: handlers code
                    Response response = new Response();
                    response.parseRequest(responseStream, s);
                }
            });
        }

        server.listen(9999);
    }
}