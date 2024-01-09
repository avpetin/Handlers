
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Response {
    final static List<String> validPaths = List.of(
            "/index.html",
            "/spring.svg",
            "/spring.png",
            "/resources.html",
            "/styles.css",
            "/app.js",
            "/links.html",
            "/forms.html",
            "/classic.html",
            "/events.html",
            "/events.js"
    );

    public void parseRequest(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        if (!validPaths.contains(path)) {
            requestNotFound(out, path);
        }

        path = getQueryParam(path, "UTF-8");

        // special case for classic
        switch(path){
            case "/classic.html":
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                requestOk(out, mimeType, content.length);
                break;
            default:
                final var length = Files.size(filePath);
                requestOk(out, mimeType, length);
                break;
        }
        Files.copy(filePath, out);
        out.flush();
    }

    private String getQueryParam(String name){
        return URLEncodedUtils.parse(name, "UTF-8");
    }


    private void requestNotFound(BufferedOutputStream out, String path) throws IOException{
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        }
    }

    private void requestOk(BufferedOutputStream out, String mimeType, long length) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: "+ mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

/*    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }*/
}
