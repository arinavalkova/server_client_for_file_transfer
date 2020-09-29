import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import networks.Consts;
import networks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWindowController{

    public class Server extends Thread{

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public Server() {}
        public void setSocket(Socket socket2) {
            socket = socket2;
            start();
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line;
            try {
                while (true) {
                    line = new String(Tools.getBytes(in, Tools.Settings.SERVICE, null));
                    if (line.equals("quit")) {

                        serverTextArea.appendText("Gotten quit from " + socket.getInetAddress() + " " + socket.getPort() + "\n");

                        Tools.sendBytes(out, ("Server reply " + line + " - OK" + "\n").getBytes(), Tools.Settings.SERVICE);
                        break;
                    } else if (line.equals("loadToServer")) {

                        byte[] message = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_SERVER_PATH);

                        if (message != null) {
                            serverTextArea.appendText(new String(message) + " successfully loaded to server!\n");
                            Tools.sendBytes(out, (new String(message) + " was successful loaded to server...").getBytes(), Tools.Settings.SERVICE);

                        } else {
                            serverTextArea.appendText("File wanted to upload to the server but something went wrong\n");
                            Tools.sendBytes(out, ("Problems to upload to the server. Try again...").getBytes(), Tools.Settings.SERVICE);

                        }
                    } else if (line.equals("getServerFilesList")) {

                        serverTextArea.appendText("Sending file list to " + socket.getInetAddress() + " " + socket.getPort() + "\n");
                        String fileList = Tools.getFileList();
                        Tools.sendBytes(out, fileList.getBytes(), Tools.Settings.SERVICE);

                        serverTextArea.appendText("File list has sent to " + socket.getInetAddress() + " " + socket.getPort() + "\n");

                    } else if (line.equals("loadFromServer")) {

                        byte[] fileName = Tools.getBytes(in, Tools.Settings.SERVICE, null);
                        File file = Tools.findFile(fileName);
                        serverTextArea.appendText("Client " + socket.getInetAddress() + " " + socket.getPort() + " tried to get " + new String(fileName) + "\n");
                        if (file != null) {
                            serverTextArea.appendText(new String(fileName) + " found and can be uploaded!\n");
                            Tools.sendBytes(out, "found".getBytes(), Tools.Settings.SERVICE);
                            Tools.sendBytes(out, (Consts.DEFAULT_MULTI_SERVER_PATH + new String(fileName)).getBytes(), Tools.Settings.DATA);
                        } else {
                            serverTextArea.appendText(new String(fileName) + " not found and can't be uploaded!\n");
                            Tools.sendBytes(out, "not found".getBytes(), Tools.Settings.SERVICE);
                        }
                    } else {
                        serverTextArea.appendText("Bad command\n");
                    }
                }

                Tools.closeSocketConnection(socket, in, out);
            } catch (Exception e) {
                System.out.println("Exception : " + e);
                Tools.closeSocketConnection(socket, in, out);
            }
        }

    }

    @FXML
    private JFXListView serverContentListView;

    @FXML
    private Label serverPathLabel;

    @FXML
    private JFXButton uploadButton;

    @FXML
    private JFXButton deleteButton;

    @FXML
    private JFXTextArea serverTextArea;

    @FXML
    private JFXButton changeServerPath;

    private ObservableList<String> listOfFiles = FXCollections.observableArrayList();
    private static Thread listOfServerFilesThread;

    @FXML
    void initialize() {
        startServer();
        showFileList();
        setUploadButtonEvent();
    }

    private void startServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket server = new ServerSocket(Consts.DEFAULT_SERVER_PORT)) {
                    serverTextArea.appendText("Server is starting working...\n");

                    while (!server.isClosed()) {
                        Socket client = server.accept();
                        serverTextArea.appendText("Connection to "+ client.getInetAddress() + " " + client.getPort() + " accepted...");
                        new Server().setSocket(client);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void showFileList() {
        serverContentListView.setItems(listOfFiles);

        listOfServerFilesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!listOfServerFilesThread.isInterrupted()) {
                    File file = new File(Consts.DEFAULT_MULTI_SERVER_PATH);
                    File[] files = file.listFiles();

                    ArrayList<String> fileListArray = new ArrayList<>();
                    for(int i = 0; i < files.length; i++) {
                        fileListArray.add(files[i].getName());
                    }

                    for (int i = 0; i < listOfFiles.size(); i++) {
                        String currentFile = listOfFiles.get(i);

                        if (!fileListArray.contains(currentFile))
                            Platform.runLater((() -> listOfFiles.remove(currentFile)));
                    }

                    for (int i = 0; i < fileListArray.size(); i++) {
                        String currentFile = fileListArray.get(i);

                        if (!listOfFiles.contains(currentFile))
                            Platform.runLater((() -> listOfFiles.add(currentFile)));
                    }

                    try {
                        Thread.sleep(Consts.FIVE_SEC);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
        listOfServerFilesThread.start();
    }

    private void setUploadButtonEvent() {

    }
}
