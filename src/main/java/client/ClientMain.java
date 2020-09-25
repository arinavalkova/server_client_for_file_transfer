package client;

import networks.FileProtocol;
import networks.Tools;

import java.io.*;
import java.net.Socket;

public class ClientMain {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        FileProtocol fileProtocol = new FileProtocol(argsParser.getFilePath());
        try {

            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Start loading " + fileProtocol.getMessage());
            out.writeUTF(fileProtocol.getMessage());
            out.flush();

            String serverAnswer= in.readUTF();
            System.out.println(serverAnswer);

            out.writeUTF("quit");
            out.flush();

            Tools.closeSocketConnection(clientSocket, in, out);
        } catch (IOException e) {
            Tools.closeSocketConnection(clientSocket, in, out);
            System.err.println(e);
        }
    }
}
