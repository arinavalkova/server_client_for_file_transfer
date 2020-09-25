package client;

import networks.Packet;
import networks.Tools;

import java.io.*;
import java.net.Socket;

public class MultiClient {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static BufferedReader reader;

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);

        try {
            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("Enter command:");
                String command = reader.readLine();
                String[] commandArray = command.split(" ");

                if (commandArray[0].equals("loadToServer")) {

                    System.out.println("Start loading " + commandArray[1]);

                    out.write(new Packet("loadToServer", Tools.Settings.SERVICE).getBytes());
                    out.write(new Packet(commandArray[1], Tools.Settings.DATA).getBytes());
                    out.flush();

                    byte[] serverAnswer = in.readAllBytes();
                    System.out.println(new String(serverAnswer));

                } else if (commandArray[0].equals("quit")) {

                    Tools.sendBytes(out, new Packet("quit", Tools.Settings.SERVICE));

                    byte[] message = Tools.getBytes(in);
                    System.out.println(new String(message));
                    break;

                } else if (commandArray[0].equals("getServerFilesList")) {

                    out.write(new Packet("getServerFilesList", Tools.Settings.SERVICE).getBytes());
                    out.flush();

                    byte[] fileList = in.readAllBytes();
                    System.out.println(new String(fileList));

                } else if (commandArray[0].equals("loadFromServer")) {

                    out.write(new Packet("loadFromServer", Tools.Settings.SERVICE).getBytes());
                    out.write(new Packet(commandArray[1], Tools.Settings.SERVICE).getBytes());
                    out.flush();

                    byte[] file = in.readAllBytes();
                    System.out.println(new String(file));

                } else {

                    System.out.println("Bad command. Try again!");

                }
            } catch (IOException e) {
                Tools.closeSocketConnection(clientSocket, in, out);
                System.err.println(e);
            }
        }
        Tools.closeSocketConnection(clientSocket, in, out);
    }
}
