package server;

import networks.Packet;
import networks.Tools;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerMain extends Thread {
    private Socket socket;
    private final File file = new File("D:/secondLabNetworks/server/");

    public ServerMain() {}
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
                byte[] entry = Tools.getBytes(in, Tools.Settings.SERVICE);
                if(entry != null)
                    System.out.println("Loaded from " + socket.getInetAddress() + " " + socket.getPort() + " " + new String(entry));

                if (Arrays.equals(entry, "quit".getBytes())) {
                    Tools.sendBytes(out, new Packet(("Server reply loading " + new String(entry) + " - OK" + "\n").getBytes()).getBytes(), Tools.Settings.SERVICE);
                    break;
                }

                Tools.sendBytes(out, new Packet(("Server reply loading " + new String(entry) + " - OK" + "\n").getBytes()).getBytes(), Tools.Settings.SERVICE);
            }
        } catch(Exception e) {
            System.out.println("Exception : " + e);
        }
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

    private void saveFile(String message) {

    }
}

