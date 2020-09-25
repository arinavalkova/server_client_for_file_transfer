package server;

import networks.Packet;
import networks.Tools;

import java.io.*;
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

            String line = null;
            while(true) {
                line = in.readUTF();
                if (line.equals("quit")) {

                    System.out.println("Gotten quit from " + socket.getInetAddress() + " " + socket.getPort());
                    out.writeUTF(new Packet("Server reply " + line + " - OK" + "\n", Tools.Settings.SERVICE).getString());
                    out.flush();
                    break;

                } else if (line.equals("loadToServer")) {

                    String message = in.readUTF();
                    saveFile(message);
                    System.out.println(message + " loaded to server...");
                    out.writeUTF(new Packet(message + " was successful loaded to server...", Tools.Settings.SERVICE).getString());

                } else if(line.equals("getServerFilesList")) {

                    System.out.println("Sending file list to " + socket.getInetAddress() + " " + socket.getPort());
                    String fileList = getFileList();
                    out.writeUTF(new Packet(fileList, Tools.Settings.SERVICE).getString());
                    out.flush();
                    System.out.println("File list has sent to " + socket.getInetAddress() + " " + socket.getPort());

                } else if(line.equals("loadFromServer")) {

                    String fileName = in.readUTF();
                    File file = findFile(fileName);
                    System.out.println("Client " + socket.getInetAddress() + " " + socket.getPort() + " tried to get " + fileName);
                    if(file != null) {
                        out.writeUTF(new Packet("File found", Tools.Settings.SERVICE).getString());
                    }
                    else {
                        out.writeUTF(new Packet("File not found", Tools.Settings.SERVICE).getString());
                    }
                    out.flush();

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

    private File findFile(String fileName) {
        File[] files = file.listFiles();

        for (File value : files) {
            if(value.getName().equals(fileName)){
                return value;
            }
        }
        return null;
    }

    private void saveFile(String message) {

    }
}

