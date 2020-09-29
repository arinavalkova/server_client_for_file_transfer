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
import networks.Tools;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MainWindowController {

    private static Socket clientSocket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static BufferedReader reader;
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

    @FXML
    private JFXButton quitButton;

    @FXML
    void initialize() {
        try {
            clientSocket = new Socket(Consts.defaultServerIp, Consts.defaultServerPort);

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));

        } catch (IOException e) {
            e.printStackTrace();
        }

        setEvents();
        setListOfServerFilesThread();
    }

    private void setEvents() {///крестик обработать, позакрывать все везде, а если я не выберу в файл чюзере
        setQuitButtonEvent();
        setUploadToServerButtonEvent();
        setLoadFromServerButtonEvent();
    }

    private void setListOfServerFilesThread() {
        serverContentListView.setItems(listOfFiles);

        listOfServerFilesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!listOfServerFilesThread.isInterrupted()) {
                    Tools.sendBytes(out, "getServerFilesList".getBytes(), Tools.Settings.SERVICE);

                    byte[] fileList = Tools.getBytes(in, Tools.Settings.SERVICE, null);
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

    private void setQuitButtonEvent() {
        quitButton.setOnAction(event -> {
            Tools.sendBytes(out, "quit".getBytes(), Tools.Settings.SERVICE);

            byte[] message = Tools.getBytes(in, Tools.Settings.SERVICE, null);
            serverAnswerLabel.setText(new String(message));
        });
    }

    private void setUploadToServerButtonEvent() {

        uploadButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);

            serverAnswerLabel.setText("Start uploading " + file.getAbsolutePath());

            Tools.sendBytes(out, "loadToServer".getBytes(), Tools.Settings.SERVICE);
            Tools.sendBytes(out, file.getAbsolutePath().getBytes(), Tools.Settings.DATA);

            byte[] serverAnswer = Tools.getBytes(in, Tools.Settings.SERVICE, null);
            serverAnswerLabel.setText(new String(serverAnswer));
        });
    }

    private void setLoadFromServerButtonEvent() {
        loadFromServerButton.setOnAction(event -> {
            MultipleSelectionModel selectedFile = serverContentListView.getSelectionModel();
            String selectedFileString = selectedFile.getSelectedItem().toString();

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(null);

            Consts.defaultMultiClientPath = dir.getAbsolutePath() + "\\";

            Tools.sendBytes(out, "loadFromServer".getBytes(), Tools.Settings.SERVICE);
            Tools.sendBytes(out, selectedFileString.getBytes(), Tools.Settings.SERVICE);

            byte[] answer = Tools.getBytes(in, Tools.Settings.SERVICE, null);
            if(new String(answer).equals("found")) {
                byte[] fileName = Tools.getBytes(in, Tools.Settings.DATA, Consts.defaultMultiClientPath);
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
