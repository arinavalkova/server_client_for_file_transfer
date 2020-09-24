package client;

import networks.FileProtocol;

import java.io.*;
import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        FileProtocol fileProtocol = new FileProtocol(argsParser.getFilePath());
        try {

            Socket clientSocket = new Socket("localhost", 4004);

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            System.out.println("Start loading " + fileProtocol.getMessage());
            out.write(fileProtocol.getMessage() + "\n");
            out.flush();

            String serverAnswer= in.readLine();
            System.out.println(serverAnswer);
        } catch (
                IOException e) {
            System.err.println(e);
        }
    }
}
