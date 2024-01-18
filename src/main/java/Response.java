import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public void sendResponse(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        if (!Response.validPaths.contains(filePath.toString())) {
            requestNotFound(out, filePath.toString());
        }

        // special case for classic
        if (filePath.toString().endsWith("/classic.html")) {
            final var template = Files.readString(filePath, StandardCharsets.UTF_8);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            requestOk(out, mimeType, content.length);
        } else {
            final var length = Files.size(filePath);
            requestOk(out, mimeType, length);
        }
        Files.copy(filePath, out);
        out.flush();
    }

    public static void requestNotFound(BufferedOutputStream out, String path) throws IOException {
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        }
    }

    public static void requestOk(BufferedOutputStream out, String mimeType, long length) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    public static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }
}
