import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.List;

public class Request {                                                          //https://selectel.ru/blog/http-request/
    private String method;
    private String path;
    private List<String> headers;
    private String body;
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
        this.path = path;
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

    public String getQueryParam(String name) {
        String value = null;
        List<NameValuePair> params = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            if (param.getName() == name) {
                value = param.getValue();
            }
        }
        return value;
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(path, StandardCharsets.UTF_8);
    }

    public Request build() {
        return new Request(method, headers, body);
    }
}
