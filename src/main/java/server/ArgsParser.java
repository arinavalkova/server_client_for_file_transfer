package server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import networks.Consts;

public class ArgsParser {
    @Parameter(names = { "-p", "-port" }, description = "Port for connection to server")
    private Integer port = Consts.defaultServerPort;

    public ArgsParser(String[] args) {
        JCommander.newBuilder()
                .addObject(this)
                .build()
                .parse(args);
    }

    public Integer getPort() {
        return port;
    }
}
