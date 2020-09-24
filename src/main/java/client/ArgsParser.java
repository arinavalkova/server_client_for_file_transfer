package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import networks.Consts;

public class ArgsParser {
    @Parameter(names = { "-p", "-port" }, description = "Port for connection to server")
    private Integer port = Consts.defaultServerPort;

    @Parameter(names = "-ip", description = "Port for connection to server")
    private String ip = Consts.defaultServerIp;

    @Parameter(names = { "-f", "-file" }, description = "File for sending to the server")
    private String filePath = Consts.defaultFilePath;

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
