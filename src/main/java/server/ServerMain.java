package server;

import networks.FileProtocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        try {
            ServerSocket server = new ServerSocket(4004);

            while(true)
            {
                Socket clientSocket = server.accept();
                

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                FileProtocol fileProtocol = new FileProtocol(in.readLine());
                System.out.println("Loaded " + fileProtocol.getMessage());

                out.write(fileProtocol.getMessage() + " has already loaded to server" + "\n");
                out.flush();
            }

        } catch (IOException e) {
            System.err.println(e);
        }
    }
}

