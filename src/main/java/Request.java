public class Request {
    private String requestMethod;
    private String header;
    private String body;

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }
}
