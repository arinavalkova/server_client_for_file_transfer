package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import networks.Consts;

public class ArgsParser {
    @Parameter(names = { "-p", "-port" }, description = "Port for connection to server")
    private Integer port = Consts.DEFAULT_SERVER_PORT;

    @Parameter(names = "-ip", description = "Port for connection to server")
    private String ip = Consts.DEFAULT_SERVER_IP;

    @Parameter(names = { "-f", "-file" }, description = "File for sending to the server")
    private String filePath = Consts.DEFAULT_FILE_PATH;

    public ArgsParser(String[] args) {
        JCommander.newBuilder()
                .addObject(this)
                .build()
                .parse(args);
    }

    public Integer getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public String getFilePath() {
        return filePath;
    }
}
