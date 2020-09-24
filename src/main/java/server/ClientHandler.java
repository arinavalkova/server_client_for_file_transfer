package server;

import networks.Tools;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static Socket clientDialog;
    private static BufferedReader in;
    private static BufferedWriter out;

    public ClientHandler(Socket client) {
        ClientHandler.clientDialog = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientDialog.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientDialog.getOutputStream()));

            while (!clientDialog.isClosed()) {

                String entry = in.readLine();
                if(entry != null)
                    System.out.println("Loaded from " + clientDialog.getInetAddress() + " " + clientDialog.getPort() + " " + entry);

                if (entry.equalsIgnoreCase("quit")) {
                    out.write("Server reply loading " + entry + " - OK" + "\n");
                    out.flush();
                    break;
                }

                out.write("Server reply loading " + entry + " - OK" + "\n");
                out.flush();
            }

            Tools.closeSocketConnection(clientDialog, in, out);

        } catch (IOException e) {
            Tools.closeSocketConnection(clientDialog, in, out);
            e.printStackTrace();
        }catch (NullPointerException e) {
            Tools.closeSocketConnection(clientDialog, in, out);
        }
    }
}