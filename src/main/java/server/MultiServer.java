package server;

import networks.Consts;
import networks.Packet;
import networks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer extends Thread {
    private  Socket socket;
    private final File file = new File("D:/secondLabNetworks/server/");

    public MultiServer() {}
    public void setSocket(Socket socket) {
        this.socket = socket;
        start();
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream (socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String line;
            while(true) {
                line = new String(Tools.getBytes(in, Tools.Settings.SERVICE, null));
                if (line.equals("quit")) {

                    System.out.println("Gotten quit from " + socket.getInetAddress() + " " + socket.getPort());

                    Tools.sendBytes(out, ("Server reply " + line + " - OK" + "\n").getBytes(), Tools.Settings.SERVICE);
                    break;
                } else if (line.equals("loadToServer")) {

                    byte[] message = Tools.getBytes(in, Tools.Settings.DATA, Consts.defaultMultiServerPath);

                    if(message != null) {
                        System.out.println(new String(message) + " successfully loaded to server!");
                        Tools.sendBytes(out, (new String(message) + " was successful loaded to server...").getBytes(), Tools.Settings.SERVICE);

                    } else {
                        System.out.println("File wanted to upload to the server but something went wrong");
                        Tools.sendBytes(out, ("Problems to upload to the server. Try again...").getBytes(), Tools.Settings.SERVICE);

                    }
                } else if(line.equals("getServerFilesList")) {

                    System.out.println("Sending file list to " + socket.getInetAddress() + " " + socket.getPort());
                    String fileList = getFileList();
                    Tools.sendBytes(out, fileList.getBytes(), Tools.Settings.SERVICE);

                    System.out.println("File list has sent to " + socket.getInetAddress() + " " + socket.getPort());

                } else if(line.equals("loadFromServer")) {

                    byte[] fileName = Tools.getBytes(in, Tools.Settings.SERVICE, null);
                    File file = findFile(fileName);
                    System.out.println("Client " + socket.getInetAddress() + " " + socket.getPort() + " tried to get " + new String(fileName));
                    if(file != null) {
                        System.out.println(new String(fileName) + " found and can be uploaded!");
                        Tools.sendBytes(out, "found".getBytes(), Tools.Settings.SERVICE);
                        Tools.sendBytes(out, (Consts.defaultMultiServerPath + new String(fileName)).getBytes(), Tools.Settings.DATA);
                    }
                    else {
                        System.out.println(new String(fileName) + " not found and can't be uploaded!");
                        Tools.sendBytes(out, "not found".getBytes(), Tools.Settings.SERVICE);
                    }
                } else {
                    System.out.println("Bad command");
                }
            }
        } catch(Exception e) {
            System.out.println("Exception : " + e);
        }
    }

    private String getFileList() {
        File[] files = file.listFiles();
        String fileList = "";

        for (File value : files) {
            fileList += value.getName() + " ";
        }

        return fileList;
    }

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);

        try (ServerSocket server = new ServerSocket(argsParser.getPort())) {
            System.out.println("Server is starting working...");

            while (!server.isClosed()) {
                Socket client = server.accept();
                System.out.println("Connection to "+ client.getInetAddress() + " " + client.getPort() + " accepted...");
                new MultiServer().setSocket(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File findFile(byte[] fileName) {
        File[] files = file.listFiles();
        String fileNameString = new String(fileName);

        for (File value : files) {
            if(value.getName().equals(fileNameString)){
                return value;
            }
        }
        return null;
    }
}

