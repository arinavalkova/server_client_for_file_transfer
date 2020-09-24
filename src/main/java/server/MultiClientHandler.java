package server;

import networks.Tools;

import java.io.*;
import java.net.Socket;

public class MultiClientHandler implements Runnable {

    private static Socket clientDialog;
    private static BufferedReader in;
    private static BufferedWriter out;

    public MultiClientHandler(Socket client) {
        MultiClientHandler.clientDialog = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientDialog.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientDialog.getOutputStream()));

            while (!clientDialog.isClosed()) {

                String entry = in.readLine();

                if (entry.equals("quit")) {
                    System.out.println("Gotten quit from " + clientDialog.getInetAddress() + " " + clientDialog.getPort());
                    out.write("Server reply " + entry + " - OK" + "\n");
                    out.flush();
                    break;
                } else if (entry.equals("loadToServer")){
                    String message = in.readLine();
                    saveFile(message);
                    System.out.println(message + " loaded to server...");
                    out.write(message + "was successful loaded to server...");
                } else if(entry.equals("getServerFilesList")) {
                    System.out.println("Sending file list to " + clientDialog.getInetAddress() + " " + clientDialog.getPort());
                    out.write("----file list------" + "\n");
                    out.flush();
                    System.out.println("File list has sent to " + clientDialog.getInetAddress() + " " + clientDialog.getPort());
                } else if(entry.equals("loadFromServer")) {
                    String fileName = in.readLine();
                    String message = findFile(fileName);
                    if(message != null) {
                        out.write(message);
                    }
                    else {
                        out.write("File not find");
                    }
                    out.flush();
                } else {
                    System.out.println("Bad command");
                }
            }

            Tools.closeSocketConnection(clientDialog, in, out);

        } catch (IOException e) {
            Tools.closeSocketConnection(clientDialog, in, out);
            e.printStackTrace();
        } catch (NullPointerException e) {
            Tools.closeSocketConnection(clientDialog, in, out);
        }
    }

    private String findFile(String fileName) {
        return null;
    }

    private void saveFile(String message) {

    }
}