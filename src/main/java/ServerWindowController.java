import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import networks.Consts;
import networks.SpeedChecker;
import networks.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerWindowController {

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

    @FXML
    private Label speedLabel;

    @FXML
    private Label serverAnswerLabel;

    private ObservableList<String> listOfFiles = FXCollections.observableArrayList();
    private static Thread listOfServerFilesThread;

    @FXML
    void initialize() {
        setPathLabel();
        startServer();
        showFileList();
        setEvents();
    }

    private void setPathLabel() {
        serverPathLabel.setText(Consts.DEFAULT_MULTI_SERVER_PATH);
    }

    private void setEvents() {
        setUploadButtonEvent();
        setDeleteButtonEvent();
        setChangeServerPathEvent();
    }

    private void startServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket server = new ServerSocket(Consts.DEFAULT_SERVER_PORT)) {
                    serverTextArea.appendText("Server is starting working...\n");

                    while (!server.isClosed()) {
                        Socket client = server.accept();
                        serverTextArea.appendText("Connection to " + client.getInetAddress() + " " + client.getPort() + " accepted...");
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
                    for (int i = 0; i < files.length; i++) {
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
        uploadButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file == null) {
                return;
            }

            Platform.runLater((() -> serverAnswerLabel.setText("Start uploading " + file.getAbsolutePath())));

           Thread uploadThread = new Thread(new Runnable() {
               @Override
               public void run() {
                   File newFile = new File(Consts.DEFAULT_MULTI_SERVER_PATH + file.getName());
                   try {
                       Files.copy(file.toPath(), newFile.toPath());
                   } catch (IOException e) {
                       e.printStackTrace();
                   }

                   byte[] hashFile = Tools.getHash(file.getAbsolutePath());
                   byte[] hashNewFile = Tools.getHash(newFile.getAbsolutePath());
                   if (Arrays.equals(hashFile, hashNewFile)) {
                       Platform.runLater((() -> serverAnswerLabel.setText(file.getName() + " successfully uploaded!")));
                   } else {
                       Platform.runLater((() -> serverAnswerLabel.setText(file.getName() + " has problems with uploading! Try Again!")));
                       file.delete();
                   }
               }
           });
           uploadThread.start();
        });
    }

    private void setDeleteButtonEvent() {
        deleteButton.setOnAction(event -> {
            MultipleSelectionModel selectedFile = serverContentListView.getSelectionModel();
            Object selectedFileObject = selectedFile.getSelectedItem();
            if (selectedFileObject == null) {
                serverAnswerLabel.setText("File is not chosen!");
                return;
            }
            String selectedFileString = selectedFileObject.toString();

            File file = new File(Consts.DEFAULT_MULTI_SERVER_PATH + selectedFileString);
            System.out.println(Consts.DEFAULT_MULTI_SERVER_PATH + selectedFileString);
            file.setWritable(true);
            if (file.delete()) {
                serverAnswerLabel.setText(file.getName() + " successfully deleted!");
            } else {
                serverAnswerLabel.setText(file.getName() + " has problems with deleting! Try again!");
            }
        });
    }

    private void setChangeServerPathEvent() {
        changeServerPath.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(null);
            if (dir == null) {
                return;
            }
            Consts.DEFAULT_MULTI_SERVER_PATH = dir.getAbsolutePath() + "\\";

            serverPathLabel.setText(Consts.DEFAULT_MULTI_SERVER_PATH);
        });
    }

    public class Server extends Thread {

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        private SpeedChecker speedChecker;

        public Server() {
        }

        public void setSocket(Socket socket2) {
            socket = socket2;
            start();
        }

        public void run() {
            speedChecker = new SpeedChecker();

            initSocketBuffers();
            initTimerThread();
            initServer();
        }

        private void initSocketBuffers() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void initTimerThread() {
            Thread timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Tools.sleepSec(Consts.THREE_SEC);
                        Platform.runLater(() -> speedLabel.setText(String.format(" Inst: %.3f Mb/s, Aver: %.3f Mb/s",
                                speedChecker.getInstantSpeed(), speedChecker.getAverageSpeed())));
                    }
                }
            });
            timerThread.start();
        }

        private void initServer() {
            String line;
            try {
                while (true) {
                    line = new String(Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker));
                    if (line.equals("quit")) {

                        serverTextArea.appendText("Gotten quit from " + socket.getInetAddress() + " " + socket.getPort() + "\n");

                        Tools.sendBytes(out, ("Server reply " + line + " - OK" + "\n").getBytes(), Tools.Settings.SERVICE);
                        break;
                    } else if (line.equals("loadToServer")) {

                        byte[] message = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_SERVER_PATH, speedChecker);

                        if (message != null) {
                            serverTextArea.appendText(new String(message) + " successfully loaded to server!\n");
                            Tools.sendBytes(out, (new String(message) + " was successful loaded to server...").getBytes(), Tools.Settings.SERVICE);

                        } else {
                            serverTextArea.appendText("File wanted to upload to the server but something went wrong\n");
                            Tools.sendBytes(out, ("Problems to upload to the server. Try again...").getBytes(), Tools.Settings.SERVICE);
                        }
                        speedChecker.reset();
                    } else if (line.equals("getServerFilesList")) {
                        String fileList = Tools.getFileList();
                        Tools.sendBytes(out, fileList.getBytes(), Tools.Settings.SERVICE);

                    } else if (line.equals("loadFromServer")) {

                        byte[] fileName = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);
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
                        speedChecker.reset();
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
}
