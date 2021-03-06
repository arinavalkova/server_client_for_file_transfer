package server;

import networks.Consts;
import networks.Packet;
import networks.SpeedChecker;
import networks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    SpeedChecker speedChecker;

    public ServerMain() {
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        start();
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            speedChecker = new SpeedChecker();

            Thread timerThread = new Thread(new Runnable() {
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

            String line = null;
            byte[] entry = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_SERVER_PATH, speedChecker);
            if (entry != null)
                System.out.println("Loaded from " + socket.getInetAddress() + " " + socket.getPort() + " " + new String(entry));

            Tools.sendBytes(out, new Packet(("Server reply loading " + new String(entry) + " - OK" + "\n").getBytes()).getBytes(),
                    Tools.Settings.SERVICE);
            speedChecker.reset();
            Tools.closeSocketConnection(socket, in , out);
        } catch (Exception e) {
            System.out.println("Exception : " + e);
            speedChecker.reset();
            Tools.closeSocketConnection(socket, in , out);
        }
    }

    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);

        try (ServerSocket server = new ServerSocket(argsParser.getPort())) {
            System.out.println("Server is starting working...");

            while (!server.isClosed()) {
                Socket client = server.accept();
                System.out.println("Connection to " + client.getInetAddress() + " " + client.getPort() + " accepted...");
                new ServerMain().setSocket(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}