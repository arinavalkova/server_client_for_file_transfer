package client;

import networks.FileProtocol;
import networks.Tools;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.Socket;

public class MultiClient {

    private static Socket clientSocket;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static BufferedReader reader;

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);

        try {
            clientSocket = new Socket(argsParser.getIp(), argsParser.getPort());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
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
                    FileProtocol fileProtocol = new FileProtocol(commandArray[1]);
                    System.out.println("Start loading " + commandArray[1]);
                    out.write("loadToServer");
                    out.write(fileProtocol.getMessage() + "\n");
                    out.flush();

                    String serverAnswer = in.readLine();
                    System.out.println(serverAnswer);
                } else if (commandArray[0].equals("quit")) {
                    out.write("quit" + "\n");
                    out.flush();

                    String message = in.readLine();
                    System.out.println(message);
                    break;
                } else if (commandArray[0].equals("getServerFilesList")) {
                    out.write("getServerFilesList" + "\n");
                    out.flush();

                    String fileList = in.readLine();
                    System.out.println(fileList);
                    printFileList(fileList);
                } else if (commandArray[0].equals("loadFromServer")) {
                    out.write("loadFromServer " + commandArray[1]);
                    out.flush();

                    String file = in.readLine();
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

    private static void printFileList(String fileList) {

    }
}
