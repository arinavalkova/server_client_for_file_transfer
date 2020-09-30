import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
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

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientWindowController {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Thread listOfServerFilesThread;

    private ObservableList<String> listOfFiles = FXCollections.observableArrayList();

    @FXML
    private JFXListView serverContentListView;

    @FXML
    private JFXButton uploadButton;

    @FXML
    private Label serverAnswerLabel;

    @FXML
    private Label speedLabel;

    @FXML
    private JFXButton loadFromServerButton;

    public static Socket clientSocketGetter() {
        return clientSocket;
    }

    public static DataInputStream DataInputStreamGetter() {
        return in;
    }

    public static DataOutputStream DataOutputStreamGetter() {
        return out;
    }

    private SpeedChecker speedChecker;

    @FXML
    void initialize() {
        try {
            clientSocket = new Socket(Consts.DEFAULT_SERVER_IP, Consts.DEFAULT_SERVER_PORT);
            speedChecker = new SpeedChecker();

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                    Platform.runLater(() -> speedLabel.setText(String.format(" Inst: %.3f Mb/s, Aver: %.3f Mb/s",
                            speedChecker.getInstantSpeed(), speedChecker.getAverageSpeed())));
                }
            }
        });
        timerThread.start();

        setEvents();
        setListOfServerFilesThread();
    }

    private void setEvents() {
        setUploadToServerButtonEvent();
        setLoadFromServerButtonEvent();
    }

    private void setListOfServerFilesThread() {
        serverContentListView.setItems(listOfFiles);

        listOfServerFilesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!listOfServerFilesThread.isInterrupted()) {
                    if(clientSocket.isClosed())
                        break;
                    Tools.sendBytes(out, "getServerFilesList".getBytes(), Tools.Settings.SERVICE);

                    if(clientSocket.isClosed())
                        break;
                    byte[] fileList = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);
                    String fileListString = new String(fileList);
                    ArrayList<String> fileListArray = new ArrayList<>(Arrays.asList(fileListString.split(" ")));

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

    private void setUploadToServerButtonEvent() {

        uploadButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if(file == null) {
                return;
            }

            serverAnswerLabel.setText("Start uploading " + file.getAbsolutePath());

            Tools.sendBytes(out, "loadToServer".getBytes(), Tools.Settings.SERVICE);
            Tools.sendBytes(out, file.getAbsolutePath().getBytes(), Tools.Settings.DATA);

            byte[] serverAnswer = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);
            serverAnswerLabel.setText(new String(serverAnswer));
            speedChecker.reset();
        });
    }

    private void setLoadFromServerButtonEvent() {
        loadFromServerButton.setOnAction(event -> {
            MultipleSelectionModel selectedFile = serverContentListView.getSelectionModel();
            Object selectedFileObject = selectedFile.getSelectedItem();
            if(selectedFileObject == null) {
                serverAnswerLabel.setText("File is not chosen!");
                return;
            }
            String selectedFileString = selectedFileObject.toString();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(null);
            if(dir == null) {
                return;
            }
            Consts.DEFAULT_MULTI_CLIENT_PATH = dir.getAbsolutePath() + "\\";

            Tools.sendBytes(out, "loadFromServer".getBytes(), Tools.Settings.SERVICE);
            Tools.sendBytes(out, selectedFileString.getBytes(), Tools.Settings.SERVICE);

            byte[] answer = Tools.getBytes(in, Tools.Settings.SERVICE, null, speedChecker);
            if (new String(answer).equals("found")) {
                byte[] fileName = Tools.getBytes(in, Tools.Settings.DATA, Consts.DEFAULT_MULTI_CLIENT_PATH, speedChecker);
                if(fileName == null) {
                    serverAnswerLabel.setText("Problems with loading file " + selectedFileString);
                } else {
                    serverAnswerLabel.setText(new String(fileName) + " successfully loaded from server!");
                }
            } else {
                serverAnswerLabel.setText(selectedFileString + " not found on server and can't upload from server. Try again!");
            }
        });
    }
}
