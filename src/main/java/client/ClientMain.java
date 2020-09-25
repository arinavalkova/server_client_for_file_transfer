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
            out.write(packet.getBytes());
            out.flush();

            byte[] serverAnswer = in.readAllBytes();
            System.out.println(new String(serverAnswer));

            out.write(new Packet("quit", Tools.Settings.SERVICE).getBytes());
            out.flush();

            Tools.closeSocketConnection(clientSocket, in, out);
        } catch (IOException e) {
            Tools.closeSocketConnection(clientSocket, in, out);
            System.err.println(e);
        }
    }
}
