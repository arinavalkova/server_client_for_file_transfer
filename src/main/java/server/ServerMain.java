package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);

        try (ServerSocket server = new ServerSocket(argsParser.getPort())) {
            System.out.println("Server is starting working...");

            while (!server.isClosed()) {
                Socket client = server.accept();

                System.out.println("Connection to "+ client.getInetAddress() + " " + client.getPort() + " accepted...");

                Thread thread = new Thread(new MultiClientHandler(client));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

