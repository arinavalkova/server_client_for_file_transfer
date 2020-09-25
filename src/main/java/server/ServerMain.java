package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
                String entry = in.readUTF();
                if(entry != null)
                    System.out.println("Loaded from " + socket.getInetAddress() + " " + socket.getPort() + " " + entry);

                if (entry.equalsIgnoreCase("quit")) {
                    out.writeUTF("Server reply loading " + entry + " - OK" + "\n");
                    out.flush();
                    break;
                }

                out.writeUTF("Server reply loading " + entry + " - OK" + "\n");
                out.flush();
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

