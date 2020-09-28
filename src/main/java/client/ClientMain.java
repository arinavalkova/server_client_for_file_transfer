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
        Packet packet = new Packet(argsParser.getFilePath().getBytes());
        try {
            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Start loading " + argsParser.getFilePath());
            Tools.sendBytes(out, packet.getBytes(), Tools.Settings.SERVICE);

            byte[] serverAnswer = Tools.getBytes(in, Tools.Settings.SERVICE, null);
            System.out.println(new String(serverAnswer));

            Tools.sendBytes(out, new Packet("quit".getBytes()).getBytes(), Tools.Settings.SERVICE);

            Tools.closeSocketConnection(clientSocket, in, out);
        } catch (IOException e) {
            Tools.closeSocketConnection(clientSocket, in, out);
            System.err.println(e);
        }
    }
}
