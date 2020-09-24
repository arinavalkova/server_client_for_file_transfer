package networks;

public class FileProtocol {
    private String message;

    public FileProtocol(String filePath)
    {
        message = filePath;
    }

    public String getMessage() {
        return message;
    }
}
