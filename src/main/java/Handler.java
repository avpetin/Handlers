import java.io.BufferedOutputStream;

public abstract class Handler {
    public abstract void handle(Request request, BufferedOutputStream responseStream);
}
