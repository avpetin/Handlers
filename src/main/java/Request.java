import java.util.ArrayList;
import java.util.List;

public class Request {
    private String requestMethod;
    private List<String> header = new ArrayList<>();
    private String body;

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public List<String> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }
}
