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

                    out.writeUTF(new Packet("loadToServer", Tools.Settings.SERVICE).getString());
                    out.writeUTF(new Packet(commandArray[1], Tools.Settings.DATA).getString());
                    out.flush();

                    String serverAnswer = in.readUTF();
                    System.out.println(serverAnswer);

                } else if (commandArray[0].equals("quit")) {

                    out.writeUTF(new Packet("quit", Tools.Settings.SERVICE).getString());
                    out.flush();

                    String message = in.readUTF();
                    System.out.println(message);
                    break;

                } else if (commandArray[0].equals("getServerFilesList")) {

                    out.writeUTF(new Packet("getServerFilesList", Tools.Settings.SERVICE).getString());
                    out.flush();

                    String fileList = in.readUTF();
                    System.out.println(fileList);

                } else if (commandArray[0].equals("loadFromServer")) {

                    out.writeUTF(new Packet("loadFromServer", Tools.Settings.SERVICE).getString());
                    out.writeUTF(new Packet(commandArray[1], Tools.Settings.SERVICE).getString());
                    out.flush();

                    String file = in.readUTF();
                    System.out.println(file);

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
