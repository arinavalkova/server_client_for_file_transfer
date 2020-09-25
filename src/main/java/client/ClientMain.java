package client;

import networks.Packet;
import networks.Tools;

import java.io.*;
import java.net.Socket;

public class ClientMain {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        Packet packet = new Packet(argsParser.getFilePath(), Tools.Settings.DATA);
        try {
            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Start loading " + argsParser.getFilePath());
            out.writeUTF(packet.getString());
            out.flush();

            String serverAnswer = in.readUTF();
            System.out.println(serverAnswer);

            out.writeUTF(new Packet("quit", Tools.Settings.SERVICE).getString());
            out.flush();

            Tools.closeSocketConnection(clientSocket, in, out);
        } catch (IOException e) {
            Tools.closeSocketConnection(clientSocket, in, out);
            System.err.println(e);
        }
    }
}
