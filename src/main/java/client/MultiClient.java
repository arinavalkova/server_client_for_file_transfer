package client;

import networks.Consts;
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

                    if(commandArray.length < 2) {
                        System.out.println("Don't forgot path to file! Try again!");
                        continue;
                    }

                    File file = new File(commandArray[1]);
                    if(!file.exists()) {
                        System.out.println("Bad path to file! Try again!");
                        continue;
                    }

                    System.out.println("Start loading " + commandArray[1]);

                    Tools.sendBytes(out, "loadToServer".getBytes(), Tools.Settings.SERVICE);
                    Tools.sendBytes(out, commandArray[1].getBytes(), Tools.Settings.DATA);

                    byte[] serverAnswer = Tools.getBytes(in, Tools.Settings.SERVICE, null);
                    System.out.println(new String(serverAnswer));

                } else if (commandArray[0].equals("quit")) {

                    Tools.sendBytes(out,"quit".getBytes(), Tools.Settings.SERVICE);

                    byte[] message = Tools.getBytes(in, Tools.Settings.SERVICE, null);
                    System.out.println(new String(message));
                    break;

                } else if (commandArray[0].equals("getServerFilesList")) {

                    Tools.sendBytes(out, "getServerFilesList".getBytes(), Tools.Settings.SERVICE);

                    byte[] fileList = Tools.getBytes(in, Tools.Settings.SERVICE, null);
                    System.out.println(new String(fileList));

                } else if (commandArray[0].equals("loadFromServer")) {

                    if(commandArray.length < 2) {
                        System.out.println("Don't forgot path to file! Try again!");
                        continue;
                    }

                    Tools.sendBytes(out, "loadFromServer".getBytes(), Tools.Settings.SERVICE);
                    Tools.sendBytes(out, commandArray[1].getBytes(), Tools.Settings.SERVICE);//////////////

                    byte[] fileName = Tools.getBytes(in, Tools.Settings.DATA, Consts.defaultMultiClientPath);
                    if(fileName != null) {
                        System.out.println(new String(fileName) + " successfully loaded from server!");
                    } else {
                        System.out.println(commandArray[1] + " can't upload from server. Try again!");
                    }
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
