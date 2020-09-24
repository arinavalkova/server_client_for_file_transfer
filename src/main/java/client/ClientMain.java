package client;

import networks.FileProtocol;
import networks.Tools;

import java.io.*;
import java.net.Socket;

public class ClientMain {

    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        FileProtocol fileProtocol = new FileProtocol(argsParser.getFilePath());
        try {

            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            System.out.println("Start loading " + fileProtocol.getMessage());
            out.write(fileProtocol.getMessage() + "\n");
            out.flush();

            String serverAnswer= in.readLine();
            System.out.println(serverAnswer);

            out.write("quit");
            out.flush();

            Tools.closeSocketConnection(clientSocket, in, out);
        } catch (IOException e) {
            Tools.closeSocketConnection(clientSocket, in, out);
            System.err.println(e);
        }
    }
}
