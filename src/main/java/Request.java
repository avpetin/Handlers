import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Request {                                                          //https://selectel.ru/blog/http-request/
    private String method;
    private String path;
    private List<String> headers;
    private String body;
    private List<NameValuePair> queryList = new ArrayList<>();
    private String query;
    private RequestParser parser;

    public Request() {
        parser = new RequestParser();
    }

    public Request(String method, List<String> headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    public void setRequestMethod(String requestMethod) {
        this.method = requestMethod;
    }

    public void setHeader(List<String> headers) {
        this.headers = headers;
    }

    public void setPath(String path) {
        String[] parts = path.split("\\?");
        query = parts[1];
        queryList = getQueryParams();
        final var filePath = Path.of(".", parts[0]);
        this.path = filePath.toString();
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRequestMethod() {
        return method;
    }

    public List<String> getHeader() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }

    public Request parse(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        return parser.parseRequest(in, out);
    }

    private String getQueryParam(String name) {
        String value = null;
        List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if (param.getName().equals(name)) {
                value = param.getValue();
            }
        }
        return value;
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }

    public Request build() {
        return new Request(method, headers, body);
    }
}
