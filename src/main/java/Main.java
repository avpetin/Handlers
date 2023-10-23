import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {                                 //https://selectel.ru/blog/http-request/
    public static void main(String[] args){
        final var server = new Server();
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
         server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                // TODO: handlers code
                responseStream.write((
                        server.getResponseStatus() +
                                "Content-Type: " + server.getContentType() + "\r\n" +
                                "Content-Length: " + server.getLength() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                if(server.getLength() > 0){
                    responseStream.write(server.getContent()
                            .getBytes());
                }
                responseStream.flush();
            }
        });

       server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                // TODO: handlers code
                responseStream.write((
                        server.getResponseStatus() +
                                "Content-Type: " + server.getContentType() + "\r\n" +
                                "Content-Length: " + server.getLength() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                if(server.getLength() > 0){
                    responseStream.write(server.getContent()
                            .getBytes());
                }
                responseStream.flush();
            }
        });

        server.listen(9999);
    }
}