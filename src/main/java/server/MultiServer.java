package server;

import javafx.application.Platform;
import networks.Consts;
import networks.SpeedChecker;
import networks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;

public class MultiServer extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    SpeedChecker speedChecker;
    Thread timerThread;

    public MultiServer() {}
    public void setSocket(Socket socket) {
        this.socket = socket;
        start();
    }

    public void run() {
        try {
            in = new DataInputStream (socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            speedChecker = new SpeedChecker();

            timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        Tools.sleepSec(Consts.THREE_SEC);
                        var is = speedChecker.getInstantSpeed();
                        var as = speedChecker.getAverageSpeed();
                        if(is != 0 && as != 0)
                            System.out.println(String.format(
                                    " %s %d Inst: %.3f Mb/s, Aver: %.3f Mb/s\n", socket.getInetAddress(),
                                    socket.getPort(), is, as)
                            );
                    }
                }
            });
            timerThread.start();

            String line;
            while(true) {
                line = new String(Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker));
                if (line.equals("quit")) {

                    System.out.println("Gotten quit from " + socket.getInetAddress() + " " + socket.getPort());

                    Tools.sendBytes(out, ("Server reply " + line + " - OK" + "\n").getBytes(), Tools.Settings.SERVICE);
                    timerThread.interrupt();
                    break;
                } else if (line.equals("loadToServer")) {

                    byte[] message = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_SERVER_PATH, speedChecker);

                    if(message != null) {
                        System.out.println(new String(message) + " successfully loaded to server!");
                        Tools.sendBytes(out, (new String(message) + " was successful loaded to server...").getBytes(), Tools.Settings.SERVICE);

                    } else {
                        System.out.println("File wanted to upload to the server but something went wrong");
                        Tools.sendBytes(out, ("Problems to upload to the server. Try again...").getBytes(), Tools.Settings.SERVICE);

                    }
                    speedChecker.reset();
                } else if(line.equals("getServerFilesList")) {

                    System.out.println("Sending file list to " + socket.getInetAddress() + " " + socket.getPort());
                    String fileList = Tools.getFileList();
                    Tools.sendBytes(out, fileList.getBytes(), Tools.Settings.SERVICE);

                    System.out.println("File list has sent to " + socket.getInetAddress() + " " + socket.getPort());

                } else if(line.equals("loadFromServer")) {

                    byte[] fileName = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);
                    File file = Tools.findFile(fileName);
                    System.out.println("Client " + socket.getInetAddress() + " " + socket.getPort() + " tried to get " + new String(fileName));
                    if(file != null) {
                        System.out.println(new String(fileName) + " found and can be uploaded!");
                        Tools.sendBytes(out, "found".getBytes(), Tools.Settings.SERVICE);
                        Tools.sendBytes(out, (Consts.DEFAULT_MULTI_SERVER_PATH + new String(fileName)).getBytes(), Tools.Settings.DATA);
                    }
                    else {
                        System.out.println(new String(fileName) + " not found and can't be uploaded!");
                        Tools.sendBytes(out, "not found".getBytes(), Tools.Settings.SERVICE);
                    }
                } else {
                    System.out.println("Bad command");
                }
            }

            timerThread.interrupt();
            Tools.closeSocketConnection(socket, in, out);
        } catch(Exception e) {
            System.out.println("Exception : " + e);
            timerThread.interrupt();
            Tools.closeSocketConnection(socket, in, out);
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
}