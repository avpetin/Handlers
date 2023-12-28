import java.util.ArrayList;
import java.util.List;

public class Request { //https://selectel.ru/blog/http-request/
    private String requestMethod;
    private String header;
    private List<String> body;

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setBody(List<String> body) {
        this.body = body;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getHeader() {
        return header;
    }

    public List<String> getBody() {
        return body;
    }
}
